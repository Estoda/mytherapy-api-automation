package tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import models.AvailabilityRequest;
import models.BookingRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.testng.annotations.Test;

import base.BaseTest;
import utils.AuthManager;
import utils.Role;

public class BookingTest extends BaseTest
{
    @Test(groups = {"smoke"})
    public void getMyBookingsWithPatientToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.PATIENT))
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(200)
            .body("appointmentId", everyItem(not(emptyOrNullString())))
            .body("slotId", everyItem(not(emptyOrNullString())))
            .body("therapistName", everyItem(not(emptyOrNullString())))
            .body("appointmentDatetime", everyItem(not(emptyOrNullString())))
            .body("durationMinutes", everyItem(notNullValue()))
            .body("status", everyItem(not(emptyOrNullString())))
            .body("createdAt", everyItem(not(emptyOrNullString())));
    }

    @Test(groups = {"negative"})
    public void getMyBookingsWithTherapistToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.THERAPIST))
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getMyBookingsWithAdminToken()
    {
        given()
            .auth().oauth2(AuthManager.getToken(Role.ADMIN))
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(403);
    }

    @Test(groups = {"negative"})
    public void getMyBookingsWithoutToken()
    {
        given()
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(401);
    }

    @Test(groups = {"booking-flow"})
    public void createBooking()
    {
        String therapistToken = AuthManager.getToken(Role.THERAPIST);
        String patientToken = AuthManager.getToken(Role.PATIENT);

        // Test slots always end in 59 seconds, so they are easy to find in the database
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.MINUTES).plusSeconds(59);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        // 1. The therapist creates a slot
        String slotId = given()
                .auth().oauth2(therapistToken)
                .contentType("application/json")
                .body(new AvailabilityRequest(start.toString(), end.toString()))
            .when()
                .post("/api/therapist/availability")
            .then()
                .statusCode(200)
                .extract()
                .path("slotId");

        // 2. The patient books it
        String appointmentId = given()
                .auth().oauth2(patientToken)
                .contentType("application/json")
                .body(new BookingRequest(slotId))
            .when()
                .post("/api/patient/bookings")
            .then()
                .statusCode(200)
                .body("appointmentId", not(emptyOrNullString()))
                .body("slotId", equalTo(slotId))
                .body("therapistName", not(emptyOrNullString()))
                .body("durationMinutes", equalTo(60))
                .body("status", equalTo("Scheduled"))
                .extract()
                .path("appointmentId");

        // 3. The booking appears in the patient's list (matched by appointmentId)
        given()
            .auth().oauth2(patientToken)
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(200)
            .body("appointmentId", hasItem(appointmentId));
    }

    // BUG-002: GET /api/patient/bookings/my returns the appointment id in the slotId field.
    // Expected: the real slot id. Note: when enabled, this creates one slot and one booking.
    @Test(groups = {"booking-flow"}, enabled = false)
    public void bookingListReturnsRealSlotId()
    {
        String therapistToken = AuthManager.getToken(Role.THERAPIST);
        String patientToken = AuthManager.getToken(Role.PATIENT);

        Instant start = Instant.now().plus(2, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.MINUTES).plusSeconds(59);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        String slotId = given()
                .auth().oauth2(therapistToken)
                .contentType("application/json")
                .body(new AvailabilityRequest(start.toString(), end.toString()))
            .when()
                .post("/api/therapist/availability")
            .then()
                .statusCode(200)
                .extract()
                .path("slotId");

        String appointmentId = given()
                .auth().oauth2(patientToken)
                .contentType("application/json")
                .body(new BookingRequest(slotId))
            .when()
                .post("/api/patient/bookings")
            .then()
                .statusCode(200)
                .extract()
                .path("appointmentId");

        given()
            .auth().oauth2(patientToken)
        .when()
            .get("/api/patient/bookings/my")
        .then()
            .statusCode(200)
            .body("find { it.appointmentId == '" + appointmentId + "' }.slotId", equalTo(slotId));
    }
}