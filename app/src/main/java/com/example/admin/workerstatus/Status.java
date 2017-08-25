package com.example.admin.workerstatus;

/**
 * Created by admin on 21/8/17.
 */

public class Status {

    private String status;
    private String date;

    public Status(){}

    public Status(String status, String date){
        this.status = status;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
