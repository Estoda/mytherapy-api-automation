# BUG-001: A therapist can access `GET /api/users/therapists` (should be denied)

| Field           | Value                                              |
| --------------- | -------------------------------------------------- |
| **Bug ID**      | BUG-001                                            |
| **Module**      | Users                                              |
| **Endpoint**    | `GET /api/users/therapists`                        |
| **Type**        | Authorization / access control                     |
| **Severity**    | Medium                                             |
| **Priority**    | Medium                                             |
| **Status**      | Open                                               |
| **Environment** | `http://mytherapy.runasp.net` (shared test server) |
| **Found by**    | Automated test (Rest Assured + TestNG)             |
| **Found on**    | 2026-10-09                                         |

---

## Summary

A logged-in user with the **Therapist** role can call `GET /api/users/therapists` and receives the full therapist list with `200 OK`. According to the project requirements, a therapist should **not** be able to access this endpoint, so the request should be rejected.

---

## Preconditions

- An approved therapist account exists.
- The therapist logs in with `POST /api/auth/login` and receives a valid JWT token.

---

## Steps to Reproduce

1. Send `POST /api/auth/login` with the therapist's email and password.
2. Copy the `token` value from the response.
3. Send the following request:

```http
GET /api/users/therapists HTTP/1.1
Host: mytherapy.runasp.net
Authorization: Bearer <therapist token>
```

---

## Expected Result

The request is **rejected**, because the Therapist role is not allowed to use this endpoint.

For consistency with the rest of the API, the expected status is **`403 Forbidden`**: the user is authenticated but not allowed. The API already returns `403` when a therapist token is used on the patient endpoint `GET /api/patient/availability`.

No therapist data is returned.

---

## Actual Result

The API returns **`200 OK`** and the complete list of therapists:

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8

[
    {
        "id": "32d66e02-85dd-4e90-9b0e-20238c5377a8",
        "fullName": "Ahmed Amin",
        "profilePicture": null
    },
    {
        "id": "9ad1bd3a-34cd-424b-8226-27b7c9ed6f86",
        "fullName": "Walaa Magdy",
        "profilePicture": "http://mytherapy.runasp.net/uploads/profiles/6b8f2e54-ac7b-44a2-b8b9-9aee55ece40d.jpg"
    }
    ... (8 therapists in total)
]
```

---

## Comparison with Other Behavior

| Request                         | Token         | Result                                   |
| ------------------------------- | ------------- | ---------------------------------------- |
| `GET /api/users/therapists`     | Patient       | 200 (allowed)                            |
| `GET /api/users/therapists`     | **Therapist** | **200 (bug: should be denied)**          |
| `GET /api/users/therapists`     | None          | 401 Unauthorized                         |
| `GET /api/patient/availability` | Therapist     | 403 Forbidden (correct for a wrong role) |

---

## Impact

- The endpoint's access rules are not enforced for the Therapist role.
- A therapist can read the names, ids, and profile picture URLs of all other therapists.
- Role-based access control is inconsistent across the API, because other endpoints do reject the wrong role.

---

## Suggested Fix

Restrict the endpoint to the allowed role(s) with a role-based authorization attribute on the controller action, for example:

```csharp
[Authorize(Roles = "Patient")]
```

(Use the role or roles the requirements allow. This example assumes patients only, and the Admin role may also need access.)

---

## Automated Test

The bug is reproduced by this test in `UsersTest.java`:

```java
@Test(groups = {"negative"})
public void getTherapistsWithTherapistToken()
{
    String token = AuthManager.getToken(Role.THERAPIST);

    given()
        .auth().oauth2(token)
    .when()
        .get("/api/users/therapists")
    .then()
        .statusCode(403);
}
```

**Current result:** the test fails with `Expected status code <403> but was <200>`.
**After the fix:** the test passes.

---

## Notes

- The Admin role has not been tested on this endpoint yet.
- The list of allowed roles for this endpoint is taken from the project owner's requirement that therapists must not access it.
- The 401 and 403 status codes have different meanings: **401** means the request has no valid identity (no token), and **403** means the identity is valid but not allowed. A therapist has a valid token, so **403** is the correct code.
