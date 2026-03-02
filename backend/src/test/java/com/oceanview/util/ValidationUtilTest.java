package com.oceanview.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    public void primitiveValidation_methods_handleGroupedCases() {
        List<UnaryCase> notEmptyCases = List.of(
            new UnaryCase(null, false),
            new UnaryCase("", false),
            new UnaryCase("     ", false),
            new UnaryCase("hello", true)
        );
        for (UnaryCase c : notEmptyCases) {
            assertEquals(c.expected(), ValidationUtil.isNotEmpty(c.input()), "isNotEmpty input=" + c.input());
        }

        List<UnaryCase> nameCases = List.of(
            new UnaryCase("John", true),
            new UnaryCase("De Silva", true),
            new UnaryCase("J", false),
            new UnaryCase("John123", false),
            new UnaryCase(null, false),
            new UnaryCase("John@", false)
        );
        for (UnaryCase c : nameCases) {
            assertEquals(c.expected(), ValidationUtil.isValidName(c.input()), "isValidName input=" + c.input());
        }

        List<UnaryCase> addressCases = List.of(
            new UnaryCase("No 25, Main Street, Colombo", true),
            new UnaryCase("No1", false),
            new UnaryCase(null, false),
            new UnaryCase("", false)
        );
        for (UnaryCase c : addressCases) {
            assertEquals(c.expected(), ValidationUtil.isValidAddress(c.input()), "isValidAddress input=" + c.input());
        }

        List<UnaryCase> phoneCases = List.of(
            new UnaryCase("0771234567", true),
            new UnaryCase("12345", false),
            new UnaryCase("077-123-4567", true),
            new UnaryCase("+94 77 123 4567", true),
            new UnaryCase("077abc1234", false)
        );
        for (UnaryCase c : phoneCases) {
            assertEquals(c.expected(), ValidationUtil.isValidPhone(c.input()), "isValidPhone input=" + c.input());
        }

        List<UnaryCase> emailCases = List.of(
            new UnaryCase("test@gmail.com", true),
            new UnaryCase("testgmail.com", false),
            new UnaryCase("test@", false),
            new UnaryCase(null, true),
            new UnaryCase("", true)
        );
        for (UnaryCase c : emailCases) {
            assertEquals(c.expected(), ValidationUtil.isValidEmail(c.input()), "isValidEmail input=" + c.input());
        }

        List<UnaryCase> dateCases = List.of(
            new UnaryCase("2025-06-15", true),
            new UnaryCase("15/06/2025", false),
            new UnaryCase("15-06-2025", false),
            new UnaryCase(null, false),
            new UnaryCase("this is not a date", false)
        );
        for (UnaryCase c : dateCases) {
            assertEquals(c.expected(), ValidationUtil.isValidDate(c.input()), "isValidDate input=" + c.input());
        }
    }

    @Test
    public void domainValidation_methods_handleGroupedCases() {
        List<RangeCase> rangeCases = List.of(
            new RangeCase("2025-06-15", "2025-06-20", true),
            new RangeCase("2025-06-15", "2025-06-15", false),
            new RangeCase("2025-06-20", "2025-06-15", false)
        );
        for (RangeCase c : rangeCases) {
            assertEquals(c.expected(),
                ValidationUtil.isCheckOutAfterCheckIn(c.checkIn(), c.checkOut()),
                "isCheckOutAfterCheckIn " + c);
        }

        List<UnaryCase> roomTypeCases = List.of(
            new UnaryCase("Standard", true),
            new UnaryCase("Deluxe", true),
            new UnaryCase("Suite", true),
            new UnaryCase("Family", true),
            new UnaryCase("Penthouse", false),
            new UnaryCase("standard", false)
        );
        for (UnaryCase c : roomTypeCases) {
            assertEquals(c.expected(), ValidationUtil.isValidRoomType(c.input()), "isValidRoomType input=" + c.input());
        }

        List<UnaryCase> statusCases = List.of(
            new UnaryCase("Confirmed", true),
            new UnaryCase("Cancelled", true),
            new UnaryCase("Checked Out", true),
            new UnaryCase("Active", false)
        );
        for (UnaryCase c : statusCases) {
            assertEquals(c.expected(), ValidationUtil.isValidStatus(c.input()), "isValidStatus input=" + c.input());
        }
    }

    @Test
    public void validateGuest_handlesValidAndInvalidCases() {
        assertNull(ValidationUtil.validateGuest(
            "John", "Smith",
            "No 25, Main Street, Colombo",
            "0771234567",
            "john@gmail.com"
        ));

        List<GuestValidationCase> invalidCases = List.of(
            new GuestValidationCase("", "Smith", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com"),
            new GuestValidationCase("John", "S", "No 25, Main Street, Colombo", "0771234567", "john@gmail.com"),
            new GuestValidationCase("John", "Smith", "No 25, Main Street, Colombo", "123", "john@gmail.com"),
            new GuestValidationCase("John", "Smith", "No 25, Main Street, Colombo", "0771234567", "invalidemail")
        );

        for (GuestValidationCase c : invalidCases) {
            assertNotNull(ValidationUtil.validateGuest(
                c.firstName(), c.lastName(), c.address(), c.phone(), c.email()
            ), c.toString());
        }
    }

    @Test
    public void validateBooking_handlesValidAndInvalidCases() {
        assertNull(ValidationUtil.validateBooking("Standard", "2025-08-01", "2025-08-05"));

        List<BookingValidationCase> invalidCases = List.of(
            new BookingValidationCase("Penthouse", "2025-08-01", "2025-08-05"),
            new BookingValidationCase("Standard", "2025-08-01", "2025-08-01"),
            new BookingValidationCase("Deluxe", "01-08-2025", "05-08-2025"),
            new BookingValidationCase("Suite", "2025-08-10", "2025-08-05")
        );

        for (BookingValidationCase c : invalidCases) {
            assertNotNull(ValidationUtil.validateBooking(c.roomType(), c.checkIn(), c.checkOut()), c.toString());
        }
    }

    private record UnaryCase(String input, boolean expected) {}

    private record RangeCase(String checkIn, String checkOut, boolean expected) {}

    private record GuestValidationCase(
        String firstName, String lastName, String address, String phone, String email
    ) {}

    private record BookingValidationCase(String roomType, String checkIn, String checkOut) {}
}
