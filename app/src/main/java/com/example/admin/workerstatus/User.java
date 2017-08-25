package com.example.admin.workerstatus;

public class User {

    private String latlng;
    private String time;
    private String name;
    private String date;
    private String address;

    public User(){}

    public User(String latlng, String time, String name, String date, String address){
        this.latlng = latlng;
        this.time = time;
        this.name = name;
        this.date = date;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
