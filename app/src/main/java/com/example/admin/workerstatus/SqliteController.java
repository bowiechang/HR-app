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

        db.execSQL("CREATE TABLE Attendance (Name TEXT, Date TEXT, Day TEXT, CheckIn TEXT, CheckOut TEXT, Flag TEXT, Status TEXT, Duration TEXT)");
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

    public void insertCheckInRecords(CheckIn checkIn, String hours, String checkout) {

        String flag = "";
        if(checkIn.getFlag().equals(true)){
            flag = "true";
        }
        else if(checkIn.getFlag().equals(false)){
            flag = "false";
        }

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Date", checkIn.getDate());
        values.put("Name", checkIn.getName());
        values.put("CheckIn", checkIn.getCheckin());
        values.put("Flag", flag);
        values.put("Status", checkIn.getMc());

        //
        values.put("Day", checkIn.getDay());
        values.put("Duration", hours);
        values.put("CheckOut", checkout);
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
        database.execSQL("CREATE TABLE Attendance (Name TEXT, Date TEXT, Day TEXT, CheckIn TEXT, CheckOut TEXT, Flag TEXT, Status TEXT, Duration TEXT)");
        database.execSQL("CREATE TABLE LocationTracking (Date TEXT, Name TEXT, Time TEXT, Address TEXT)");
    }




}
