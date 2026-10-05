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

The main goals of this project are:

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

- Java
- Rest Assured
- TestNG
- Maven
- JSON
- Jackson / JSON serialization
- Git
- GitHub

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

```test
JWT Bearer Token
```

---

# Authentication

The API uses JWT authentication.

There are different user roles:

- Patient
- Therapist
- Admin

The automation framework will handle authentication and reuse the generated tokens when testing protected endpoints.

Expected flow:

```test
Login
   ↓
Receive JWT Token
   ↓
Store Token
   ↓
Use Token in Protected Requests
```

Separate credentials/tokens should be maintained for different roles when required.

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

---

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

# Known API Behaviors (per requirements)

| Scenario                                        | Status | Error body                              |
| ----------------------------------------------- | ------ | --------------------------------------- |
| Wrong password / non-existing email             | 401    | `StatusCode`, `Message`                 |
| Empty email or password, invalid email format   | 401    | `StatusCode`, `Message`                 |
| Missing or null field                           | 400    | `status`, `errors` (ASP.NET validation) |
| Wrong data type (e.g. number instead of string) | 400    | `status`, `errors` (ASP.NET validation) |
