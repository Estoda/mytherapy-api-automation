# BUG-002: Non-admin users can access `GET /api/users/patients` (should be admin only)

| Field           | Value                                                 |
| --------------- | ----------------------------------------------------- |
| **Bug ID**      | BUG-002                                               |
| **Module**      | Users                                                 |
| **Endpoint**    | `GET /api/users/patients`                             |
| **Type**        | Authorization / access control, privacy               |
| **Severity**    | High                                                  |
| **Priority**    | High                                                  |
| **Status**      | Open                                                  |
| **Environment** | `http://mytherapy.runasp.net` (shared test server)    |
| **Found by**    | Automated test (Rest Assured + TestNG)                |
| **Found on**    | 2026-10-09                                            |
| **Related**     | BUG-001 (same problem on `GET /api/users/therapists`) |

---

## Summary

`GET /api/users/patients` returns the full list of patients with `200 OK` to users with the **Patient** role and to users with the **Therapist** role. According to the project requirements, this endpoint is for **Admins only**, so requests from any other role should be rejected.

The severity is higher than BUG-001 because the data belongs to patients of a therapy platform, which is sensitive: any logged-in patient can see who the other patients are.

---

## Preconditions

- A patient account and an approved therapist account exist.
- Each user logs in with `POST /api/auth/login` and receives a valid JWT token.

---

## Steps to Reproduce

Repeat the steps once with the patient token and once with the therapist token.

1. Send `POST /api/auth/login` with the user's email and password.
2. Copy the `token` value from the response.
3. Send the following request:

```http
GET /api/users/patients HTTP/1.1
Host: mytherapy.runasp.net
Authorization: Bearer <patient or therapist token>
```

---

## Expected Result

| Token     | Expected                             |
| --------- | ------------------------------------ |
| Admin     | `200 OK` with the patient list       |
| Patient   | `403 Forbidden`                      |
| Therapist | `403 Forbidden`                      |
| None      | `401 Unauthorized` (to be confirmed) |

---

## Actual Result

| Token     | Actual                                                                 |
| --------- | ---------------------------------------------------------------------- |
| Admin     | Not tested yet (no admin account configured in the automation project) |
| Patient   | **`200 OK`** with the full patient list (bug)                          |
| Therapist | **`200 OK`** (bug)                                                     |
| None      | Not tested yet                                                         |

Response body returned to the patient token (first two of 10 patients):

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8

[
    {
        "id": "bba76eab-becb-4fb8-907c-34d28bd5278d",
        "fullName": "sara",
        "profilePicture": null
    },
    {
        "id": "f65c291e-72fa-4b57-bd66-89337374884d",
        "fullName": "Esraa Magdy",
        "profilePicture": "http://mytherapy.runasp.net/uploads/profiles/7291449c-fca2-4fa4-bb3d-87c90ca33c11.jpg"
    }
    ... (10 patients in total)
]
```

---

## Impact

- The endpoint's access rule (admin only) is not enforced for the Patient and Therapist roles.
- Any logged-in patient can read the ids, full names, and profile picture URLs of all other patients.
- A therapist can list patients they have no booking with.
- This is a privacy risk for a platform that handles therapy.

---

## Suggested Fix

Restrict the endpoint to the Admin role with a role-based authorization attribute:

```csharp
[Authorize(Roles = "Admin")]
```

---

## Automated Tests

Both tests are in `UsersTest.java`. They are currently **disabled** (`enabled = false`) so CI stays green. After the fix, remove `enabled = false` and they should pass.

```java
// BUG-002: a patient can currently access this endpoint (returns 200). Expected: 403.
@Test(groups = {"negative"}, enabled = false)
public void getPatientsWithPatientToken()
{
    given()
        .auth().oauth2(AuthManager.getToken(Role.PATIENT))
    .when()
        .get("/api/users/patients")
    .then()
        .statusCode(403);
}

// BUG-002: a therapist can currently access this endpoint (returns 200). Expected: 403.
@Test(groups = {"negative"}, enabled = false)
public void getPatientsWithTherapistToken()
{
    given()
        .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
    .when()
        .get("/api/users/patients")
    .then()
        .statusCode(403);
}
```

**Current result when enabled:** both fail with `Expected status code <403> but was <200>`.

---

## Notes

- The Admin role has not been tested on this endpoint. A test with an admin token (expecting `200`) needs a real admin account in the configuration.
- The no-token case for this endpoint has not been checked yet. It is expected to return `401`, like the therapists endpoint.
- The allowed role (Admin only) comes from the project owner's requirement. The README does not list the allowed roles per endpoint.
- Some of the returned names look like test data (for example "test", "string", "Hacker"). That is not part of this bug.
