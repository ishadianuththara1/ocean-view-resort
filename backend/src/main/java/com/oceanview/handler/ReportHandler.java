package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.service.ReportService;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class ReportHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final ReportService reportService;
    private final Map<String, String> sessionStore;

    public ReportHandler(ReportService reportService, Map<String, String> sessionStore) {
        this.reportService = reportService;
        this.sessionStore  = sessionStore;
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

        String query = exchange.getRequestURI().getQuery();
        String type  = null;
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("type=")) {
                    type = param.substring(5);
                    break;
                }
            }
        }

        if (type == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Missing required parameter: type");
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
            return;
        }

        try {
            String json;
            switch (type) {
                case "daily":
                    json = reportService.getDailySummary();
                    break;
                case "revenue":
                    json = reportService.getRevenueByRoomType();
                    break;
                case "guests":
                    json = reportService.getGuestList();
                    break;
                default:
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Invalid type. Use: daily, revenue, or guests");
                    CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
                    return;
            }
            CorsUtil.sendResponse(exchange, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Internal server error.");
            CorsUtil.sendResponse(exchange, 500, gson.toJson(err));
        }
    }
}
