package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.AuthManager;
import utils.Role;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.DataProvider;
import static io.restassured.RestAssured.*;

public class AdminTherapistsTest extends BaseTest
{
    @Test(groups = {"smoke"})
    public void getPendingTherapistsWithAdminToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .get("/api/admin/therapists/pending")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test(groups = {"negative"})
    public void getPendingTherapistsWithPatientToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .get("/api/admin/therapists/pending")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getPendingTherapistsWithTherapistToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .get("/api/admin/therapists/pending")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getPendingTherapistsWithoutToken()
    {
        given()
        .when()
            .get("/api/admin/therapists/pending")
        .then()
            .statusCode(401);
    }

    private static final String UNKNOWN_THERAPIST_ID = "00000000-0000-0000-0000-000000000000";

    @DataProvider(name = "adminActions")
    public Object[][] adminActions()
    {
        return new Object[][] { {"approve"}, {"reject"} };
    }

    @Test(dataProvider = "adminActions", groups = {"negative"})
    public void adminActionOnUnknownTherapist(String action)
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .post("/api/admin/therapists/" + UNKNOWN_THERAPIST_ID + "/" + action)
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("title", equalTo("Not Found"));
    }

    @Test(dataProvider = "adminActions", groups = {"negative"})
    public void adminActionWithPatientToken(String action)
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .post("/api/admin/therapists/" + UNKNOWN_THERAPIST_ID + "/" + action)
        .then()
            .statusCode(403);
    }

    @Test(dataProvider = "adminActions", groups = {"negative"})
    public void adminActionWithTherapistToken(String action)
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .post("/api/admin/therapists/" + UNKNOWN_THERAPIST_ID + "/" + action)
        .then()
            .statusCode(403);
    }

    @Test(dataProvider = "adminActions", groups = {"negative"})
    public void adminActionWithoutToken(String action)
    {
        given()
        .when()
            .post("/api/admin/therapists/" + UNKNOWN_THERAPIST_ID + "/" + action)
        .then()
            .statusCode(401);
    }
}