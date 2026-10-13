# BUG-001: Non-admin users can access `GET /api/users/patients` (should be admin only)

| Field           | Value                                              |
| --------------- | -------------------------------------------------- |
| **Bug ID**      | BUG-001                                            |
| **Module**      | Users                                              |
| **Endpoint**    | `GET /api/users/patients`                          |
| **Type**        | Authorization / access control, privacy            |
| **Severity**    | High                                               |
| **Priority**    | High                                               |
| **Status**      | Open                                               |
| **Environment** | `http://mytherapy.runasp.net` (shared test server) |
| **Found by**    | Automated test (Rest Assured + TestNG)             |
| **Found on**    | 2026-10-09                                         |

---

## Summary

`GET /api/users/patients` returns the full list of patients with `200 OK` to users with the **Patient** role and to users with the **Therapist** role. According to the project requirements, this endpoint is for **Admins only**, to protect patients' privacy, so requests from any other role should be rejected.

The severity is high because the data belongs to patients of a therapy platform, which is sensitive: any logged-in patient can see who the other patients are.

---

## Preconditions

- An admin account, a patient account, and an approved therapist account exist.
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

| Token     | Expected                       |
| --------- | ------------------------------ |
| Admin     | `200 OK` with the patient list |
| Patient   | `403 Forbidden`                |
| Therapist | `403 Forbidden`                |
| None      | `401 Unauthorized`             |

`403` is expected for the wrong roles because the user is authenticated but not allowed. The API already returns `403` when a therapist token is used on the patient endpoint `GET /api/patient/availability`.

---

## Actual Result

| Token     | Actual                                        |
| --------- | --------------------------------------------- |
| Admin     | `200 OK` (correct)                            |
| Patient   | **`200 OK`** with the full patient list (bug) |
| Therapist | **`200 OK`** with the full patient list (bug) |
| None      | `401 Unauthorized` (correct)                  |

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
// BUG-001: a patient can currently access this endpoint (returns 200). Expected: 403.
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

// BUG-001: a therapist can currently access this endpoint (returns 200). Expected: 403.
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

- The therapists endpoints, `GET /api/users/therapists` and `GET /api/users/therapists/{id}`, are **open to all roles by design**, so users can browse therapists. That behavior is correct and is not part of this bug.
- Some of the returned names look like test data (for example "test", "string", "Hacker"). That is not part of this bug.
