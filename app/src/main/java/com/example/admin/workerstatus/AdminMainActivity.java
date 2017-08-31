package com.example.admin.workerstatus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.darwindeveloper.onecalendar.clases.Day;
import com.darwindeveloper.onecalendar.views.OneCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private OneCalendarView calendarView;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
    private RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private List<String> listForRV;
    private HashMap<String, String> hashMap;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        calendarView = (OneCalendarView) findViewById(R.id.oneCalendar);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fab = (FloatingActionButton)findViewById(R.id.fab);

        listForRV = new ArrayList<>();
        hashMap = new HashMap<>();
        initCalendar();
        initDrawer();
    }

    private void readForRv(final int monthkey){

        if(!listForRV.isEmpty()){
            listForRV.clear();
        }

        //this is the retrieval of location tracker
//        databaseReference2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    if(user != null) {
//                        //check if it matches monthkey
//                        if(getMonth(user.getDate()).equals("0" +(monthkey+1)) && getYear(user.getDate()).equals(calendarView.getYear() + "")) {
//                            insertSQLiteLocationTracking(user);
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

        databaseReference.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        //check if it matches monthkey
                        if(getMonth(checkIn.getDate()).equals("0" +(monthkey+1)) && getYear(checkIn.getDate()).equals(calendarView.getYear() + "")) {
                            if (!listForRV.contains(checkIn.getDate())) {
                                listForRV.add(checkIn.getDate());
                            }
                            System.out.println(checkIn.getName());
                        }
//                        JSONObject json = new JSONObject((Map)snapshot.getValue());
//                        System.out.println("objjson values: " + json.toString());
//                        try {
////                            System.out.println("obj val1" + json.getJSONObject("name").toString());
//                            System.out.println("obj val2" + json.get("name").toString());
//                            System.out.println("obj val3" + json.getString("name"));
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }

                getList();

                System.out.println(hashMap.size() + "size of hm");

                if(listForRV.isEmpty()){
                    Toast.makeText(AdminMainActivity.this, "empty list", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getMonth(String date){
        String[] split = date.split("-");
        return split[1];
    }

    private String getYear(String date){
        String[] split = date.split("-");
        return split[2];
    }

    private void getList(){

        AdminMainAdapter adminMainAdapter = new AdminMainAdapter(AdminMainActivity.this, listForRV);
        recyclerView.setAdapter(adminMainAdapter);
    }


    private void initCalendar() {

        readForRv(calendarView.getMonth());

        calendarView.setOnCalendarChangeListener(new OneCalendarView.OnCalendarChangeListener() {

            @Override
            public void prevMonth() {
                readForRv(calendarView.getMonth());
            }

            @Override
            public void nextMonth() {
                readForRv(calendarView.getMonth());
            }
        });

        calendarView.setOneCalendarClickListener(new OneCalendarView.OneCalendarClickListener() {


            @Override
            public void dateOnClick(Day day, int position) {
                //recuerde que en java los meses inician desde 0
                Date date = day.getDate();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String formatted_day = df.format(day.getDate());
                String [] split = formatted_day.split("-");

                String daykey = split[0];
                String monthkey = split[1];

                String datekey = daykey + "-" + monthkey + "-" + calendarView.getYear();

                Intent intent = new Intent(AdminMainActivity.this, DetailedDateActivity.class);
                Bundle extras = new Bundle();
                extras.putString("date", datekey);
                intent.putExtras(extras);
                startActivity(intent);

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

    private void insertSQLiteCheckIn(CheckIn checkIn){

        SqliteController sqliteController = new SqliteController(this);
        sqliteController.insertCheckInRecords(checkIn);
    }

    private void insertSQLiteLocationTracking(User user){

        SqliteController sqliteController = new SqliteController(this);
        sqliteController.insertLocationTrackingRecords(user);
    }

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

        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
        String filename="attendance.xls";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = {"bezbowie@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));

    }

    private void sentReportWithGmailBg(){

        String filename="attendance.xls";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
        Uri path = Uri.fromFile(filelocation);

        BackgroundMail.newBuilder(this)
                .withUsername("bv.master01@gmail.com")
                .withPassword("bv123456")
                .withMailto("bezbowie@gmail.com")
                .withType(BackgroundMail.TYPE_PLAIN)
                .withAttachments("attendance.xls")
                .withSubject("this is the subject")
                .withBody("this is the body")
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Toast.makeText(AdminMainActivity.this, "done mail", Toast.LENGTH_SHORT).show();
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Toast.makeText(AdminMainActivity.this, "failed mail", Toast.LENGTH_SHORT).show();
                    }
                })
                .send();

    }

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
                drawer.selectItem(2);
            }
        });

        drawer.setOnItemClickListener(new DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(DrawerItem item, long id, int position) {
                drawer.selectItem(position);
                //2 view cal
                //3 gen report
                //4 logout

                if(position == 2){
                    drawerLayout.closeDrawer(drawer);
                }
                else if(position == 3){
                    Intent i = new Intent(AdminMainActivity.this, AdminCalendarActivity.class);
                    startActivity(i);
                }
                else if(position == 4){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signOut();

                    Intent i = new Intent(AdminMainActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }



}
