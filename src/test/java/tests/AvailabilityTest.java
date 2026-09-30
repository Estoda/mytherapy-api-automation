package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.AuthManager;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class AvailabilityTest extends BaseTest {
    @Test 
    public void getPatientAvailability()
    {
        String token = AuthManager.getToken();
        given()
            .auth().oauth2(token)
        .when()
            .get("/api/patient/availability")
        .then()
            .statusCode(200)
            .log().body();
    }    
}
