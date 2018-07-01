package com.example.admin.workerstatus;

/**
 * Created by admin on 28/9/17.
 */

public class Hours {

    private String name;
    private String month;
    private String normal;
    private String overtime;
    private String overtime2;

    public Hours(){}

    public Hours(String name, String month, String normal, String overtime, String overtime2){
        this.name = name;
        this.month = month;
        this.normal = normal;
        this.overtime = overtime;
        this.overtime2 = overtime2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getOvertime() {
        return overtime;
    }

    public void setOvertime(String overtime) {
        this.overtime = overtime;
    }

    public String getOvertime2() {
        return overtime2;
    }

    public void setOvertime2(String overtime2) {
        this.overtime2 = overtime2;
    }
}
