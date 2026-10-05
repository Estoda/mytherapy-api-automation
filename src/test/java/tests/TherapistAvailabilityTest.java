package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.AuthManager;
import utils.Role;
import models.AvailabilityRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TherapistAvailabilityTest extends BaseTest
{
    @Test(groups = {"smoke"})
    public void getMyAvailability()
    {
        String token = AuthManager.getToken(Role.THERAPIST);

        given()
            .auth().oauth2(token)
        .when()
            .get("/api/therapist/availability/my")
        .then()
            .statusCode(200)
            .log().body();
    }

    @Test (groups = {"regression"})
    public void createAvailability()
    {
        String token = AuthManager.getToken(Role.THERAPIST);

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        String slotId = given()
                            .auth().oauth2(token)
                            .contentType("application/json")
                            .body(new AvailabilityRequest(start.toString(), end.toString()))
                        .when()
                            .post("/api/therapist/availability")
                        .then()
                            .statusCode(200)
                            .body("slotId", not(emptyOrNullString()))
                            .body("therapistId", not(emptyOrNullString()))
                            .body("isBooked", equalTo(false))
                            .extract()
                            .path("slotId");

        given()
            .auth().oauth2(token)
        .when()
            .delete("/api/therapist/availability/" + slotId)
        .then()
            .statusCode(200);


        given()
            .auth().oauth2(token)
        .when()
            .get("/api/therapist/availability/my")
        .then()
            .log().all()
            .statusCode(200)
            .body("slotId", not(hasItem(slotId)));
    }
}
