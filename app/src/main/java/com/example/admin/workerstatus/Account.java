package com.example.admin.workerstatus;

/**
 * Created by admin on 18/9/17.
 */

public class Account {

    private String userid;
    private String empNo;
    private String name;
    private String wpNo;
    private String position;
    private String nationality;
    private String wp;

    public Account(){}

    public Account(String userid, String empNo, String name, String wpNo, String position, String nationality, String wp){
        this.userid = userid;
        this.empNo = empNo;
        this.name = name;
        this.wpNo = wpNo;
        this.position = position;
        this.nationality = nationality;
        this.wp = wp;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWpNo() {
        return wpNo;
    }

    public void setWpNo(String wpNo) {
        this.wpNo = wpNo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getWp() {
        return wp;
    }

    public void setWp(String wp) {
        this.wp = wp;
    }
}
