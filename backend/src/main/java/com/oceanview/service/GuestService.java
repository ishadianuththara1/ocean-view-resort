package com.oceanview.service;

import com.oceanview.dao.GuestDAO;
import com.oceanview.model.Guest;
import com.oceanview.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

public class GuestService {

    private final GuestDAO guestDAO;

    public GuestService(GuestDAO guestDAO) {
        this.guestDAO = guestDAO;
    }

    public String createGuest(Guest g) throws SQLException {
        String error = ValidationUtil.validateGuest(
            g.getFirstName(), g.getLastName(),
            g.getAddress(), g.getContactNumber(), g.getEmail()
        );
        if (error != null) return error;
        return guestDAO.createGuest(g) ? null : "Failed to save guest. Please try again.";
    }

    public List<Guest> getAllGuests() throws SQLException {
        return guestDAO.getAllGuests();
    }

    public Guest getGuestById(int id) throws SQLException {
        return guestDAO.getGuestById(id);
    }

    public List<Guest> searchGuests(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return guestDAO.getAllGuests();
        }
        return guestDAO.searchGuests(query.trim());
    }

    public String updateGuest(Guest g) throws SQLException {
        String error = ValidationUtil.validateGuest(
            g.getFirstName(), g.getLastName(),
            g.getAddress(), g.getContactNumber(), g.getEmail()
        );
        if (error != null) return error;
        return guestDAO.updateGuest(g) ? null : "Guest not found or could not be updated.";
    }

    public String deleteGuest(int id) throws SQLException {
        try {
            return guestDAO.deleteGuest(id) ? null : "Guest not found.";
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451 || e.getMessage().contains("foreign key constraint")) {
                return "Cannot delete this guest - they have existing reservations. "
                     + "Please delete their reservations first.";
            }
            throw e;
        }
    }
}
