package com.oceanview.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.util.CorsUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class LogoutHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    private final Map<String, String> sessionStore;

    public LogoutHandler(Map<String, String> sessionStore) {
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
        if (token != null) {
            sessionStore.remove(token);
        }

        CorsUtil.setCookie(exchange, "SESSIONID", "", 0);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("message", "Logged out successfully.");
        CorsUtil.sendResponse(exchange, 200, gson.toJson(res));
    }
}
