package com.example.admin.workerstatus;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.darwindeveloper.onecalendar.clases.Day;
import com.darwindeveloper.onecalendar.views.OneCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AdminCalendarActivity extends AppCompatActivity implements OnClickListener {

    private OneCalendarView calendarView;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
    private DatabaseReference databaseReference3 = FirebaseDatabase.getInstance().getReference().child("User");
    private DatabaseReference databaseReference4 = FirebaseDatabase.getInstance().getReference().child("Hours");
    private FloatingActionButton fab;
    private Button btnGenerateReportForAll, btnGenerateReportForUser;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    private int monthkey;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_generatereport);

        calendarView = (OneCalendarView) findViewById(R.id.oneCalendar);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        btnGenerateReportForAll = (Button) findViewById(R.id.btnGenerateForAll);
        btnGenerateReportForUser = (Button) findViewById(R.id.btnGenerateForUser);

        btnGenerateReportForAll.setOnClickListener(this);
        btnGenerateReportForUser.setOnClickListener(this);
        initCalendar();
        initDrawer();

    }

    private String getMonth(String date){
        String[] split = date.split("-");
        return split[1];
    }

    private String getYear(String date){
        String[] split = date.split("-");
        return split[2];
    }


    private void initCalendar() {

        monthkey = calendarView.getMonth();

        calendarView.setOnCalendarChangeListener(new OneCalendarView.OnCalendarChangeListener() {

            @Override
            public void prevMonth() {
                monthkey = calendarView.getMonth();
            }

            @Override
            public void nextMonth() {
                monthkey = calendarView.getMonth();
            }
        });

        calendarView.setOneCalendarClickListener(new OneCalendarView.OneCalendarClickListener() {


            @Override
            public void dateOnClick(Day day, int position) {
                //recuerde que en java los meses inician desde 0
                Date date = day.getDate();
                int year = date.getYear();
                int month = date.getMonth();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int numDay = cal.get(Calendar.DAY_OF_MONTH);
                Toast.makeText(AdminCalendarActivity.this, numDay + " " + calendarView.getStringMonth(month) + " " + year, Toast.LENGTH_SHORT).show();

                System.out.println("pos= " + position);

                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                System.out.println("day vals: " + df.format(day.getDate()));

                day.setBackgroundColor(Color.parseColor("#fad501"));

            }

            @Override
            public void dateOnLongClick(Day day, int position) {

            }
        });
    }

    private void setSQLite(){

        SqliteController sqliteController = new SqliteController(this);
        sqliteController.deleteDB();
        sqliteController.createDB();
    }

    private void insertSQLiteCheckInForAll(final int monthkey){

        final SqliteController sqliteController = new SqliteController(this);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        //check if it matches monthkey
                        if(getMonth(checkIn.getDate()).equals("0" +(monthkey+1)) && getYear(checkIn.getDate()).equals(calendarView.getYear() + "")) {

                            final String hours = snapshot.child("hours").getValue().toString();
                            final String hours2 = String.valueOf(hours);

                            databaseReference3.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        final Account account = snapshot.getValue(Account.class);
                                        if (account != null) {
                                            if(account.getUserid().equals(checkIn.getName())) {

                                                databaseReference4.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                                            Hours hour = snapshot1.getValue(Hours.class);
                                                            System.out.println("monthchecker"+"0"+(monthkey+1) + "-" + calendarView.getYear());
                                                            if(hour.getName().equals(checkIn.getName()) && hour.getMonth().equals("0"+(monthkey+1) + "-" + calendarView.getYear())){
                                                                System.out.println("monthchecker"+"0"+(monthkey+1) + "-" + calendarView.getYear());
                                                                sqliteController.insertCheckInRecords(checkIn, hours, account, hour);
                                                                sqliteController.insertCheckInRecordsLocation(checkIn, hours, account, hour);
                                                            }

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                break;
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void insertSQLiteCheckInForUser(final int monthkey, final String wpNo){

        final SqliteController sqliteController = new SqliteController(this);

        databaseReference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Account account= snapshot.getValue(Account.class);
                    if(account != null) {
                      if(account.getWpNo().equals(wpNo)){

                          String name = account.getUserid();
                          System.out.println("name: " + name);

                          databaseReference.orderByChild("name").equalTo(name).addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot) {

                                  for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                      final CheckIn checkIn = snapshot.getValue(CheckIn.class);
                                      if(checkIn != null) {
                                          //check if it matches monthkey
                                          if(getMonth(checkIn.getDate()).equals("0" +(monthkey+1)) && getYear(checkIn.getDate()).equals(calendarView.getYear() + "")) {

                                              final Double hours = (Double) snapshot.child("hours").getValue();
                                              final String hours2 = String.valueOf(hours);

                                              databaseReference3.addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(DataSnapshot dataSnapshot) {
                                                      for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                          Account account = snapshot.getValue(Account.class);
                                                          if (account != null) {
                                                              if(account.getUserid().equals(checkIn.getName())) {
//                                                                  sqliteController.insertCheckInRecords(checkIn, hours2, account);
//                                                                  sqliteController.insertCheckInRecordsLocation(checkIn, hours2, account);
                                                                  break;
                                                              }
                                                          }
                                                      }
                                                  }

                                                  @Override
                                                  public void onCancelled(DatabaseError databaseError) {

                                                  }
                                              });


                                          }
                                      }
                                  }
                              }

                              @Override
                              public void onCancelled(DatabaseError databaseError) {

                              }
                          });
                      }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

