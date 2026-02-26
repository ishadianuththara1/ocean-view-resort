package com.oceanview.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.oceanview.dao.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportService {

    private static final Gson gson = new Gson();

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public String getDailySummary() throws SQLException {
        String today = LocalDate.now().toString();
        Connection conn = getConn();

        JsonArray checkIns = runDailySummaryRows(conn, "sp_report_daily_checkins");
        JsonArray checkOuts = runDailySummaryRows(conn, "sp_report_daily_checkouts");

        JsonObject result = new JsonObject();
        result.addProperty("date", today);
        result.addProperty("checkInsCount", checkIns.size());
        result.add("checkIns", checkIns);
        result.addProperty("checkOutsCount", checkOuts.size());
        result.add("checkOuts", checkOuts);
        return gson.toJson(result);
    }

    private JsonArray runDailySummaryRows(Connection conn, String procedureName) throws SQLException {
        JsonArray rows = new JsonArray();
        try (CallableStatement cs = conn.prepareCall("{CALL " + procedureName + "()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                JsonObject row = new JsonObject();
                row.addProperty("reservationNumber", rs.getString("reservation_number"));
                row.addProperty("guestName",         rs.getString("guest_name"));
                row.addProperty("roomType",          rs.getString("room_type"));
                row.addProperty("checkInDate",       rs.getString("check_in_date"));
                row.addProperty("checkOutDate",      rs.getString("check_out_date"));
                row.addProperty("status",            rs.getString("status"));
                rows.add(row);
            }
        }
        return rows;
    }

    public String getRevenueByRoomType() throws SQLException {
        Connection conn = getConn();

        JsonArray rows = new JsonArray();
        double grandTotal = 0;
        int    grandCount = 0;

        try (CallableStatement cs = conn.prepareCall("{CALL sp_report_revenue_by_room_type()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                int    cnt     = rs.getInt("cnt");
                double revenue = rs.getDouble("revenue");
                grandTotal += revenue;
                grandCount += cnt;
                JsonObject row = new JsonObject();
                row.addProperty("roomType", rs.getString("room_type"));
                row.addProperty("count",    cnt);
                row.addProperty("revenue",  revenue);
                rows.add(row);
            }
        }

        JsonObject result = new JsonObject();
        result.add("rows", rows);
        result.addProperty("grandTotal", grandTotal);
        result.addProperty("grandCount", grandCount);
        return gson.toJson(result);
    }

    public String getGuestList() throws SQLException {
        Connection conn = getConn();

        JsonArray rows = new JsonArray();
        try (CallableStatement cs = conn.prepareCall("{CALL sp_report_guest_list()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                JsonObject row = new JsonObject();
                row.addProperty("guestId",          rs.getInt("guest_id"));
                row.addProperty("firstName",        rs.getString("first_name"));
                row.addProperty("lastName",         rs.getString("last_name"));
                row.addProperty("contactNumber",    rs.getString("contact_number"));
                row.addProperty("email",            rs.getString("email") != null ? rs.getString("email") : "");
                row.addProperty("reservationCount", rs.getInt("reservation_count"));
                row.addProperty("totalSpent",       rs.getDouble("total_spent"));
                rows.add(row);
            }
        }
        return gson.toJson(rows);
    }
}
