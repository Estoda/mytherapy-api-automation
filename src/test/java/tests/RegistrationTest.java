package tests;

import static org.hamcrest.Matchers.*;
import base.BaseTest;
import org.testng.annotations.Test;
import models.RegisterPatientRequest;
import models.RegisterTherapistRequest;

import static io.restassured.RestAssured.*;

public class RegistrationTest extends BaseTest
{
    @Test(groups = {"negative"})
    public void registerPatientWithEmptyBody()
    {
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/auth/register/patient")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Email[0]", equalTo("The Email field is required."))
            .body("errors.FullName[0]", equalTo("The FullName field is required."))
            .body("errors.Password[0]", equalTo("The Password field is required."));
    }

    @Test(groups = {"negative"})
    public void registerTherapistWithEmptyBody()
    {
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/auth/register/therapist")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Email[0]", equalTo("The Email field is required."))
            .body("errors.FullName[0]", equalTo("The FullName field is required."))
            .body("errors.Password[0]", equalTo("The Password field is required."))
            .body("errors.LicenseNumber[0]", equalTo("The LicenseNumber field is required."))
            .body("errors.Specialization[0]", equalTo("The Specialization field is required."));
    }

    @Test(groups = {"negative"})
    public void registerPatientWithUnverifiedEmail()
    {
        // Made-up address on example.com: nobody owns it, and nothing is created
        String email = "qa-autotest-" + System.currentTimeMillis() + "@example.com";

        given()
            .contentType("application/json")
            .body(new RegisterPatientRequest("QA Autotest", email, "Test@12345",
                    "01000000000", 1, "2000-01-01T00:00:00Z", true))
        .when()
            .post("/api/auth/register/patient")
        .then()
            .statusCode(400)
            .body("StatusCode", equalTo(400))
            .body("Message", equalTo("Email has not been verified. Please verify your email first."));
    }

    @Test(groups = {"negative"})
    public void registerTherapistWithUnverifiedEmail()
    {
        // Made-up address on example.com: nobody owns it, and nothing should be created
        String email = "qa-autotest-" + System.currentTimeMillis() + "@example.com";

        given()
            .contentType("application/json")
            .body(new RegisterTherapistRequest("QA Autotest", email, "Test@12345",
                    "01000000000", 1, "2000-01-01T00:00:00Z", true,
                    "Psychology", "QA-0000"))
        .when()
            .post("/api/auth/register/therapist")
        .then()
            .statusCode(400)
            .body("StatusCode", equalTo(400))
            .body("Message", equalTo("Email has not been verified. Please verify your email first."));
    }
}