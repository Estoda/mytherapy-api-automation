package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.AuthManager;
import utils.Role;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UsersTest extends BaseTest
{
    @Test(groups = {"smoke"})
    public void getTherapists()
    {
        String token = AuthManager.getToken(Role.ADMIN);

        given()
            .auth().oauth2(token)
        .when()
            .get("/api/users/therapists")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("id", everyItem(not(emptyOrNullString())))
            .body("fullName", everyItem(not(emptyOrNullString())));
    }

    @Test(groups = {"negative"})
    public void probeTherapistsWithoutToken()
    {
        given()
        .when()
            .get("/api/users/therapists")
        .then()
            .statusCode(401);
    }

    // BUG-001 (see bug-reports/BUG-001-therapist-can-access-therapists-list.md).
    // Expected: 403 Forbidden. Remove "enable = false" once the bug is fixed.
    @Test(groups = {"negative"}, enabled = false)
    public void probeTherapistsWithTherapistToken()
    {
        String token = AuthManager.getToken(Role.THERAPIST);

        given()
            .auth().oauth2(token)
        .when()
            .get("/api/users/therapists")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"smoke"})
    public void getPatients()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .get("/api/users/patients")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("id", everyItem(not(emptyOrNullString())))
            .body("fullName", everyItem(not(emptyOrNullString())));
    }

    // BUG-002: a patient can currently access this endpoint (returns 200). Expected: 403.
    @Test(groups = {"negative"}, enabled = false)
    public void getPatientsWithPatientToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .get("/api/users/patients")
        .then()
            .statusCode(403);
    }

    // BUG-002: a therapist can currently access this endpoint (returns 200). Expected: 403.
    @Test(groups = {"negative"}, enabled = false)
    public void getPatientsWithTherapistToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .get("/api/users/patients")
        .then()
            .statusCode(403);
    }
}
