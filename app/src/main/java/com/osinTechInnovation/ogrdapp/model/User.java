package com.osinTechInnovation.ogrdapp.model;

public class User {
    private String email;
    private String foreign_key;
    private String surName;
    private String userId;
    private String username;
    private long hoursOverall;
    private long  hoursToSettle;
    private double paycheck;
    private String entriesAmount;
    private String deviceId;
    private String subsOrderId;

    public User(
            String email,
            String foreign_key,
            String surName,
            String userId,
            String userName,
            long hoursOverall,
            long hoursToSettle,
            double paycheck,
            String entriesAmount,
            String deviceId
    ) {
        this.email = email;
        this.foreign_key = foreign_key;
        this.surName = surName;
        this.userId = userId;
        this.username = userName;
        this.hoursOverall=  hoursOverall;
        this.hoursToSettle = hoursToSettle;
        this.paycheck = paycheck;
        this.entriesAmount = entriesAmount;
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSubsOrderId() {
        return subsOrderId;
    }

    public void setSubsOrderId(String subsOrderId) {
        this.subsOrderId = subsOrderId;
    }

    public String getEntriesAmount() {
        return entriesAmount;
    }

    public void setEntriesAmount(String entriesAmount) {
        this.entriesAmount = entriesAmount;
    }

    public long getHoursOverall() {
        return hoursOverall;
    }

    public void setHoursOverall(long hoursOverall) {
        this.hoursOverall = hoursOverall;
    }

    public long getHoursToSettle() {
        return hoursToSettle;
    }

    public void setHoursToSettle(long hoursToSettle) {
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

    public double getPaycheck() {
        return paycheck;
    }

    public void setPaycheck(double paycheck) {
        this.paycheck = paycheck;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", foreign_key='" + foreign_key + '\'' +
                ", surName='" + surName + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", hoursOverall=" + hoursOverall +
                ", hoursToSettle=" + hoursToSettle +
                ", paycheck=" + paycheck +
                '}';
    }
}
