package com.oceanview.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Reservation;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class BillService {

    private static final Gson gson = new Gson();

    private final ReservationDAO reservationDAO;

    private static final Map<String, Double> ROOM_RATES = new HashMap<>();
    static {
        ROOM_RATES.put("Standard",  80.0);
        ROOM_RATES.put("Deluxe",   150.0);
        ROOM_RATES.put("Suite",    250.0);
        ROOM_RATES.put("Family",   180.0);
    }

    public BillService(ReservationDAO reservationDAO) {
        this.reservationDAO = reservationDAO;
    }

    public String calculateBill(int reservationId) throws SQLException {
        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r == null) return null;

        LocalDate checkIn  = LocalDate.parse(r.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(r.getCheckOutDate());
        long   nights        = ChronoUnit.DAYS.between(checkIn, checkOut);
        double pricePerNight = ROOM_RATES.getOrDefault(r.getRoomType(), 0.0);
        double total         = nights * pricePerNight;

        JsonObject bill = new JsonObject();
        bill.addProperty("reservationNumber", r.getReservationNumber());
        bill.addProperty("guestName",         r.getGuestName());
        bill.addProperty("address",           r.getAddress());
        bill.addProperty("contactNumber",     r.getContactNumber());
        bill.addProperty("roomType",          r.getRoomType());
        bill.addProperty("checkInDate",       r.getCheckInDate());
        bill.addProperty("checkOutDate",      r.getCheckOutDate());
        bill.addProperty("numberOfNights",    nights);
        bill.addProperty("pricePerNight",     pricePerNight);
        bill.addProperty("totalAmount",       total);
        return gson.toJson(bill);
    }
}