//    private void insertSQLiteLocationTrackingForAll(final int monthkey){
//
//        final SqliteController sqliteController = new SqliteController(this);
//
//
//        databaseReference2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    if(user != null) {
//                        //check if it matches monthkey
//                        if(getMonth(user.getDate()).equals("0" +(monthkey+1)) && getYear(user.getDate()).equals(calendarView.getYear() + "")) {
//                            sqliteController.insertLocationTrackingRecords(user);
//                            System.out.println(user + "user results");
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    private void insertSQLiteLocationTrackingForUser(final int monthkey, final String namekey){
//
//        final SqliteController sqliteController = new SqliteController(this);
//
//        databaseReference2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    if(user != null) {
//                        //check if it matches monthkey
//                        if(getMonth(user.getDate()).equals("0" +(monthkey+1)) && getYear(user.getDate()).equals(calendarView.getYear() + "")) {
//                            if(user.getName().equals(namekey)) {
//                                sqliteController.insertLocationTrackingRecords(user);
//                                System.out.println(user + "user results");
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void getSQLiteValues(){
        SqliteController sqliteController = new SqliteController(this);
        sqliteController.getAllRecord();

        exportSQLtoExcel();
    }

    private void exportSQLtoExcel(){

        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File file = new File(directory_path);
        file.mkdir();
        // Export SQLite DB as EXCEL FILE
        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(getApplicationContext(), "androidsqlite.db", directory_path);
        sqliteToExcel.exportAllTables("attendance.xls", new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {
                Log.d("export", "start");
            }

            @Override
            public void onCompleted(String filePath) {
                Log.d("export", "completed");
                Log.d("fp", filePath);
//                sendReport(filePath);
//                sentReportWithGmailBg();
            }

            @Override
            public void onError(Exception e) {
                Log.d("export", "error");
            }
        });
    }

    private void sendReport(){

        if(firebaseUser != null){
            email = firebaseUser.getEmail();
        }

        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
        String filename="attendance.xls";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = { email };
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));

    }

    //automatic but have to investigate more
