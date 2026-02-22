package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oceanview.model.Reservation;
import com.oceanview.service.ReservationService;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ReservationHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final ReservationService reservationService;
    private final Map<String, String> sessionStore;

    public ReservationHandler(ReservationService reservationService, Map<String, String> sessionStore) {
        this.reservationService = reservationService;
        this.sessionStore       = sessionStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String token = CorsUtil.getSessionIdFromCookie(exchange);
        if (token == null || !sessionStore.containsKey(token)) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Unauthorized. Please log in.");
            CorsUtil.sendResponse(exchange, 401, gson.toJson(err));
            return;
        }

        String path   = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        String query  = exchange.getRequestURI().getQuery();
        String[] parts = path.split("/");

        try {
            if (parts.length == 3) {
                if ("GET".equals(method)) {
                    if (query != null && query.startsWith("search=")) {
                        String term = URLDecoder.decode(query.substring(7), StandardCharsets.UTF_8);
                        handleSearch(exchange, term);
                    } else {
                        handleGetAll(exchange);
                    }
                } else if ("POST".equals(method)) {
                    handleCreate(exchange);
                } else {
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Method not allowed.");
                    CorsUtil.sendResponse(exchange, 405, gson.toJson(err));
                }
            } else if (parts.length == 4) {
                int id;
                try {
                    id = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Invalid reservation ID.");
                    CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
                    return;
                }
                if ("GET".equals(method)) {
                    handleGetOne(exchange, id);
                } else if ("PUT".equals(method)) {
                    handleUpdate(exchange, id);
                } else if ("DELETE".equals(method)) {
                    handleDelete(exchange, id);
                } else {
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Method not allowed.");
                    CorsUtil.sendResponse(exchange, 405, gson.toJson(err));
                }
            } else {
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Endpoint not found.");
                CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Internal server error.");
            CorsUtil.sendResponse(exchange, 500, gson.toJson(err));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws Exception {
        List<Reservation> list = reservationService.getAllReservations();
        CorsUtil.sendResponse(exchange, 200, gson.toJson(list));
    }

    private void handleSearch(HttpExchange exchange, String term) throws Exception {
        List<Reservation> list = reservationService.searchReservations(term);
        CorsUtil.sendResponse(exchange, 200, gson.toJson(list));
    }

    private void handleGetOne(HttpExchange exchange, int id) throws Exception {
        Reservation r = reservationService.getReservationById(id);
        if (r == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Reservation not found.");
            CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
        } else {
            CorsUtil.sendResponse(exchange, 200, gson.toJson(r));
        }
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        String body = CorsUtil.readBody(exchange);
        JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

        Reservation r = new Reservation();

        String guestIdStr = obj.has("guestId") ? obj.get("guestId").getAsString() : null;
        if (guestIdStr == null || guestIdStr.trim().isEmpty()) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Please select a guest for this reservation.");
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
            return;
        }
        try {
            r.setGuestId(Integer.parseInt(guestIdStr.trim()));
        } catch (NumberFormatException e) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Invalid guest ID.");
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
            return;
        }

        r.setRoomType(    nullToEmpty(obj.has("roomType")     ? obj.get("roomType").getAsString()     : null));
        r.setCheckInDate( nullToEmpty(obj.has("checkInDate")  ? obj.get("checkInDate").getAsString()  : null));
        r.setCheckOutDate(nullToEmpty(obj.has("checkOutDate") ? obj.get("checkOutDate").getAsString() : null));
        r.setCreatedBy(1);

        String error = reservationService.createReservation(r);
        if (error != null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", error);
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Reservation created successfully.");
            res.addProperty("reservationNumber", r.getReservationNumber());
            CorsUtil.sendResponse(exchange, 201, gson.toJson(res));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws Exception {
        Reservation existing = reservationService.getReservationById(id);
        if (existing == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Reservation not found.");
            CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
            return;
        }

        String body = CorsUtil.readBody(exchange);
        JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
        Reservation r = new Reservation();
        r.setReservationId(id);
        r.setReservationNumber(existing.getReservationNumber());
        r.setGuestId(existing.getGuestId());
        r.setRoomType(    nullToEmpty(obj.has("roomType")     ? obj.get("roomType").getAsString()     : null));
        r.setCheckInDate( nullToEmpty(obj.has("checkInDate")  ? obj.get("checkInDate").getAsString()  : null));
        r.setCheckOutDate(nullToEmpty(obj.has("checkOutDate") ? obj.get("checkOutDate").getAsString() : null));
        r.setStatus(      nullToEmpty(obj.has("status")       ? obj.get("status").getAsString()       : null));

        String error = reservationService.updateReservation(r);
        if (error != null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", error);
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Reservation updated successfully.");
            CorsUtil.sendResponse(exchange, 200, gson.toJson(res));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        boolean deleted = reservationService.deleteReservation(id);
        if (!deleted) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Reservation not found.");
            CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Reservation deleted successfully.");
            CorsUtil.sendResponse(exchange, 200, gson.toJson(res));
        }
    }

    private String nullToEmpty(String v) { return v == null ? "" : v; }
}
