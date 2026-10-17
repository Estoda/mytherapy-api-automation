# BUG-003: `POST /api/auth/send-verification-code` returns 500 and leaks SMTP server details when the email is missing

| Field           | Value                                                    |
| --------------- | -------------------------------------------------------- |
| **Bug ID**      | BUG-003                                                  |
| **Module**      | Authentication / Email verification                      |
| **Endpoint**    | `POST /api/auth/send-verification-code`                  |
| **Type**        | Input validation, error handling, information disclosure |
| **Severity**    | High                                                     |
| **Priority**    | High                                                     |
| **Status**      | Open                                                     |
| **Environment** | `http://mytherapy.runasp.net` (shared test server)       |
| **Found by**    | Automated probe (Rest Assured + TestNG)                  |
| **Found on**    | 2026-10-17                                               |

---

## Summary

Sending a request with an **empty body** (no `email`) to `POST /api/auth/send-verification-code` is not rejected by validation. The API goes on to try to send an email and fails with **`500 Internal Server Error`**. The error response includes the raw message from the mail server (Gmail SMTP) and a Google support link.

Two problems come from this:

1. A missing `email` should be rejected with `400 Bad Request` and a validation message, like the other endpoints in the API do.
2. Internal infrastructure details should never be returned to the client.

---

## Preconditions

None. The endpoint is public (no token required).

---

## Steps to Reproduce

Send the following request:

```http
POST /api/auth/send-verification-code HTTP/1.1
Host: mytherapy.runasp.net
Content-Type: application/json

{}
```

---

## Expected Result

`400 Bad Request` with a validation error, in the same format the API uses for other missing fields:

```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.1",
  "title": "One or more validation errors occurred.",
  "status": 400,
  "errors": {
    "Email": ["The Email field is required."]
  }
}
```

No email is sent.

---

## Actual Result

`500 Internal Server Error`:

```json
{
  "StatusCode": 500,
  "Message": "The SMTP server has unexpectedly disconnected: 5.7.9 Please log in with your web browser and then try again. For more\n5.7.9 information, go to\n5.7.9  https://support.google.com/mail/?p=WebLoginRequired 5b1f17b1804b1-4a1df370381sm14589105e9.1 - gsmtp"
}
```

---

## Comparison with Other Endpoints

| Request                                           | Status  | Message                                                    |
| ------------------------------------------------- | ------- | ---------------------------------------------------------- |
| `POST /api/auth/register/patient` with `{}`       | 400     | `The Email field is required.` (and other required fields) |
| `POST /api/auth/verify-email` with `{}`           | 400     | `Invalid verification code.`                               |
| `POST /api/auth/send-verification-code` with `{}` | **500** | **SMTP error with Gmail details (bug)**                    |

---

## Impact

- **Information disclosure:** the response reveals that the backend uses Gmail SMTP and exposes the mail server's error text and a session identifier.
- **Wrong status code:** a client error (missing input) is reported as a server error, which makes monitoring and client error handling unreliable.
- **Possible mail service problem (not confirmed):** the Gmail error `5.7.9 ... WebLoginRequired` usually means the sender account needs to be re-authenticated or its app password is not valid. If that is the case, **no verification email can be sent at all**, and because users must verify their email before registering, nobody could register. This was observed on a request with no recipient, so it still needs to be confirmed with a request that has a valid email.

---

## Suggested Fix

1. Add `[Required]` and `[EmailAddress]` validation to the request model so a missing or invalid `email` returns `400` before any email is sent.
2. Catch SMTP errors in the service and return a generic message, for example `Could not send the verification email. Please try again later.`, and log the real error on the server only.
3. Check that the sender account's credentials are valid.

---

## Automated Test

The bug is covered by a test in `EmailVerificationTest.java`. It is **disabled** (`enabled = false`). After the fix, remove `enabled = false` and the test should pass.

```java
// BUG-003: an empty body returns 500 with SMTP details. Expected: 400 validation error.
@Test(groups = {"negative"}, enabled = false)
public void sendVerificationCodeWithEmptyBody()
{
    given()
        .contentType("application/json")
        .body("{}")
    .when()
        .post("/api/auth/send-verification-code")
    .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("errors.Email[0]", equalTo("The Email field is required."));
}
```

**Current result when enabled:** fails with `Expected status code <400> but was <500>`.

---

## Notes

- The `send-verification-code` endpoint was **not** called with a real email address during testing, because it sends a real email.
- This bug is about **email verification** (the code that must be verified before registering). It is unrelated to **therapist verification** (admin approval of a new therapist).
