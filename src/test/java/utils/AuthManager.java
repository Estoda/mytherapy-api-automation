package utils;

import static io.restassured.RestAssured.given;

public class AuthManager
{
    private static final String EMAIL = "programmer82253@gmail.com";
    private static final String PASSWORD = "admin";

    public static String getToken()
    {
        String requestBody = 
        """
                {
                    "email": "%s",
                    "password": "%s"
                }
        """.formatted(EMAIL, PASSWORD);
        return given()
                    .contentType("application/json")
                    .body(requestBody)
                    .log().all()
                .when()
                    .post("/api/auth/login")
                .then()
                    .log().all()
                    .statusCode(200)
                    .extract()
                    .path("token");
    }
}