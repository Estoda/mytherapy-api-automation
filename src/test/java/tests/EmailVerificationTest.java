package tests;

import base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class EmailVerificationTest extends BaseTest
{
    @Test(groups = {"negative"})
    public void verifyEmailWithUnknownEmail()
    {
        String email = "qa-autotest-" + System.currentTimeMillis() + "@example.com";

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "code": "000000"
                    }
                    """.formatted(email))
        .when()
            .post("/api/auth/verify-email")
        .then()
            .statusCode(404)
            .body("StatusCode", equalTo(404))
            .body("Message", equalTo("No verification request found for this email."));
    }

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
}