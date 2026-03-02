package com.oceanview.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oceanview.service.ReportService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportServiceIntegrationTest {

    @BeforeAll
    static void beforeAll() {
        DatabaseTestSupport.assumeDatabaseReady();
    }

    @Test
    public void dailySummary_containsTodayCheckinsAndCheckouts() throws Exception {
        int guestId = insertGuest("RPT-D" + System.currentTimeMillis());
        int checkInReservationId = 0;
        int checkOutReservationId = 0;

        String checkInNumber = shortReservationNumber("RIN");
        String checkOutNumber = shortReservationNumber("ROUT");
        LocalDate today = LocalDate.now();

        try {
            checkInReservationId = insertReservation(
                guestId,
                checkInNumber,
                "Standard",
                today,
                today.plusDays(1),
                "Confirmed"
            );

            checkOutReservationId = insertReservation(
                guestId,
                checkOutNumber,
                "Deluxe",
                today.minusDays(1),
                today,
                "Checked Out"
            );

            ReportService reportService = new ReportService();
            JsonObject result = JsonParser.parseString(reportService.getDailySummary()).getAsJsonObject();
            JsonArray checkIns = result.getAsJsonArray("checkIns");
            JsonArray checkOuts = result.getAsJsonArray("checkOuts");

            assertTrue(containsReservation(checkIns, checkInNumber));
            assertTrue(containsReservation(checkOuts, checkOutNumber));
        } finally {
            tryDeleteReservation(checkInReservationId);
            tryDeleteReservation(checkOutReservationId);
            tryDeleteGuest(guestId);
        }
    }

    @Test
    public void revenueByRoomType_increasesAfterConfirmedReservation() throws Exception {
        ReportService reportService = new ReportService();
        Map<String, Double> baseline = readRevenueByRoomType(reportService.getRevenueByRoomType());

        int guestId = insertGuest("RPT-R" + System.currentTimeMillis());
        int reservationId = 0;

        try {
            reservationId = insertReservation(
                guestId,
                shortReservationNumber("RREV"),
                "Deluxe",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(10),
                "Confirmed"
            );

            Map<String, Double> after = readRevenueByRoomType(reportService.getRevenueByRoomType());
            double beforeDeluxe = baseline.getOrDefault("Deluxe", 0.0);
            double afterDeluxe = after.getOrDefault("Deluxe", 0.0);

            assertTrue(afterDeluxe >= beforeDeluxe + 450.0 - 0.01,
                "Expected Deluxe revenue to increase by at least 450. before=" + beforeDeluxe + ", after=" + afterDeluxe);
        } finally {
            tryDeleteReservation(reservationId);
            tryDeleteGuest(guestId);
        }
    }

    @Test
    public void guestList_containsInsertedGuestWithSpendAndReservationCount() throws Exception {
        int guestId = insertGuest("RPT-G" + System.currentTimeMillis());
        int reservationId = 0;

        try {
            reservationId = insertReservation(
                guestId,
                shortReservationNumber("RGL"),
                "Standard",
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(17),
                "Confirmed"
            );

            ReportService reportService = new ReportService();
            JsonArray rows = JsonParser.parseString(reportService.getGuestList()).getAsJsonArray();
            JsonObject guestRow = findGuestRow(rows, guestId);

            assertNotNull(guestRow);
            assertTrue(guestRow.get("reservationCount").getAsInt() >= 1);
            assertTrue(guestRow.get("totalSpent").getAsDouble() >= 160.0 - 0.01);
        } finally {
            tryDeleteReservation(reservationId);
            tryDeleteGuest(guestId);
        }
    }

    private static int insertGuest(String marker) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, address, contact_number, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseTestSupport.appConnection()
            .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Report");
            ps.setString(2, marker);
            ps.setString(3, "Report Address");
            ps.setString(4, "071" + marker.substring(Math.max(0, marker.length() - 7)));
            ps.setString(5, marker.toLowerCase() + "@mail.com");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private static int insertReservation(
        int guestId,
        String reservationNumber,
        String roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        String status
    ) throws SQLException {
        String sql = "INSERT INTO reservations "
            + "(reservation_number, guest_id, room_type, check_in_date, check_out_date, total_amount, status, created_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DatabaseTestSupport.appConnection()
            .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reservationNumber);
            ps.setInt(2, guestId);
            ps.setString(3, roomType);
            ps.setString(4, checkIn.toString());
            ps.setString(5, checkOut.toString());
            ps.setDouble(6, 0.0);
            ps.setString(7, status);
            ps.setInt(8, 1);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private static Map<String, Double> readRevenueByRoomType(String revenueJson) {
        JsonObject root = JsonParser.parseString(revenueJson).getAsJsonObject();
        JsonArray rows = root.getAsJsonArray("rows");
        Map<String, Double> revenue = new HashMap<>();
        for (JsonElement e : rows) {
            JsonObject row = e.getAsJsonObject();
            revenue.put(row.get("roomType").getAsString(), row.get("revenue").getAsDouble());
        }
        return revenue;
    }

    private static boolean containsReservation(JsonArray rows, String reservationNumber) {
        for (JsonElement e : rows) {
            JsonObject row = e.getAsJsonObject();
            if (reservationNumber.equals(row.get("reservationNumber").getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject findGuestRow(JsonArray rows, int guestId) {
        for (JsonElement e : rows) {
            JsonObject row = e.getAsJsonObject();
            if (row.get("guestId").getAsInt() == guestId) {
                return row;
            }
        }
        return null;
    }

    private static String shortReservationNumber(String prefix) {
        long token = System.currentTimeMillis() % 1_000_000_000L;
        return prefix + "-" + token;
    }

    private static void tryDeleteReservation(int reservationId) throws SQLException {
        if (reservationId <= 0) {
            return;
        }
        try (PreparedStatement ps = DatabaseTestSupport.appConnection()
            .prepareStatement("DELETE FROM reservations WHERE reservation_id = ?")) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    private static void tryDeleteGuest(int guestId) throws SQLException {
        if (guestId <= 0) {
            return;
        }
        try (PreparedStatement ps = DatabaseTestSupport.appConnection()
            .prepareStatement("DELETE FROM guests WHERE guest_id = ?")) {
            ps.setInt(1, guestId);
            ps.executeUpdate();
        }
    }
}
