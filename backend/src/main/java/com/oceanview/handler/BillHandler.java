package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.service.BillService;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class BillHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final BillService billService;
    private final Map<String, String> sessionStore;

    public BillHandler(BillService billService, Map<String, String> sessionStore) {
        this.billService  = billService;
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

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Method not allowed.");
            CorsUtil.sendResponse(exchange, 405, gson.toJson(err));
            return;
        }

        String path   = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length != 4) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Usage: GET /api/bill/{reservationId}");
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
            return;
        }

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

        try {
            String billJson = billService.calculateBill(id);
            if (billJson == null) {
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Reservation not found.");
                CorsUtil.sendResponse(exchange, 404, gson.toJson(err));
            } else {
                CorsUtil.sendResponse(exchange, 200, billJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Internal server error.");
            CorsUtil.sendResponse(exchange, 500, gson.toJson(err));
        }
    }
}
