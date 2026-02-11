package com.oceanview.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ValidationUtil {

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) return false;
        String t = name.trim();
        return t.length() >= 2 && t.length() <= 50 && t.matches("[a-zA-Z ]+");
    }

    public static boolean isValidAddress(String address) {
        if (!isNotEmpty(address)) return false;
        String t = address.trim();
        return t.length() >= 5 && t.length() <= 255;
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String digits = phone.replaceAll("[\\s\\-\\+]", "");
        return digits.matches("\\d{10,15}");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true;
        return email.trim().matches("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    }

    public static boolean isValidDate(String date) {
        if (!isNotEmpty(date)) return false;
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isCheckOutAfterCheckIn(String checkIn, String checkOut) {
        try {
            return LocalDate.parse(checkOut).isAfter(LocalDate.parse(checkIn));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidRoomType(String roomType) {
        if (!isNotEmpty(roomType)) return false;
        return roomType.equals("Standard") || roomType.equals("Deluxe")
            || roomType.equals("Suite")    || roomType.equals("Family");
    }

    public static boolean isValidStatus(String status) {
        if (!isNotEmpty(status)) return false;
        return status.equals("Confirmed") || status.equals("Cancelled") || status.equals("Checked Out");
    }

    public static String validateGuest(String firstName, String lastName,
            String address, String contactNumber, String email) {

        if (!isValidName(firstName)) {
            return "First name must be 2-50 characters (letters and spaces only).";
        }
        if (!isValidName(lastName)) {
            return "Last name must be 2-50 characters (letters and spaces only).";
        }
        if (!isValidAddress(address)) {
            return "Address must be between 5 and 255 characters.";
        }
        if (!isValidPhone(contactNumber)) {
            return "Contact number must contain 10-15 digits.";
        }
        if (!isValidEmail(email)) {
            return "Email address format is invalid.";
        }
        return null;
    }

    public static String validateBooking(String roomType, String checkIn, String checkOut) {
        if (!isValidRoomType(roomType)) {
            return "Room type must be Standard, Deluxe, Suite, or Family.";
        }
        if (!isValidDate(checkIn)) {
            return "Check-in date is invalid. Use the format yyyy-MM-dd.";
        }
        if (!isValidDate(checkOut)) {
            return "Check-out date is invalid. Use the format yyyy-MM-dd.";
        }
        if (!isCheckOutAfterCheckIn(checkIn, checkOut)) {
            return "Check-out date must be after check-in date (at least 1 night).";
        }
        return null;
    }
}
