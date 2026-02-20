package com.oceanview.dao;

import com.oceanview.model.Room;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    private final Connection connection;

    public RoomDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Room> getAllRooms() throws SQLException {
        List<Room> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_room_get_all()}");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomType(rs.getString("room_type"));
                room.setPricePerNight(rs.getDouble("price_per_night"));
                room.setAvailable(rs.getBoolean("is_available"));
                list.add(room);
            }
        }
        return list;
    }
}
