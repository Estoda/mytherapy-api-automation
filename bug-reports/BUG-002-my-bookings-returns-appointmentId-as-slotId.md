# BUG-002: `GET /api/patient/bookings/my` returns the appointment id in the `slotId` field

| Field           | Value                                                                       |
| --------------- | --------------------------------------------------------------------------- |
| **Bug ID**      | BUG-002                                                                     |
| **Module**      | Patient Booking                                                             |
| **Endpoint**    | `GET /api/patient/bookings/my` (compared with `POST /api/patient/bookings`) |
| **Type**        | Data inconsistency                                                          |
| **Severity**    | Medium                                                                      |
| **Priority**    | Medium                                                                      |
| **Status**      | Open                                                                        |
| **Environment** | `http://mytherapy.runasp.net` (shared test server)                          |
| **Found by**    | Automated test (Rest Assured + TestNG)                                      |
| **Found on**    | 2026-10-17                                                                  |

---

## Summary

When a patient books a slot, `POST /api/patient/bookings` returns the booking with two **different** ids: `appointmentId` (the new appointment) and `slotId` (the slot that was booked). When the same booking is read back from `GET /api/patient/bookings/my`, the `slotId` field contains the **appointment id** instead of the real slot id.

The same field name holds two different values in two endpoints, so a client cannot use `slotId` from the booking list to identify the slot.

---

## Preconditions

- An approved therapist account and a patient account exist.
- Both users have valid JWT tokens.

---

## Steps to Reproduce

1. As the therapist, create a slot with `POST /api/therapist/availability` and copy the `slotId` from the response.
2. As the patient, book it:

```http
POST /api/patient/bookings HTTP/1.1
Host: mytherapy.runasp.net
Authorization: Bearer <patient token>
Content-Type: application/json

{ "slotId": "<slotId from step 1>" }
```

3. Note the `appointmentId` and `slotId` in the booking response.
4. As the patient, read the booking list:

```http
GET /api/patient/bookings/my HTTP/1.1
Host: mytherapy.runasp.net
Authorization: Bearer <patient token>
```

5. Find the booking with the same `appointmentId` and compare its `slotId` with the one from step 3.

---

## Expected Result

The booking in the list has the same `slotId` that was returned when the booking was created, and it is **different** from the `appointmentId`.

---

## Actual Result

The booking response (step 3):

```json
{
  "appointmentId": "3d4bd8a4-3f39-4a28-b6fe-e062a5c9eecd",
  "slotId": "ffb0478e-0eaa-4de6-9420-f5c460c0fe28",
  "therapistName": "Dr.Mohamed Ali",
  "appointmentDatetime": "2026-10-19T12:08:59",
  "durationMinutes": 60,
  "status": "Scheduled",
  "createdAt": "2026-10-17T12:08:01.7330487Z"
}
```

The same booking in the list (step 5):

```json
{
  "appointmentId": "3d4bd8a4-3f39-4a28-b6fe-e062a5c9eecd",
  "slotId": "3d4bd8a4-3f39-4a28-b6fe-e062a5c9eecd",
  "therapistName": "Dr.Mohamed Ali",
  "appointmentDatetime": "2026-10-19T12:08:59",
  "durationMinutes": 60,
  "status": "Scheduled",
  "createdAt": "2026-10-17T12:08:01.7330487"
}
```

| Field           | `POST` response            | `GET /my` list                          |
| --------------- | -------------------------- | --------------------------------------- |
| `appointmentId` | `3d4bd8a4-...`             | `3d4bd8a4-...`                          |
| `slotId`        | `ffb0478e-...` (real slot) | **`3d4bd8a4-...` (the appointment id)** |

In every booking in the list, `appointmentId` and `slotId` are identical.

---

## Impact

- A client that stores or displays `slotId` from the booking list gets the wrong id, so it cannot link a booking to its slot.
- Features that depend on the slot (for example a reschedule or cancel flow, once they exist) would use the wrong id.
- The same field returns different data in two endpoints of the same module.

---

## Suggested Fix

In the mapping code that builds the booking list response, set `slotId` from the appointment's slot (the slot that was booked), not from the appointment's own id.

---

## Automated Test

The bug is covered by a test in `BookingTest.java`. It is **disabled** (`enabled = false`) so CI stays green and no test data is created. After the fix, remove `enabled = false` and the test should pass.

```java
// BUG-002: GET /api/patient/bookings/my returns the appointment id in the slotId field.
// Expected: the real slot id. Note: when enabled, this creates one slot and one booking.
@Test(groups = {"booking-flow"}, enabled = false)
public void bookingListReturnsRealSlotId()
{
    String therapistToken = AuthManager.getToken(Role.THERAPIST);
    String patientToken = AuthManager.getToken(Role.PATIENT);

    Instant start = Instant.now().plus(2, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.MINUTES).plusSeconds(59);
    Instant end = start.plus(1, ChronoUnit.HOURS);

    String slotId = given()
            .auth().oauth2(therapistToken)
            .contentType("application/json")
            .body(new AvailabilityRequest(start.toString(), end.toString()))
        .when()
            .post("/api/therapist/availability")
        .then()
            .statusCode(200)
            .extract()
            .path("slotId");

    String appointmentId = given()
            .auth().oauth2(patientToken)
            .contentType("application/json")
            .body(new BookingRequest(slotId))
        .when()
            .post("/api/patient/bookings")
        .then()
            .statusCode(200)
            .extract()
            .path("appointmentId");

    given()
        .auth().oauth2(patientToken)
    .when()
        .get("/api/patient/bookings/my")
    .then()
        .statusCode(200)
        .body("find { it.appointmentId == '" + appointmentId + "' }.slotId", equalTo(slotId));
}
```

**Current result when enabled:** fails because the list returns the appointment id where the slot id is expected.

---

## Notes

- The test slots created by the automation always have a start time ending in `:59` seconds, so they are easy to find and remove from the database.
- The existing `createBooking` test matches the booking in the list by `appointmentId` for this reason.
