package com.oceanview.service;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Reservation;
import com.oceanview.util.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationService {

    private final ReservationDAO reservationDAO;

    private static final Map<String, Double> ROOM_RATES = new HashMap<>();
    static {
        ROOM_RATES.put("Standard",  80.0);
        ROOM_RATES.put("Deluxe",   150.0);
        ROOM_RATES.put("Suite",    250.0);
        ROOM_RATES.put("Family",   180.0);
    }

    public ReservationService(ReservationDAO reservationDAO) {
        this.reservationDAO = reservationDAO;
    }

    public double getPricePerNight(String roomType) {
        return ROOM_RATES.getOrDefault(roomType, 0.0);
    }

    public long calculateNights(String checkIn, String checkOut) {
        LocalDate in  = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        return ChronoUnit.DAYS.between(in, out);
    }

    public double calculateTotal(String roomType, String checkIn, String checkOut) {
        return calculateNights(checkIn, checkOut) * getPricePerNight(roomType);
    }

    public String createReservation(Reservation r) throws SQLException {
        if (r.getGuestId() <= 0) {
            return "Please select a guest for this reservation.";
        }
        String error = ValidationUtil.validateBooking(r.getRoomType(), r.getCheckInDate(), r.getCheckOutDate());
        if (error != null) return error;

        r.setReservationNumber(reservationDAO.generateReservationNumber());
        r.setTotalAmount(calculateTotal(r.getRoomType(), r.getCheckInDate(), r.getCheckOutDate()));
        r.setStatus("Confirmed");

        boolean created = reservationDAO.createReservation(r);
        return created ? null : "Failed to save reservation. Please try again.";
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.getAllReservations();
    }

    public Reservation getReservationById(int id) throws SQLException {
        return reservationDAO.getReservationById(id);
    }

    public List<Reservation> searchReservations(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) return reservationDAO.getAllReservations();
        return reservationDAO.searchReservations(query.trim());
    }

    public String updateReservation(Reservation r) throws SQLException {
        String error = ValidationUtil.validateBooking(r.getRoomType(), r.getCheckInDate(), r.getCheckOutDate());
        if (error != null) return error;

        if (!ValidationUtil.isValidStatus(r.getStatus())) {
            return "Invalid status value.";
        }

        r.setTotalAmount(calculateTotal(r.getRoomType(), r.getCheckInDate(), r.getCheckOutDate()));
        boolean updated = reservationDAO.updateReservation(r);
        return updated ? null : "Reservation not found or could not be updated.";
    }

    public boolean deleteReservation(int id) throws SQLException {
        return reservationDAO.deleteReservation(id);
    }
}
