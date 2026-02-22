package com.oceanview.dao;

import com.oceanview.model.Reservation;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private final Connection connection;

    public ReservationDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createReservation(Reservation r) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_create(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setString(1, r.getReservationNumber());
            stmt.setInt(2, r.getGuestId());
            stmt.setString(3, r.getRoomType());
            stmt.setString(4, r.getCheckInDate());
            stmt.setString(5, r.getCheckOutDate());
            stmt.setDouble(6, r.getTotalAmount());
            stmt.setString(7, r.getStatus());
            stmt.setInt(8, r.getCreatedBy());
            stmt.registerOutParameter(9, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(9);
        }
    }

    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_get_all()}");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        }
        return list;
    }

    public Reservation getReservationById(int id) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_get_by_id(?)}")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    public List<Reservation> searchReservations(String query) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_search(?)}")) {
            stmt.setString(1, query);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        }
        return list;
    }

    public boolean updateReservation(Reservation r) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_update(?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, r.getReservationId());
            stmt.setString(2, r.getRoomType());
            stmt.setString(3, r.getCheckInDate());
            stmt.setString(4, r.getCheckOutDate());
            stmt.setDouble(5, r.getTotalAmount());
            stmt.setString(6, r.getStatus());
            stmt.registerOutParameter(7, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(7);
        }
    }

    public boolean deleteReservation(int id) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_reservation_delete(?, ?)}")) {
            stmt.setInt(1, id);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(2);
        }
    }

    public String generateReservationNumber() throws SQLException {
        String sql = "SELECT fn_next_reservation_number() AS reservation_number";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("reservation_number");
            }
            return null;
        }
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRoomType(rs.getString("room_type"));
        r.setCheckInDate(rs.getString("check_in_date"));
        r.setCheckOutDate(rs.getString("check_out_date"));
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setStatus(rs.getString("status"));
        r.setCreatedBy(rs.getInt("created_by"));
        r.setGuestName(rs.getString("guest_name"));
        r.setAddress(rs.getString("address"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setEmail(rs.getString("email"));
        return r;
    }
}
