# MyTherapy API Automation

Automated API testing project for the **MyTherapy** backend using Java, Rest Assured, TestNG, and Maven.

The goal of this project is to build a professional API automation framework and automate the API test scenarios that were previously designed and executed manually using Postman.

---

# Project Overview

**MyTherapy** is an ASP.NET Core backend application that provides APIs for a therapy platform.

The backend includes functionality for:

- Authentication
- Email Verification
- Patient Registration
- Therapist Registration
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
├── pom.xml
├── testng.xml
├── .gitignore
└── src/test/
    ├── java/
    │   ├── base/
    │   │   └── BaseTest.java
    │   ├── models/
    │   │   ├── LoginRequest.java
    │   │   └── AvailabilityRequest.java
    │   ├── tests/
    │   │   ├── AuthTest.java
    │   │   ├── AvailabilityTest.java
    │   │   └── TherapistAvailabilityTest.java
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
| `tests`  | Test classes                                                                                             |
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

`config.properties` is listed in `.gitignore`, so real credentials are never committed.

---

# Running the Tests

Run all tests (uses `testng.xml`):

```bash
mvn test
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

## Test Groups

| Group        | Meaning                                                                       |
| ------------ | ----------------------------------------------------------------------------- |
| `smoke`      | A few basic checks that show the API is alive (login, read data with a token) |
| `negative`   | Invalid input, missing data, and unauthorized requests                        |
| `regression` | Longer flows that create and clean up data                                    |

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

# Test Coverage

## Authentication (`AuthTest`)

| Scenario                                                                                                                             | Expected result                    |
| ------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------- |
| Valid login                                                                                                                          | 200, token and expiration returned |
| Invalid credentials (data-driven: wrong password, non-existing email, empty email, empty password, both empty, invalid email format) | 401, `Invalid credentials!`        |
| Missing or null email / password                                                                                                     | 400, validation error              |
| Wrong data type for email / password                                                                                                 | 400, JSON conversion error         |

## Patient Availability (`AvailabilityTest`)

| Scenario                              | Expected result                         |
| ------------------------------------- | --------------------------------------- |
| Get availability with a patient token | 200, list of slots with required fields |
| Get availability without a token      | 401                                     |

## Therapist Availability (`TherapistAvailabilityTest`)

| Scenario                                                       | Expected result                                 |
| -------------------------------------------------------------- | ----------------------------------------------- |
| Get the therapist's own slots                                  | 200                                             |
| Create a slot, delete it, then verify it is gone from the list | 200 on create and delete, slot no longer listed |

---

# Known API Behaviors (per requirements)

| Scenario                                        | Status | Error body                              |
| ----------------------------------------------- | ------ | --------------------------------------- |
| Wrong password / non-existing email             | 401    | `StatusCode`, `Message`                 |
| Empty email or password, invalid email format   | 401    | `StatusCode`, `Message`                 |
| Missing or null field                           | 400    | `status`, `errors` (ASP.NET validation) |
| Wrong data type (e.g. number instead of string) | 400    | `status`, `errors` (ASP.NET validation) |

---

# API Endpoints

## Authentication

```text
POST /api/auth/register/patient
POST /api/auth/register/therapist
POST /api/auth/login
POST /api/auth/send-verification-code
POST /api/auth/verify-email
```

## Admin Therapists

```text
GET  /api/admin/therapists/pending
POST /api/admin/therapists/{id}/approve
POST /api/admin/therapists/{id}/reject
```

## Patient Availability

```text
GET /api/patient/availability
```

## Patient Booking

```text
POST /api/patient/bookings
GET  /api/patient/bookings/my
```

## Payment

```text
POST /api/payment/initiate
POST /api/payment/webhook
```

## Profile

```text
GET  /api/profile/verification-status
POST /api/profile/upload-picture
POST /api/profile/upload-license
GET  /api/profile/earnings
```

## Reviews

```text
POST /api/reviews
GET  /api/reviews/therapist/{therapistId}
```

## Sessions

```text
POST /api/sessions/{sessionId}/upload-recording
GET  /api/sessions/{sessionId}/analysis-status
GET  /api/sessions/by-appointment/{appointmentId}
```

## Therapist Availability

```text
GET    /api/therapist/availability/my
POST   /api/therapist/availability
DELETE /api/therapist/availability/{id}
```

## Users

```text
GET /api/users/patients
GET /api/users/therapists
GET /api/users/patients/{id}
GET /api/users/therapists/{id}
```

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
- [x] Patient availability tests
- [x] Therapist availability tests (create, delete, verify)

## Planned

- [ ] Wrong-role token tests (for example, a therapist token on a patient endpoint)
- [ ] Tests for the remaining endpoints (bookings, payments, profile, reviews, sessions, admin, users)
- [ ] Test reports (Allure or ExtentReports)
- [ ] GitHub Actions workflow that runs `mvn test` on every push
