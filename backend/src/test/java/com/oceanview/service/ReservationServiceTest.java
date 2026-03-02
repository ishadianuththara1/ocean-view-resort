package com.oceanview.service;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {

    // passing null for DAO because these methods dont touch the database
    private final ReservationService service = new ReservationService(null);

    @Test
    public void getPricePerNight_returnsExpectedRates() {
        Map<String, Double> expectedRates = Map.of(
            "Standard", 80.0,
            "Deluxe", 150.0,
            "Suite", 250.0,
            "Family", 180.0
        );

        expectedRates.forEach((roomType, expectedRate) ->
            assertEquals(expectedRate, service.getPricePerNight(roomType), "roomType=" + roomType)
        );

        assertEquals(0.0, service.getPricePerNight("Penthouse"), "Unknown room type should return zero");
    }

    @Test
    public void calculateNights_returnsExpectedValues() {
        List<NightCase> cases = List.of(
            new NightCase("2025-06-15", "2025-06-18", 3),
            new NightCase("2025-06-15", "2025-06-16", 1),
            new NightCase("2025-06-10", "2025-06-17", 7),
            new NightCase("2025-06-28", "2025-07-03", 5)
        );

        for (NightCase c : cases) {
            assertEquals(c.expectedNights(), service.calculateNights(c.checkIn(), c.checkOut()), c.toString());
        }
    }

    @Test
    public void calculateTotal_returnsExpectedValues() {
        List<TotalCase> cases = List.of(
            new TotalCase("Standard", "2025-06-15", "2025-06-18", 240.0),
            new TotalCase("Suite", "2025-06-15", "2025-06-16", 250.0),
            new TotalCase("Deluxe", "2025-07-01", "2025-07-06", 750.0),
            new TotalCase("Family", "2025-09-10", "2025-09-12", 360.0),
            new TotalCase("Standard", "2025-08-01", "2025-08-08", 560.0)
        );

        for (TotalCase c : cases) {
            assertEquals(c.expectedTotal(),
                service.calculateTotal(c.roomType(), c.checkIn(), c.checkOut()),
                c.toString());
        }
    }

    private record NightCase(String checkIn, String checkOut, long expectedNights) {}

    private record TotalCase(String roomType, String checkIn, String checkOut, double expectedTotal) {}
}
