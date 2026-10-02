package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import utils.Config;
import utils.Role;
import models.LoginRequest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class AuthTest extends BaseTest {

    private static final String VALID_EMAIL = Config.email(Role.PATIENT);
    private static final String VALID_PASSWORD = Config.password(Role.PATIENT);

    // 1. Valid Login
    @Test
    public void validLogin() {
        given()
            .contentType("application/json")
            .body(new LoginRequest(VALID_EMAIL, VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", not(emptyOrNullString()))
            .body("expiration", not(emptyOrNullString()));
    }

    // 7. Missing Email
    @Test
    public void loginWithMissingEmail() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Email[0]", equalTo("The Email field is required."));
    }


    // 8. Missing Password
    @Test
    public void loginWithMissingPassword() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s"
                    }
                    """.formatted(VALID_EMAIL))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Password[0]", equalTo("The Password field is required."));
    }

    // 10. Null Email
    @Test
    public void loginWithNullEmail() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": null,
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Email[0]", equalTo("The Email field is required."));
    }


    // 11. Null Password
    @Test
    public void loginWithNullPassword() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "password": null
                    }
                    """.formatted(VALID_EMAIL))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.Password[0]", equalTo("The Password field is required."));
    }


    // 12. Wrong Email Data Type
    @Test
    public void loginWithWrongEmailDataType() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": 12345,
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.request[0]", equalTo("The request field is required."))
            .body(
                "errors['$.email'][0]",
                containsString("The JSON value could not be converted to System.String")
            );
    }


    // 13. Wrong Password Data Type
    @Test
    public void loginWithWrongPasswordDataType() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "password": 12345
                    }
                    """.formatted(VALID_EMAIL))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.request[0]", equalTo("The request field is required."))
            .body(
                "errors['$.password'][0]",
                containsString("The JSON value could not be converted to System.String")
            );
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials()
    {
        return new Object[][]
        {
            {"wrong password",      VALID_EMAIL,               "wrongPassword123"},
            {"non-existing email",  "nonexisting123456@gmail.com", VALID_PASSWORD},
            {"empty email",         "",                            VALID_PASSWORD},
            {"empty password",      VALID_EMAIL,                               ""},
            {"both fields empty",                    "",                       ""},
            {"invalid email format",           "not-an-email",     VALID_PASSWORD},
        };
    }

    @Test(dataProvider = "invalidCredentials")
    public void loginWithInvalidCredentials(String scenario, String email, String password)
    {
        given()
            .contentType("application/json")
            .body(new LoginRequest(email, password))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("StatusCode", equalTo(401))
            .body("Message", equalTo("Invalid credentials!"));
    }
}