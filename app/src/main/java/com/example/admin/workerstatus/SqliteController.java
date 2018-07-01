package com.example.admin.workerstatus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 27/8/17.
 */

public class SqliteController extends SQLiteOpenHelper{
    private static final String LOGCAT = null;
    private SQLiteDatabase database = this.getWritableDatabase();

    public SqliteController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
        Log.d(LOGCAT,"Created");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE Attendance (UserID Text, EMPNo Text, Name TEXT, WPNo TEXT, Position TEXT, Nationality TEXT, WP Text, '01' TEXT, '02' TEXT, '03' TEXT, '04' TEXT, '05' TEXT, '06' TEXT," +
                "'07' TEXT, '08' TEXT, '09' TEXT, '10' TEXT, '11' TEXT, '12' TEXT, '13' TEXT, '14' TEXT, '15' TEXT, '16' TEXT, '17' TEXT," +
                "'18' TEXT, '19' TEXT, '20' TEXT, '21' TEXT, '22' TEXT, '23' TEXT, '24' TEXT, '25' TEXT, '26' TEXT, '27' TEXT, '28' TEXT," +
                "'29' TEXT, '30' TEXT, '31' TEXT, Normal TEXT, '1.5' TEXT, '2' TEXT)");
        db.execSQL("CREATE TABLE LocationTracking (Date TEXT, Name TEXT, Time TEXT, Address TEXT)");

        Log.d(LOGCAT, "onCreate reached");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       onCreate(db);
    }

//    public void insertRecords(HashMap<String, String> queryValues) {
//
//        SQLiteDatabase database = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("Name", queryValues.get("name"));
//        database.insert("Attendance", null, values);
//        database.close();
//    }

    public void insertCheckInRecords(CheckIn checkIn, String hours, Account account, Hours hour) {

//        String flag = "";
//        if(checkIn.getFlag().equals(true)){
//            flag = "true";
//        }
//        else if(checkIn.getFlag().equals(false))
//            flag = "false";
//        }

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String [] split = checkIn.getDate().split("-");
        String date = split[0];
//        System.out.println(date + ":date vlaue");

        for(int i = 1; i <= 31; i ++){
            if(i == Integer.parseInt(date)){
                if(hours!=null){
                    values.put("'" + date + "'", hours);
                }
                else{
                    values.put("'" + date + "'", "0");
                }
            }
            else{
                String i2 = "";
                if(i < 10){
                    i2 = "0" + i;
                    System.out.println("i2 = " + i2);
                    values.put("'" + i2 + "'", "0");
                }
                else{
                    i2 = i + "";
                    System.out.println("i2 = " + i2);
                    values.put("'" + i2 + "'", "0");
                }

            }
        }

//
//        if(hours!=null) {
//            values.put("'" + date + "'", hours);
//            System.out.println("date: " + date);
//            System.out.println("hours: " + hours);
//        }
//        else if(hours == null){
//            values.put("'" + date + "'", "0");
//        }

        values.put("UserID", account.getUserid());
        values.put("EMPNo", account.getEmpNo());
        values.put("Name", account.getName());
        values.put("WPNo", account.getWpNo());
        values.put("Position", account.getPosition());
        values.put("Nationality", account.getNationality());
        values.put("WP", account.getWp());
        values.put("Normal", hour.getNormal());
        values.put("'1.5'", hour.getOvertime());
        values.put("'2'", hour.getOvertime2());
//        values.put("CheckIn", checkIn.getCheckin());
//        values.put("Flag", flag);
//        values.put("Status", checkIn.getMc());
//
//        //
//        values.put("Day", checkIn.getDay());
//        values.put("Duration", hours);
//        values.put("CheckOut", checkout);
        //
        database.insert("Attendance", null, values);
        database.close();
    }

    public void insertCheckInRecordsLocation(CheckIn checkIn, String hours, Account account, Hours hour) {

        String flag = "";
//        if(checkIn.getFlag().equals(true)){
//            flag = "true";
//        }
//        else if(checkIn.getFlag().equals(false)){
//            flag = "false";
//        }

        String [] split = checkIn.getDate().split("-");
        String date = split[0];
//        System.out.println(date + ":date vlaue");

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

//        if(hours!=null) {
//            values.put("'" + date + "'", location);
//            System.out.println("date: " + date);
//            System.out.println("hours: " + hours);
//        }
//        else if(hours == null){
//            values.put("'" + date + "'", "NA");
//        }


        for(int i = 1; i <= 31; i ++){
            if(i == Integer.parseInt(date)){
                if(hours!=null){
                    values.put("'" + date + "'", checkIn.getLocation());
                }
                else{
                    values.put("'" + date + "'", "NA");
                }
            }
            else{
                String i2 = "";
                if(i < 10){
                    i2 = "0" + i;
                    System.out.println("i2 = " + i2);
                    values.put("'" + i2 + "'", "NA");
                }
                else{
                    i2 = i + "";
                    System.out.println("i2 = " + i2);
                    values.put("'" + i2 + "'", "NA");
                }

            }
        }

        values.put("UserID", account.getUserid());
        values.put("EMPNo", account.getEmpNo());
        values.put("Name", account.getName());
        values.put("WPNo", account.getWpNo());
        values.put("Position", account.getPosition());
        values.put("Nationality", account.getNationality());
        values.put("WP", account.getWp());
//        values.put("CheckIn", checkIn.getCheckin());
//        values.put("Flag", flag);
//        values.put("Status", checkIn.getMc());
//
//        //
//        values.put("Day", checkIn.getDay());
//        values.put("Duration", hours);
//        values.put("CheckOut", checkout);
        //
        database.insert("Attendance", null, values);
        database.close();
    }

    public void insertLocationTrackingRecords(User user) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Date", user.getDate());
        values.put("Name", user.getName());
        values.put("Time", user.getTime());
        values.put("Address", user.getAddress());
        database.insert("LocationTracking", null, values);
        database.close();
    }

    public ArrayList<HashMap<String, String>> getAllRecord() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM Attendance";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Date", cursor.getString(0));
                map.put("Name", cursor.getString(1));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    public void deleteDB(){
        String q1, q2;
        q1 = "DROP TABLE IF EXISTS Attendance";
        q2 = "DROP TABLE IF EXISTS LocationTracking";
        database.execSQL(q1);
        database.execSQL(q2);
    }

    public void createDB(){
        database.execSQL("CREATE TABLE Attendance (UserID Text, EMPNo Text, Name TEXT, WPNo TEXT, Position TEXT, Nationality TEXT, WP Text, '01' TEXT, '02' TEXT, '03' TEXT, '04' TEXT, '05' TEXT, '06' TEXT," +
                "'07' TEXT, '08' TEXT, '09' TEXT, '10' TEXT, '11' TEXT, '12' TEXT, '13' TEXT, '14' TEXT, '15' TEXT, '16' TEXT, '17' TEXT," +
                "'18' TEXT, '19' TEXT, '20' TEXT, '21' TEXT, '22' TEXT, '23' TEXT, '24' TEXT, '25' TEXT, '26' TEXT, '27' TEXT, '28' TEXT," +
                "'29' TEXT, '30' TEXT, '31' TEXT, Normal TEXT, '1.5' TEXT, '2' TEXT)");
        database.execSQL("CREATE TABLE LocationTracking (Date TEXT, Name TEXT, Time TEXT, Address TEXT)");
    }

}
