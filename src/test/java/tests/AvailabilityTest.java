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
            .body("size()", greaterThan(0))
            .body("slotId", everyItem(not(emptyOrNullString())))
            .body("therapistId", everyItem(not(emptyOrNullString())))
            .body("therapistName", everyItem(not(emptyOrNullString())))
            .body("startTime", everyItem(not(emptyOrNullString())))
            .body("endTime", everyItem(not(emptyOrNullString())))
            .body("isBooked", everyItem(notNullValue()));
    }   
    
    @Test
    public void getPatientAvailabilityWithoutToken()
    {
        given()
        .when()
            .get("/api/patient/availability")
        .then()
            .statusCode(401);
    }
}
