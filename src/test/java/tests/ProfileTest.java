package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.AuthManager;
import utils.Role;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.*;

public class ProfileTest extends BaseTest
{
    @Test(groups = {"smoke"})
    public void getVerificationStatusWithTherapistToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .get("/api/profile/verification-status")
        .then()
            .statusCode(200)
            .body("verificationStatus", notNullValue());
    }

    @Test(groups = {"negative"})
    public void getVerificationStatusWithPatientToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .get("/api/profile/verification-status")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getVerificationStatusWithAdminToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .get("/api/profile/verification-status")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getVerificationStatusWithoutToken()
    {
        given()
        .when()
            .get("/api/profile/verification-status")
        .then()
            .statusCode(401);
    }

    @Test(groups = {"smoke"})
    public void getEarningsWithTherapistToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .get("/api/profile/earnings")
        .then()
            .statusCode(200)
            .body("todayEarnings", notNullValue())
            .body("thisMonthEarnings", notNullValue())
            .body("totalEarnings", notNullValue())
            .body("recentPayments", notNullValue());
    }

    @Test(groups = {"negative"})
    public void getEarningsWithPatientToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .get("/api/profile/earnings")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getEarningsWithAdminToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .get("/api/profile/earnings")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getEarningsWithoutToken()
    {
        given()
        .when()
            .get("/api/profile/earnings")
        .then()
            .statusCode(401);
    }
}