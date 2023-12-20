package com.example.ogrdapp.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

public class TimeModel  implements Parcelable {
    private String userName;
    private String userSurname;
    private String timeBegin;
    private String timeEnd;
    private String timeOverall;
    private boolean moneyOverall;
    private String id;
    private long timeOverallInLong;
    private Timestamp timeAdded;
    private double withdrawnMoney;
    private String documentId;


    public TimeModel(String userName, String userSurname, String timeBegin, String timeEnd, String timeOverall,
                     boolean moneyOverall, String id, long timeOverallInLong, Timestamp timeAdded,double withdrawnMoney,String documentId) {
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
    }



    public TimeModel(Parcel in) {
        this.userName= in.readString();
        this.userSurname = in.readString();
        this.timeBegin = in.readString();
        this.timeEnd = in.readString();
        this.timeOverall = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.moneyOverall = in.readBoolean();
        }
        this.id= in.readString();
        this.timeOverallInLong = in.readLong();
        this.timeAdded = in.readParcelable(Timestamp.class.getClassLoader());
        this.withdrawnMoney = in.readDouble();
        this.documentId = in.readString();
    }

    public TimeModel() {
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

    public double getWithdrawnMoney() {
        return withdrawnMoney;
    }

    public void setWithdrawnMoney(double withdrawnMoney) {
        this.withdrawnMoney = withdrawnMoney;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {

        parcel.writeString(userName);
        parcel.writeString(userSurname);
        parcel.writeString(timeBegin);
        parcel.writeString(timeEnd);
        parcel.writeString(timeOverall);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(moneyOverall);
        }
        parcel.writeString(id);
        parcel.writeLong(timeOverallInLong);
        parcel.writeParcelable(timeAdded,i);
        parcel.writeDouble(withdrawnMoney);
        parcel.writeString(documentId);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TimeModel createFromParcel(Parcel in) {
            return new TimeModel(in);
        }

        public TimeModel[] newArray(int size) {
            return new TimeModel[size];
        }
    };
}
