package com.oceanview.integration;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.UserDAO;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DaoIntegrationTest {

    @BeforeAll
    static void beforeAll() {
        DatabaseTestSupport.assumeDatabaseReady();
    }

    @Test
    public void userDao_findByCredentials_returnsSeededUser() throws Exception {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByCredentials("admin", "admin123");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    public void roomDao_getAllRooms_returnsSeededRooms() throws Exception {
        RoomDAO roomDAO = new RoomDAO();
        List<Room> rooms = roomDAO.getAllRooms();
        assertFalse(rooms.isEmpty());
        assertTrue(rooms.stream().anyMatch(r -> "101".equals(r.getRoomNumber())));
    }

    @Test
    public void guestDao_crudAndSearch_flowWorks() throws Exception {
        GuestDAO guestDAO = new GuestDAO();
        String marker = "ITG" + System.currentTimeMillis();
        int guestId = 0;

        Guest guest = new Guest();
        guest.setFirstName("Test");
        guest.setLastName(marker);
        guest.setAddress("No 1 Integration Road");
        guest.setContactNumber("077" + marker.substring(Math.max(0, marker.length() - 7)));
        guest.setEmail(marker.toLowerCase() + "@mail.com");

        try {
            assertTrue(guestDAO.createGuest(guest));

            List<Guest> found = guestDAO.searchGuests(marker);
            assertFalse(found.isEmpty());
            guestId = found.get(0).getGuestId();

            Guest one = guestDAO.getGuestById(guestId);
            assertNotNull(one);
            assertEquals("Test", one.getFirstName());

            one.setAddress("No 2 Updated Integration Road");
            assertTrue(guestDAO.updateGuest(one));

            Guest updated = guestDAO.getGuestById(guestId);
            assertNotNull(updated);
            assertEquals("No 2 Updated Integration Road", updated.getAddress());

            assertTrue(guestDAO.deleteGuest(guestId));
            assertNull(guestDAO.getGuestById(guestId));
            guestId = 0;
        } finally {
            if (guestId > 0) {
                deleteReservationByGuestId(guestId);
                tryDeleteGuest(guestId);
            }
        }
    }

    @Test
    public void reservationDao_crudAndSearch_flowWorks() throws Exception {
        ReservationDAO reservationDAO = new ReservationDAO();
        String marker = "ITR-" + System.currentTimeMillis();
        int guestId = insertGuest("Res", marker);
        int reservationId = 0;

        Reservation reservation = new Reservation();
        reservation.setReservationNumber(marker);
        reservation.setGuestId(guestId);
        reservation.setRoomType("Standard");
        reservation.setCheckInDate(LocalDate.now().plusDays(20).toString());
        reservation.setCheckOutDate(LocalDate.now().plusDays(22).toString());
        reservation.setTotalAmount(0.0);
        reservation.setStatus("Confirmed");
        reservation.setCreatedBy(1);

        try {
            assertTrue(reservationDAO.createReservation(reservation));

            List<Reservation> found = reservationDAO.searchReservations(marker);
            assertFalse(found.isEmpty());
            reservationId = found.get(0).getReservationId();

            Reservation one = reservationDAO.getReservationById(reservationId);
            assertNotNull(one);
            assertEquals(marker, one.getReservationNumber());

            one.setRoomType("Suite");
            one.setCheckInDate(LocalDate.now().plusDays(20).toString());
            one.setCheckOutDate(LocalDate.now().plusDays(23).toString());
            one.setStatus("Checked Out");
            one.setTotalAmount(0.0);
            assertTrue(reservationDAO.updateReservation(one));

            Reservation updated = reservationDAO.getReservationById(reservationId);
            assertNotNull(updated);
            assertEquals("Suite", updated.getRoomType());
            assertEquals("Checked Out", updated.getStatus());
            assertTrue(updated.getTotalAmount() > 0);

            assertTrue(reservationDAO.deleteReservation(reservationId));
            assertNull(reservationDAO.getReservationById(reservationId));
            reservationId = 0;
        } finally {
            if (reservationId > 0) {
                tryDeleteReservation(reservationId);
            }
            deleteReservationByGuestId(guestId);
            tryDeleteGuest(guestId);
        }
    }

    @Test
    public void reservationDao_generateReservationNumber_returnsExpectedFormat() throws Exception {
        ReservationDAO reservationDAO = new ReservationDAO();
        String number = reservationDAO.generateReservationNumber();
        assertNotNull(number);
        assertTrue(number.matches("RES-\\d{8}"), "Unexpected format: " + number);
    }

    private static int insertGuest(String firstName, String marker) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, address, contact_number, email) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseTestSupport.appConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName);
            ps.setString(2, marker);
            ps.setString(3, "Integration Address");
            ps.setString(4, "071" + marker.substring(Math.max(0, marker.length() - 7)));
            ps.setString(5, marker.toLowerCase() + "@mail.com");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private static void deleteReservationByGuestId(int guestId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE guest_id = ?";
        try (PreparedStatement ps = DatabaseTestSupport.appConnection().prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ps.executeUpdate();
        }
    }

    private static void tryDeleteReservation(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement ps = DatabaseTestSupport.appConnection().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    private static void tryDeleteGuest(int guestId) throws SQLException {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (PreparedStatement ps = DatabaseTestSupport.appConnection().prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ps.executeUpdate();
        }
    }
}
