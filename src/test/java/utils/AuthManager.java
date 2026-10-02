package utils;

import static io.restassured.RestAssured.given;

import models.LoginRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager
{
    private static final Map<Role, String> tokens = new ConcurrentHashMap<>();

    public static String getToken(Role role)
    {
        return tokens.computeIfAbsent(role, AuthManager::login);
    }

    private static String login(Role role)
    {
        LoginRequest request = new LoginRequest(Config.email(role), Config.password(role));

        return given()
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/auth/login")
                .then()
                    .statusCode(200)
                    .extract()
                    .path("token");
    }
}