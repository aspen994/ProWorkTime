package com.example.ogrdapp.model;

public class User {
    private String email;
    private String foreign_key;
    private String surName;
    private String userId;
    private String username;
    private int hoursOverall;
    private int hoursToSettle;

    public User(String email, String foreign_key, String surName, String userId, String userName,int hoursOverall,int hoursToSettle) {
        this.email = email;
        this.foreign_key = foreign_key;
        this.surName = surName;
        this.userId = userId;
        this.username = userName;
        this.hoursOverall=  hoursOverall;
        this.hoursToSettle = hoursToSettle;
    }

    public int getHoursOverall() {
        return hoursOverall;
    }

    public void setHoursOverall(int hoursOverall) {
        this.hoursOverall = hoursOverall;
    }

    public int getHoursToSettle() {
        return hoursToSettle;
    }

    public void setHoursToSettle(int hoursToSettle) {
        this.hoursToSettle = hoursToSettle;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getForeign_key() {
        return foreign_key;
    }

    public void setForeign_key(String foreign_key) {
        this.foreign_key = foreign_key;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", foreignKey='" + foreign_key + '\'' +
                ", surName='" + surName + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + username + '\'' +
                '}';
    }
}
