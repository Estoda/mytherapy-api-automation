package tests;

import base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import tests.AuthTest.token;

public class AvailabilityTest extends BaseTest {
    @Test 
    public void getPatientAvailability()
    {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/patient/availability")
        .then()
            .statusCode(200);
    }    
}
