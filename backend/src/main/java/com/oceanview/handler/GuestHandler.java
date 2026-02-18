package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oceanview.model.Guest;
import com.oceanview.service.GuestService;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GuestHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final GuestService guestService;
    private final Map<String, String> sessionStore;

    public GuestHandler(GuestService guestService, Map<String, String> sessionStore) {
        this.guestService = guestService;
        this.sessionStore = sessionStore;
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
                    err.addProperty("message", "Invalid guest ID.");
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
        List<Guest> list = guestService.getAllGuests();
        CorsUtil.sendResponse(exchange, 200, gson.toJson(list));
    }

    private void handleSearch(HttpExchange exchange, String term) throws Exception {
        List<Guest> list = guestService.searchGuests(term);
        CorsUtil.sendResponse(exchange, 200, gson.toJson(list));
    }

    private void handleGetOne(HttpExchange exchange, int id) throws Exception {
        Guest g = guestService.getGuestById(id);
        if (g == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Guest not found.");
            CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
        } else {
            CorsUtil.sendResponse(exchange, 200, gson.toJson(g));
        }
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        String body = CorsUtil.readBody(exchange);
        Guest g = parseBody(body);

        String error = guestService.createGuest(g);
        if (error != null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", error);
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Guest registered successfully.");
            CorsUtil.sendResponse(exchange, 201, gson.toJson(res));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws Exception {
        String body = CorsUtil.readBody(exchange);
        Guest g = parseBody(body);
        g.setGuestId(id);

        String error = guestService.updateGuest(g);
        if (error != null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", error);
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Guest updated successfully.");
            CorsUtil.sendResponse(exchange, 200, gson.toJson(res));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        String error = guestService.deleteGuest(id);
        if (error != null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", error);
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Guest deleted successfully.");
            CorsUtil.sendResponse(exchange, 200, gson.toJson(res));
        }
    }

    private Guest parseBody(String body) {
        JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
        Guest g = new Guest();
        g.setFirstName(    nullToEmpty(obj.has("firstName")     ? obj.get("firstName").getAsString()     : null));
        g.setLastName(     nullToEmpty(obj.has("lastName")      ? obj.get("lastName").getAsString()      : null));
        g.setAddress(      nullToEmpty(obj.has("address")       ? obj.get("address").getAsString()       : null));
        g.setContactNumber(nullToEmpty(obj.has("contactNumber") ? obj.get("contactNumber").getAsString() : null));
        String email = obj.has("email") ? obj.get("email").getAsString() : null;
        g.setEmail(email != null ? email.trim() : null);
        return g;
    }

    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
