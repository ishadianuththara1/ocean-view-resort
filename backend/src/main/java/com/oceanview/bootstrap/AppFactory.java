package com.oceanview.bootstrap;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.UserDAO;
import com.oceanview.service.AuthService;
import com.oceanview.service.BillService;
import com.oceanview.service.GuestService;
import com.oceanview.service.ReportService;
import com.oceanview.service.ReservationService;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppFactory {

    private AppFactory() {
    }

    public static AppContext create() throws SQLException {
        Map<String, String> sessionStore = new ConcurrentHashMap<>();

        UserDAO userDAO = new UserDAO();
        GuestDAO guestDAO = new GuestDAO();
        ReservationDAO reservationDAO = new ReservationDAO();
        RoomDAO roomDAO = new RoomDAO();

        AuthService authService = new AuthService(userDAO);
        GuestService guestService = new GuestService(guestDAO);
        ReservationService reservationService = new ReservationService(reservationDAO);
        BillService billService = new BillService(reservationDAO);
        ReportService reportService = new ReportService();

        return new AppContext(
            sessionStore,
            roomDAO,
            authService,
            guestService,
            reservationService,
            billService,
            reportService
        );
    }

    public static final class AppContext {
        private final Map<String, String> sessionStore;
        private final RoomDAO roomDAO;
        private final AuthService authService;
        private final GuestService guestService;
        private final ReservationService reservationService;
        private final BillService billService;
        private final ReportService reportService;

        private AppContext(
            Map<String, String> sessionStore,
            RoomDAO roomDAO,
            AuthService authService,
            GuestService guestService,
            ReservationService reservationService,
            BillService billService,
            ReportService reportService
        ) {
            this.sessionStore = sessionStore;
            this.roomDAO = roomDAO;
            this.authService = authService;
            this.guestService = guestService;
            this.reservationService = reservationService;
            this.billService = billService;
            this.reportService = reportService;
        }

        public Map<String, String> getSessionStore() {
            return sessionStore;
        }

        public RoomDAO getRoomDAO() {
            return roomDAO;
        }

        public AuthService getAuthService() {
            return authService;
        }

        public GuestService getGuestService() {
            return guestService;
        }

        public ReservationService getReservationService() {
            return reservationService;
        }

        public BillService getBillService() {
            return billService;
        }

        public ReportService getReportService() {
            return reportService;
        }
    }
}
