package com.example.admin.workerstatus;

/**
 * Created by admin on 22/8/17.
 */

public class CheckIn {

    private String name;
    private String checkin;
    private String date;
    private String mc;
    private Boolean flag;
    private String day;
    private String location;

    public CheckIn(){}

    public CheckIn(String name, String checkin, String date, String mc, Boolean flag, String day, String location){
        this.name = name;
        this.checkin = checkin;
        this.date = date;
        this.mc = mc;
        this.flag = flag;
        this.day = day;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getMc() {
        return mc;
    }

    public void setMc(String mc) {
        this.mc = mc;
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

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }
}
