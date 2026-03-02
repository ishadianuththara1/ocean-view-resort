package com.oceanview.service;

import com.oceanview.model.Guest;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GuestServiceValidationTest {

    // null dao is ok here because when validation fails the dao never gets called
    private final GuestService service = new GuestService(null);

    private Guest makeGuest(String firstName, String lastName, String address,
                            String phone, String email) {
        Guest g = new Guest();
        g.setFirstName(firstName);
        g.setLastName(lastName);
        g.setAddress(address);
        g.setContactNumber(phone);
        g.setEmail(email);
        return g;
    }

    @Test
    public void createGuest_invalidInputs_returnError() throws Exception {
        List<Guest> invalidGuests = List.of(
            makeGuest("", "Smith", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com"),
            makeGuest("John", "S", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com"),
            makeGuest("John", "Smith", "No1", "0771234567", "john@gmail.com"),
            makeGuest("John", "Smith", "No 25, Main Street, Colombo", "123", "john@gmail.com"),
            makeGuest("John", "Smith", "No 25, Main Street, Colombo", "0771234567", "invalidemail"),
            makeGuest("John123", "Smith", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com")
        );

        for (Guest guest : invalidGuests) {
            assertNotNull(service.createGuest(guest));
        }
    }

    @Test
    public void updateGuest_invalidInputs_returnError() throws Exception {
        List<Guest> invalidGuests = List.of(
            makeGuest("", "Smith", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com"),
            makeGuest("John", "Smith", "No 25, Main Street, Colombo", "0771234567", "notvalid@@email")
        );

        for (Guest guest : invalidGuests) {
            guest.setGuestId(1);
            assertNotNull(service.updateGuest(guest));
        }
    }
}
