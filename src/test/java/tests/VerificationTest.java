package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.*;

public class VerificationTest extends BaseTest
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

    @Test(groups = {"negative"})
    public void verifyEmailWithEmptyBody()
    {
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/auth/verify-email")
        .then()
            .statusCode(400)
            .body("StatusCode", equalTo(400))
            .body("Message", equalTo("Invalid verification code."));
    }
}