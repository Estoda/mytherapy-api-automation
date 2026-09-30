package tests;

import base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class AuthTest extends BaseTest {

    private static final String VALID_EMAIL = "programmer82253@gmail.com";
    private static final String VALID_PASSWORD = "admin";

    // 1. Valid Login
    @Test
    public void validLogin() {
        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(VALID_EMAIL, VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", not(emptyOrNullString()))
            .body("expiration", not(emptyOrNullString()));
    }


    // 2. Wrong Password
    @Test
    public void loginWithWrongPassword() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "password": "wrongPassword123"
                    }
                    """.formatted(VALID_EMAIL))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("StatusCode", equalTo(401))
            .body("Message", equalTo("Invalid credentials!"));
    }


    // 3. Non-existing Email
    @Test
    public void loginWithNonExistingEmail() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "nonexisting123456@gmail.com",
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("StatusCode", equalTo(401))
            .body("Message", equalTo("Invalid credentials!"));
    }


    // 4. Empty Email
    @Test
    public void loginWithEmptyEmail() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "",
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .log().body();
    }


    // 5. Empty Password
    @Test
    public void loginWithEmptyPassword() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "%s",
                        "password": ""
                    }
                    """.formatted(VALID_EMAIL))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("StatusCode", equalTo(401))
            .body("Message", equalTo("Invalid credentials!"));
    }


    // 6. Both Fields Empty
    @Test
    public void loginWithBothFieldsEmpty() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "",
                        "password": ""
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .log().body();
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


    // 9. Invalid Email Format
    @Test
    public void loginWithInvalidEmailFormat() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "email": "not-an-email",
                        "password": "%s"
                    }
                    """.formatted(VALID_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("StatusCode", equalTo(401))
            .body("Message", equalTo("Invalid credentials!"));
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
            .log().body();
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
}