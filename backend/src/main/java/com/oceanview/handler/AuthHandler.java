package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class AuthHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final AuthService authService;
    private final Map<String, String> sessionStore;

    public AuthHandler(AuthService authService, Map<String, String> sessionStore) {
        this.authService  = authService;
        this.sessionStore = sessionStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Method not allowed. Use POST.");
            CorsUtil.sendResponse(exchange, 405, gson.toJson(err));
            return;
        }

        String body = CorsUtil.readBody(exchange);

        JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
        String username = obj.has("username") ? obj.get("username").getAsString() : null;
        String password = obj.has("password") ? obj.get("password").getAsString() : null;

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Username and password are required.");
            CorsUtil.sendResponse(exchange, 400, gson.toJson(err));
            return;
        }

        try {
            User user = authService.authenticate(username, password);

            if (user == null) {
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Invalid username or password.");
                CorsUtil.sendResponse(exchange, 401, gson.toJson(err));
                return;
            }

            String token = UUID.randomUUID().toString();
            sessionStore.put(token, user.getUsername());

            CorsUtil.setCookie(exchange, "SESSIONID", token, 86400);

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("username", user.getUsername());
            res.addProperty("fullName", user.getFullName());
            CorsUtil.sendResponse(exchange, 200, gson.toJson(res));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Server error. Please try again.");
            CorsUtil.sendResponse(exchange, 500, gson.toJson(err));
        }
    }
}