//    private void sentReportWithGmailBg(){
//
//        String filename="attendance.xls";
//        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
//        Uri path = Uri.fromFile(filelocation);
//
//        BackgroundMail.newBuilder(this)
//                .withUsername("bv.master01@gmail.com")
//                .withPassword("bv123456")
//                .withMailto("bezbowie@gmail.com")
//                .withType(BackgroundMail.TYPE_PLAIN)
//                .withAttachments("attendance.xls")
//                .withSubject("this is the subject")
//                .withBody("this is the body")
//                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
//                    @Override
//                    public void onSuccess() {
//                        //do some magic
//                        Toast.makeText(AdminCalendarActivity.this, "done mail", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
//                    @Override
//                    public void onFail() {
//                        //do some magic
//                        Toast.makeText(AdminCalendarActivity.this, "failed mail", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .send();
//
//    }

    private void initDrawer(){

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        final DrawerView drawer = (DrawerView) findViewById(R.id.drawer);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        drawerLayout.addDrawerListener(drawerToggle);
        drawerLayout.closeDrawer(drawer);

        drawer.selectItem(3);
        drawer.addItem(new DrawerItem()
                .setTextPrimary("Navigation")
                .setTextSecondary("Welcome admin")
        );

        drawer.addDivider();

        drawer.addItem(new DrawerItem()
                .setImage(ContextCompat.getDrawable(this, R.drawable.drawercalendar))
                .setTextPrimary("View Calendar")
                .setTextSecondary("view all daily records")
        );

        drawer.addItem(new DrawerItem()
                .setImage(ContextCompat.getDrawable(this, R.drawable.drawergenerate))
                .setTextPrimary("Generate Report")
                .setTextSecondary("generate reports and email it out")
        );

        drawer.addItem(new DrawerItem()
                .setImage(ContextCompat.getDrawable(this, R.drawable.drawerlogout))
                .setTextPrimary("Logout")
        );

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                drawerLayout.openDrawer(drawer);
                drawer.selectItem(3);
            }
        });

        drawer.setOnItemClickListener(new DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(DrawerItem item, long id, int position) {
                drawer.selectItem(position);
                Toast.makeText(AdminCalendarActivity.this, "Clicked item #" + position, Toast.LENGTH_SHORT).show();

                //2 view cal
                //3 gen report
                //4 logout

                if(position == 2){
                    Intent i = new Intent(AdminCalendarActivity.this, AdminMainActivity.class);
                    startActivity(i);
                }
                else if(position == 3){
                    drawerLayout.closeDrawer(drawer);
                }
                else if(position == 4){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signOut();

                    Intent i = new Intent(AdminCalendarActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnGenerateForAll:
                Toast.makeText(AdminCalendarActivity.this, "Generating, please hold on", Toast.LENGTH_SHORT).show();

                setSQLite();
                insertSQLiteCheckInForAll(monthkey);
//                insertSQLiteLocationTrackingForAll(monthkey);

                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        } // Just catch the InterruptedException

                        // Now we use the Handler to post back to the main thread
                        handler.post(new Runnable() {
                            public void run() {
                                // Set the View's visibility back on the main UI Thread
                                exportSQLtoExcel();
                                sendReport();
                            }
                        });
                    }
                }).start();
                break;

            case R.id.btnGenerateForUser:
                showChangeLangDialog();
                break;

        }
    }

    private void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.customdialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

        dialogBuilder.setTitle("Generate Report For User");
        dialogBuilder.setPositiveButton("Generate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                setSQLite();

                String wpno = edt.getText().toString().toLowerCase();
                String editedwp = wpno.replace(" ", "");

                Toast.makeText(AdminCalendarActivity.this, "Generating, please hold on", Toast.LENGTH_SHORT).show();
                insertSQLiteCheckInForUser(monthkey, editedwp);
//                insertSQLiteLocationTrackingForUser(monthkey, editedname);

                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        } // Just catch the InterruptedException

                        // Now we use the Handler to post back to the main thread
                        handler.post(new Runnable() {
                            public void run() {
                                // Set the View's visibility back on the main UI Thread
                                exportSQLtoExcel();
                                sendReport();
                            }
                        });
                    }
                }).start();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onBackPressed() {
    }
}
