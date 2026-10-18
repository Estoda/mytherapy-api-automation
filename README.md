# MyTherapy API Automation

[![API Tests](https://github.com/Estoda/mytherapy-api-automation/actions/workflows/tests.yml/badge.svg)](https://github.com/Estoda/mytherapy-api-automation/actions/workflows/tests.yml)

Automated API testing project for the **MyTherapy** backend using Java, Rest Assured, TestNG, and Maven.

The goal of this project is to build a professional API automation framework and automate the API test scenarios that were previously designed and executed manually using Postman.

The project is written from the point of view of a QA engineer testing another developer's work: the expected behavior comes from the project requirements, and anything that breaks them is documented as a bug report in the [`bug-reports`](bug-reports) folder.

---

# Project Overview

**MyTherapy** is an ASP.NET Core backend application that provides APIs for a therapy platform.

The backend includes functionality for:

- Authentication
- Email Verification
- Patient Registration
- Therapist Registration
- Therapist Verification (admin approval)
- Therapist Availability
- Patient Booking
- Payments
- Profiles
- Reviews
- Sessions
- Admin Therapist Management
- Users Management

This project focuses on testing these APIs automatically.

---

# Project Goals

1. Learn API automation using Rest Assured.
2. Build a clean and maintainable API automation framework.
3. Automate the API test cases previously created in Postman.
4. Practice positive and negative API testing.
5. Practice API authentication using JWT.
6. Validate HTTP status codes and response bodies.
7. Validate JSON response fields.
8. Handle reusable test data and request models.
9. Use TestNG for test execution.
10. Use Maven for dependency management and test execution.
11. Generate useful test reports.
12. Create a professional QA portfolio project suitable for Junior QA applications.

---

# Tech Stack

- Java 17
- Rest Assured
- TestNG
- Maven (with Maven Surefire Plugin)
- Jackson (JSON serialization)
- Git / GitHub
- GitHub Actions (CI)

---

# Environment

Base URL:

```text
http://mytherapy.runasp.net
```

Swagger:

```text
http://mytherapy.runasp.net/swagger/index.html
```

Authentication:

```text
JWT Bearer Token
```

---

# Project Structure

```text
MyTherapyAPI-Automation/
├── .github/
│   └── workflows/
│       └── tests.yml
├── bug-reports/
│   ├── BUG-001-non-admin-can-access-patients-list.md
│   ├── BUG-002-my-bookings-returns-appointment-id-as-slot-id.md
│   └── BUG-003-send-verification-code-returns-500-with-smtp-details.md
├── pom.xml
├── testng.xml
├── .gitignore
└── src/test/
    ├── java/
    │   ├── base/
    │   │   └── BaseTest.java
    │   ├── models/
    │   │   ├── LoginRequest.java
    │   │   ├── AvailabilityRequest.java
    │   │   ├── BookingRequest.java
    │   │   ├── RegisterPatientRequest.java
    │   │   └── RegisterTherapistRequest.java
    │   ├── tests/
    │   │   ├── AuthTest.java
    │   │   ├── RegistrationTest.java
    │   │   ├── EmailVerificationTest.java
    │   │   ├── AdminTherapistsTest.java
    │   │   ├── AvailabilityTest.java
    │   │   ├── TherapistAvailabilityTest.java
    │   │   ├── BookingTest.java
    │   │   ├── ProfileTest.java
    │   │   └── UsersTest.java
    │   └── utils/
    │       ├── AuthManager.java
    │       ├── Config.java
    │       └── Role.java
    └── resources/
        ├── config.properties            (local only, not committed)
        └── config.properties.example
```

| Package  | Purpose                                                                                                  |
| -------- | -------------------------------------------------------------------------------------------------------- |
| `base`   | `BaseTest` sets the base URL and enables request/response logging when a test fails                      |
| `models` | Request body classes (POJOs) serialized to JSON by Jackson                                               |
| `tests`  | Test classes, one per feature area                                                                       |
| `utils`  | `Config` (reads settings), `Role` (Patient / Therapist / Admin), `AuthManager` (login and token caching) |

---

# Getting Started

## Prerequisites

- Java 17
- Maven
- Git

## Setup

1. Clone the repository.
2. Copy `src/test/resources/config.properties.example` to `src/test/resources/config.properties`.
3. Fill in the base URL and the credentials for each role:

```properties
baseUrl=http://your-api-url
patient.email=your-email
patient.password=your-password
therapist.email=your-therapist-email
therapist.password=your-therapist-password
admin.email=your-admin-email
admin.password=your-admin-password
```

The values must not have trailing spaces or other characters, because they are used exactly as written.

`config.properties` is listed in `.gitignore`, so real credentials are never committed.

---

# Running the Tests

Run all tests (uses `testng.xml`):

```bash
mvn test
```

Run all tests except the ones that create data (this is what CI runs):

```bash
mvn test -DexcludedGroups=booking-flow
```

Run one group:

```bash
mvn test -Dgroups=smoke
mvn test -Dgroups=negative
```

Run one test class:

```bash
mvn test -Dtest=AuthTest
```

Run one test method:

```bash
mvn test -Dtest=AuthTest#validLogin
```

If the tests fail with a `class file version` error after changing the Java version, rebuild from scratch with `mvn clean test`.

**Windows PowerShell:** put each `-D` argument in quotes, especially when it contains a comma:

```powershell
mvn clean test "-Dtest=EmailVerificationTest,BookingTest" "-DexcludedGroups=booking-flow"
```

## Test Groups

| Group          | Meaning                                                                              |
| -------------- | ------------------------------------------------------------------------------------ |
| `smoke`        | A few basic checks that show the API is alive (login, read data with a token)        |
| `negative`     | Invalid input, missing data, and unauthorized or forbidden requests                  |
| `regression`   | Longer flows that create and clean up data                                           |
| `booking-flow` | Flows that create data that cannot be removed through the API; excluded from CI runs |

Tests that are disabled with `enabled = false` document a known bug. Each one has a comment with the bug number, and it should be enabled again when the bug is fixed.

---

# Continuous Integration

A GitHub Actions workflow (`.github/workflows/tests.yml`) runs the test suite on every push and pull request to `main`.

The workflow:

1. Checks out the code.
2. Sets up Java 17.
3. Creates `src/test/resources/config.properties` from GitHub Secrets.
4. Runs `mvn test -DexcludedGroups=booking-flow`.

The `booking-flow` group is excluded because the API has no endpoint to cancel a booking, so every run of those tests would leave a new booking on the shared server.

Because `config.properties` is not committed, the credentials are stored as repository secrets (**Settings → Secrets and variables → Actions**):

| Secret                                  | Used for          |
| --------------------------------------- | ----------------- |
| `BASE_URL`                              | API base URL      |
| `PATIENT_EMAIL`, `PATIENT_PASSWORD`     | Patient account   |
| `THERAPIST_EMAIL`, `THERAPIST_PASSWORD` | Therapist account |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`         | Admin account     |

---

# Authentication

The API uses JWT authentication.

There are different user roles:

- Patient
- Therapist
- Admin

`AuthManager.getToken(Role role)` logs in with the credentials of the requested role and **caches the token**, so each role logs in only once per test run.

```text
Test asks for a token (Role)
        ↓
Token already stored?  → yes → reuse it
        ↓ no
Login with that role's credentials
        ↓
Store token
        ↓
Use token in protected requests
```

---

# Two Kinds of Verification

The platform has two different verification flows, and the tests keep them apart:

|                  | Email verification                                                             | Therapist verification                                                                                                                                           |
| ---------------- | ------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **What it is**   | A code sent to the user's inbox                                                | An admin approves a newly registered therapist                                                                                                                   |
| **Who**          | Patients and therapists                                                        | Therapists (approved by an admin)                                                                                                                                |
| **When**         | Before registering. A user cannot register with an email that is not verified. | After registering. A new therapist is **pending** until an admin approves them.                                                                                  |
| **Endpoints**    | `POST /api/auth/send-verification-code`, `POST /api/auth/verify-email`         | `GET /api/admin/therapists/pending`, `POST /api/admin/therapists/{id}/approve`, `POST /api/admin/therapists/{id}/reject`, `GET /api/profile/verification-status` |
| **Test classes** | `EmailVerificationTest`, `RegistrationTest`                                    | `AdminTherapistsTest`, `ProfileTest`                                                                                                                             |

Therapist verification status values: `Pending = 1`, `Approved = 2`, `Rejected = 3`.

---

# Test Coverage

## Authentication (`AuthTest`)

| Scenario                                                                                                                             | Expected result                    |
| ------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------- |
| Valid login                                                                                                                          | 200, token and expiration returned |
| Invalid credentials (data-driven: wrong password, non-existing email, empty email, empty password, both empty, invalid email format) | 401, `Invalid credentials!`        |
| Missing or null email / password                                                                                                     | 400, validation error              |
| Wrong data type for email / password                                                                                                 | 400, JSON conversion error         |

## Registration (`RegistrationTest`)

| Scenario                                                | Expected result                                                     |
| ------------------------------------------------------- | ------------------------------------------------------------------- |
| Register a patient with an empty body                   | 400, `Email`, `FullName`, `Password` are required                   |
| Register a therapist with an empty body                 | 400, the same fields plus `LicenseNumber` and `Specialization`      |
| Register a patient with an email that is not verified   | 400, `Email has not been verified. Please verify your email first.` |
| Register a therapist with an email that is not verified | 400, the same message                                               |

The API checks that the email is verified **before** it validates the other fields, so rules such as password strength or date of birth cannot be tested without a verified email. The tests use made-up `@example.com` addresses, so no account is created.

## Email Verification (`EmailVerificationTest`)

| Scenario                                         | Expected result                                      |
| ------------------------------------------------ | ---------------------------------------------------- |
| Verify an email that has no verification request | 404, `No verification request found for this email.` |
| Send a verification code with an empty body      | Disabled, see BUG-003                                |

`send-verification-code` sends a real email, so it is not called with an address in the automated tests.

## Admin Therapists (`AdminTherapistsTest`)

| Scenario                                                  | Expected result |
| --------------------------------------------------------- | --------------- |
| Get the pending therapists list as an admin               | 200, a list     |
| Get the pending list as a patient or a therapist          | 403             |
| Get the pending list without a token                      | 401             |
| Approve or reject a therapist that does not exist (admin) | 404             |
| Approve or reject as a patient or a therapist             | 403             |
| Approve or reject without a token                         | 401             |

The `approve` and `reject` tests use a non-existing id on purpose, so no real therapist is changed. They are data-driven: the same tests run for both actions.

## Patient Availability (`AvailabilityTest`)

| Scenario                                             | Expected result                         |
| ---------------------------------------------------- | --------------------------------------- |
| Get availability with a patient token                | 200, list of slots with required fields |
| Get availability without a token                     | 401                                     |
| Get availability with a therapist token (wrong role) | 403                                     |

## Therapist Availability (`TherapistAvailabilityTest`)

| Scenario                                                       | Expected result                                 |
| -------------------------------------------------------------- | ----------------------------------------------- |
| Get the therapist's own slots                                  | 200                                             |
| Create a slot, delete it, then verify it is gone from the list | 200 on create and delete, slot no longer listed |

## Booking (`BookingTest`)

| Scenario                                                                               | Expected result                                |
| -------------------------------------------------------------------------------------- | ---------------------------------------------- |
| Get my bookings as a patient                                                           | 200, list of bookings with required fields     |
| Get my bookings as a therapist or an admin                                             | 403                                            |
| Get my bookings without a token                                                        | 401                                            |
| Full flow: therapist creates a slot, patient books it, the booking appears in the list | 200, status `Scheduled` (group `booking-flow`) |
| The booking list returns the real slot id                                              | Disabled, see BUG-002                          |

## Profile (`ProfileTest`)

| Scenario                                 | Expected result                          |
| ---------------------------------------- | ---------------------------------------- |
| Get verification status as a therapist   | 200, `verificationStatus` present        |
| Get earnings as a therapist              | 200, totals and `recentPayments` present |
| Either endpoint as a patient or an admin | 403                                      |
| Either endpoint without a token          | 401                                      |

## Users (`UsersTest`)

| Scenario                                                     | Expected result                          |
| ------------------------------------------------------------ | ---------------------------------------- |
| Get the therapists list (admin, patient, therapist)          | 200, open to all roles by design         |
| Get a therapist by id (admin, patient, therapist)            | 200, profile fields present              |
| Get the therapists list or a therapist by id without a token | 401                                      |
| Get a therapist by an unknown id                             | 404, `Therapist not found with id: <id>` |
| Get a therapist by an invalid id format                      | 400, validation error                    |
| Get the patients list as an admin                            | 200                                      |
| Get the patients list without a token                        | 401                                      |
| Get the patients list as a patient or a therapist            | Disabled, see BUG-001 (should be 403)    |
| Get a patient by id as an admin                              | 200                                      |
| Get a patient by id without a token                          | 401                                      |
| Get a patient by an unknown id                               | 404, `Patient not found with id: <id>`   |
| Get a patient by an invalid id format                        | 400, validation error                    |

---

# Bug Reports

Bugs found by the automated tests are written up in the [`bug-reports`](bug-reports) folder, with steps to reproduce, expected and actual results, impact, a suggested fix, and the disabled test that proves each bug.

| ID                                                                                     | Title                                                                                                | Severity |
| -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | -------- |
| [BUG-001](bug-reports/BUG-001-non-admin-can-access-patients-list.md)                   | Non-admin users can access `GET /api/users/patients` (should be admin only)                          | High     |
| [BUG-002](bug-reports/BUG-002-my-bookings-returns-appointment-id-as-slot-id.md)        | `GET /api/patient/bookings/my` returns the appointment id in the `slotId` field                      | Medium   |
| [BUG-003](bug-reports/BUG-003-send-verification-code-returns-500-with-smtp-details.md) | `POST /api/auth/send-verification-code` returns 500 and leaks SMTP details when the email is missing | High     |

---

# Known API Behaviors (per requirements)

| Scenario                                             | Status | Error body                                   |
| ---------------------------------------------------- | ------ | -------------------------------------------- |
| Wrong password / non-existing email                  | 401    | `StatusCode`, `Message`                      |
| Empty email or password, invalid email format        | 401    | `StatusCode`, `Message`                      |
| Missing or null field                                | 400    | `status`, `errors` (ASP.NET validation)      |
| Wrong data type (e.g. number instead of string)      | 400    | `status`, `errors` (ASP.NET validation)      |
| No token on a protected endpoint                     | 401    | empty body                                   |
| Valid token with the wrong role                      | 403    | empty body                                   |
| Register with an email that is not verified          | 400    | `StatusCode`, `Message`                      |
| Verify an email with no verification request         | 404    | `StatusCode`, `Message`                      |
| Therapist or patient not found by id                 | 404    | plain text message                           |
| Admin approve or reject with an unknown therapist id | 404    | ASP.NET `ProblemDetails` (`status`, `title`) |
| Invalid id format in the URL                         | 400    | `status`, `errors` (ASP.NET validation)      |

---

# Test Data

Some tests create data on the shared test server. To make it easy to find and clean up, the tests mark what they create:

- **Slots** created by the booking tests start at a time that ends in `:59` seconds.
- **Email addresses** used by the registration tests start with `qa-autotest-` and use the `@example.com` domain, which nobody owns.

The API has no endpoint to cancel a booking, so the `booking-flow` group is excluded from CI, and bookings created by local runs are removed from the database from time to time.

---

# API Endpoints and Automation Status

| Endpoint                                           | Status                                                    |
| -------------------------------------------------- | --------------------------------------------------------- |
| `POST /api/auth/login`                             | Automated                                                 |
| `POST /api/auth/register/patient`                  | Partly (empty body, unverified email)                     |
| `POST /api/auth/register/therapist`                | Partly (empty body, unverified email)                     |
| `POST /api/auth/send-verification-code`            | Not automated (sends a real email). BUG-003 test disabled |
| `POST /api/auth/verify-email`                      | Partly (unknown email)                                    |
| `GET /api/admin/therapists/pending`                | Automated                                                 |
| `POST /api/admin/therapists/{id}/approve`          | Partly (unknown id, roles, no token)                      |
| `POST /api/admin/therapists/{id}/reject`           | Partly (unknown id, roles, no token)                      |
| `GET /api/patient/availability`                    | Automated                                                 |
| `POST /api/patient/bookings`                       | Automated (group `booking-flow`)                          |
| `GET /api/patient/bookings/my`                     | Automated. BUG-002 test disabled                          |
| `POST /api/payment/initiate`                       | Not yet                                                   |
| `POST /api/payment/webhook`                        | Not yet                                                   |
| `GET /api/profile/verification-status`             | Automated                                                 |
| `POST /api/profile/upload-picture`                 | Not yet                                                   |
| `POST /api/profile/upload-license`                 | Not yet                                                   |
| `GET /api/profile/earnings`                        | Automated                                                 |
| `POST /api/reviews`                                | Not yet                                                   |
| `GET /api/reviews/therapist/{therapistId}`         | Not yet                                                   |
| `POST /api/sessions/{sessionId}/upload-recording`  | Not yet                                                   |
| `GET /api/sessions/{sessionId}/analysis-status`    | Not yet                                                   |
| `GET /api/sessions/by-appointment/{appointmentId}` | Not yet                                                   |
| `GET /api/therapist/availability/my`               | Automated                                                 |
| `POST /api/therapist/availability`                 | Automated                                                 |
| `DELETE /api/therapist/availability/{id}`          | Automated                                                 |
| `GET /api/users/patients`                          | Automated. BUG-001 tests disabled                         |
| `GET /api/users/therapists`                        | Automated                                                 |
| `GET /api/users/patients/{id}`                     | Automated (admin, no token, unknown id, invalid id)       |
| `GET /api/users/therapists/{id}`                   | Automated                                                 |

---

# Progress

## Done

- [x] Project setup with Maven, Rest Assured, TestNG, and Jackson
- [x] Configuration kept out of the code (`config.properties`)
- [x] Role-based authentication with token caching
- [x] Request models (POJOs) with Jackson serialization
- [x] Data-driven tests with TestNG `@DataProvider`
- [x] Suite file (`testng.xml`) and test groups
- [x] Authentication tests
- [x] Registration and email verification tests (the cases that do not need a verified email)
- [x] Admin therapist management tests (read and negative cases)
- [x] Patient and therapist availability tests (create, delete, verify)
- [x] Booking tests, including the full booking flow
- [x] Profile tests (verification status and earnings)
- [x] Users tests (lists and by-id, all roles)
- [x] Wrong-role and no-token tests for the role-restricted endpoints
- [x] Bug reports for the problems found (3 so far)
- [x] GitHub Actions workflow that runs the tests on every push

## Planned

- [ ] Confirm the access rule for `GET /api/users/patients/{id}`
- [ ] Positive approve and reject tests (they need a pending therapist)
- [ ] Registration tests for the rules that need a verified email (password strength, date of birth, duplicate email)
- [ ] Payment, reviews, and sessions endpoints
- [ ] Profile upload endpoints (picture and license)
- [ ] Test reports (Allure or ExtentReports)
