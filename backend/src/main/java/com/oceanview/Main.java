package com.oceanview;

import com.oceanview.bootstrap.AppFactory;
import com.oceanview.handler.AuthHandler;
import com.oceanview.handler.BillHandler;
import com.oceanview.handler.GuestHandler;
import com.oceanview.handler.LogoutHandler;
import com.oceanview.handler.ReportHandler;
import com.oceanview.handler.ReservationHandler;
import com.oceanview.handler.RoomHandler;
import com.oceanview.handler.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting System...");

        AppFactory.AppContext app = AppFactory.create();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/login", new AuthHandler(app.getAuthService(), app.getSessionStore()));
        server.createContext("/api/logout", new LogoutHandler(app.getSessionStore()));
        server.createContext("/api/guests", new GuestHandler(app.getGuestService(), app.getSessionStore()));
        server.createContext("/api/reservations", new ReservationHandler(app.getReservationService(), app.getSessionStore()));
        server.createContext("/api/rooms", new RoomHandler(app.getRoomDAO(), app.getSessionStore()));
        server.createContext("/api/bill", new BillHandler(app.getBillService(), app.getSessionStore()));
        server.createContext("/api/reports", new ReportHandler(app.getReportService(), app.getSessionStore()));
        server.createContext("/", new StaticFileHandler(findFrontend()));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Server started on http://localhost:8080");
    }
    private static Path findFrontend() {
        for (String candidate : new String[]{"../frontend", "frontend"}) {
            Path p = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isDirectory(p) && Files.exists(p.resolve("login.html"))) {
                return p;
            }
        }
        throw new RuntimeException(
            "Cannot find frontend directory. Run the server from the project root or the backend/ directory.");
    }
}
