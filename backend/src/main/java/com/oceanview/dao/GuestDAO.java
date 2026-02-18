package com.oceanview.dao;

import com.oceanview.model.Guest;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    private final Connection connection;

    public GuestDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createGuest(Guest g) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_create(?, ?, ?, ?, ?, ?)}")) {
            stmt.setString(1, g.getFirstName());
            stmt.setString(2, g.getLastName());
            stmt.setString(3, g.getAddress());
            stmt.setString(4, g.getContactNumber());
            stmt.setString(5, g.getEmail());
            stmt.registerOutParameter(6, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(6);
        }
    }

    public List<Guest> getAllGuests() throws SQLException {
        List<Guest> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_get_all()}");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapGuest(rs));
            }
        }
        return list;
    }

    public Guest getGuestById(int id) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_get_by_id(?)}")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapGuest(rs);
                }
            }
        }
        return null;
    }

    public List<Guest> searchGuests(String query) throws SQLException {
        List<Guest> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_search(?)}")) {
            stmt.setString(1, query);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapGuest(rs));
                }
            }
        }
        return list;
    }

    public boolean updateGuest(Guest g) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_update(?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, g.getGuestId());
            stmt.setString(2, g.getFirstName());
            stmt.setString(3, g.getLastName());
            stmt.setString(4, g.getAddress());
            stmt.setString(5, g.getContactNumber());
            stmt.setString(6, g.getEmail());
            stmt.registerOutParameter(7, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(7);
        }
    }

    public boolean deleteGuest(int id) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_guest_delete(?, ?)}")) {
            stmt.setInt(1, id);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(2);
        }
    }

    private Guest mapGuest(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setAddress(rs.getString("address"));
        g.setContactNumber(rs.getString("contact_number"));
        g.setEmail(rs.getString("email"));
        g.setFullName(g.getFirstName() + " " + g.getLastName());
        return g;
    }
}
