package com.oceanview.dao;

import com.oceanview.model.User;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private final Connection connection;

    public UserDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public User findByCredentials(String username, String password) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL sp_user_find_by_credentials(?, ?)}")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    return user;
                }
            }
        }
        return null;
    }
}
