package com.oceanview.model;

public class User {

    private int    userId;
    private String username;
    private String password;
    private String fullName;

    public User() {}

    public User(int userId, String username, String password, String fullName) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public int    getUserId()   { return userId;   }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }

    public void setUserId(int userId)       { this.userId   = userId;   }
    public void setUsername(String username){ this.username = username; }
    public void setPassword(String password){ this.password = password; }
    public void setFullName(String fullName){ this.fullName = fullName; }
}
