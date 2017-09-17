package com.example.admin.workerstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserCalendarActivity extends AppCompatActivity {

    private OneCalendarView calendarView;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
    private RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private List<String> listForRV;
    private HashMap<String, String> hashMap;
    private FloatingActionButton fab;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private String[] split;
    private String name;

    private TextView tvWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_calendar);

        calendarView = (OneCalendarView) findViewById(R.id.oneCalendar);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        tvWarning = (TextView) findViewById(R.id.tvWarning);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fab = (FloatingActionButton)findViewById(R.id.fab);

        if(firebaseUser != null){
            split = firebaseUser.getEmail().split("@");
            name = split[0];
        }

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
                            if(checkIn.getName().equals(name)) {
                                if (!listForRV.contains(checkIn.getDate())) {
                                    listForRV.add(checkIn.getDate());
                                }
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
                    recyclerView.setVisibility(View.GONE);
                    tvWarning.setVisibility(View.VISIBLE);
                }
                else{
                    recyclerView.setVisibility(View.VISIBLE);
                    tvWarning.setVisibility(View.GONE);
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

        UserCalendarAdapter userCalendarAdapter = new UserCalendarAdapter(UserCalendarActivity.this, listForRV);
        recyclerView.setAdapter(userCalendarAdapter);


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

                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String formatted_day = df.format(day.getDate());
                String [] split = formatted_day.split("-");

                String daykey = split[0];
                String monthkey = split[1];

                String datekey = daykey + "-" + monthkey + "-" + calendarView.getYear();

                Intent i = new Intent(UserCalendarActivity.this, DetailedUserActivity.class);
                Bundle extras = new Bundle();
                extras.putString("date", datekey);
                extras.putString("name", name);
                i.putExtras(extras);
                startActivity(i);

            }

            @Override
            public void dateOnLongClick(Day day, int position) {

            }
        });
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
                .setTextSecondary("Welcome and have a nice day!")
        );

        drawer.addDivider();

        drawer.addItem(new DrawerItem()
                .setImage(ContextCompat.getDrawable(this, R.drawable.drawercamera))
                .setTextPrimary("Take Attendance")
                .setTextSecondary("Remember to take your attendance every day!")
        );

        drawer.addItem(new DrawerItem()
                .setImage(ContextCompat.getDrawable(this, R.drawable.drawercalendar))
                .setTextPrimary("My Past Working Days")
                .setTextSecondary("View all the days you have worked!")
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
                //2 view cal
                //3 gen report
                //4 logout

                if(position == 2){
                    Intent i = new Intent(UserCalendarActivity.this, AttendanceActivity.class);
                    startActivity(i);
                }
                else if(position == 3){
                    drawerLayout.closeDrawer(drawer);
                }
                else if(position == 4){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signOut();

                    Intent i = new Intent(UserCalendarActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }



}
