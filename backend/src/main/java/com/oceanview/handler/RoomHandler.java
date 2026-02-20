package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RoomHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final RoomDAO roomDAO;
    private final Map<String, String> sessionStore;

    public RoomHandler(RoomDAO roomDAO, Map<String, String> sessionStore) {
        this.roomDAO      = roomDAO;
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

        try {
            List<Room> rooms = roomDAO.getAllRooms();
            CorsUtil.sendResponse(exchange, 200, gson.toJson(rooms));
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Internal server error.");
            CorsUtil.sendResponse(exchange, 500, gson.toJson(err));
        }
    }
}
