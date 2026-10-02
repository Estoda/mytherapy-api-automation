package utils;

import static io.restassured.RestAssured.given;

public class AuthManager
{
    public static String getToken()
    {
        String requestBody = 
        """
                {
                    "email": "%s",
                    "password": "%s"
                }
        """.formatted(Config.patientEmail(), Config.patientPassword());
        return given()
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/auth/login")
                .then()
                    .statusCode(200)
                    .extract()
                    .path("token");
    }
}