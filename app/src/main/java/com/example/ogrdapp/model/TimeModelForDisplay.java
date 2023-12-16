package com.example.ogrdapp.model;

import com.google.firebase.Timestamp;

public class TimeModelForDisplay {
    private String userName;
    private String userSurname;
    private String timeBegin;
    private String timeEnd;
    private String timeOverall;
    private boolean moneyOverall;
    private String id;
    private long timeOverallInLong;
    private Timestamp timeAdded;
    private int withdrawnMoney;
    private String documentId;
    private long timeOverallInLongLefToSettle;

    public TimeModelForDisplay(String userName, String userSurname, String timeBegin, String timeEnd, String timeOverall,
                     boolean moneyOverall, String id, long timeOverallInLong, Timestamp timeAdded,int withdrawnMoney,String documentId,
                               long timeOverallInLongLefToSettle) {
        this.userName = userName;
        this.userSurname = userSurname;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.timeOverall = timeOverall;
        this.moneyOverall = moneyOverall;
        this.id = id;
        this.timeOverallInLong = timeOverallInLong;
        this.timeAdded = timeAdded;
        this.withdrawnMoney = withdrawnMoney;
        this.documentId = documentId;
        this.timeOverallInLongLefToSettle = timeOverallInLongLefToSettle;
    }


    public TimeModelForDisplay() {
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeOverallInLong() {
        return timeOverallInLong;
    }

    public void setTimeOverallInLong(long timeOverallInLong) {
        this.timeOverallInLong = timeOverallInLong;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public String getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(String timeBegin) {
        this.timeBegin = timeBegin;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getTimeOverall() {
        return timeOverall;
    }

    public void setTimeOverall(String timeOverall) {
        this.timeOverall = timeOverall;
    }

    public boolean getMoneyOverall() {
        return moneyOverall;
    }

    public void setMoneyOverall( boolean moneyOverall) {
        this.moneyOverall = moneyOverall;
    }

    public int getWithdrawnMoney() {
        return withdrawnMoney;
    }

    public void setWithdrawnMoney(int withdrawnMoney) {
        this.withdrawnMoney = withdrawnMoney;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public boolean isMoneyOverall() {
        return moneyOverall;
    }

    public long getTimeOverallInLongLefToSettle() {
        return timeOverallInLongLefToSettle;
    }

    public void setTimeOverallInLongLefToSettle(long timeOverallInLongLefToSettle) {
        this.timeOverallInLongLefToSettle = timeOverallInLongLefToSettle;
    }
}
