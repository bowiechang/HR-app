package com.example.admin.workerstatus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailedDateActivity extends AppCompatActivity implements OnClickListener {

    private String datekey, date, status2;
    private RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private List<CheckIn> list;
    private Button btn;
    private TextView tvDate, tvStatus;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("DateStatus");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_date);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            datekey = getIntent().getExtras().getString("date");
        }

        init();
        getDateAndStatus();
        read();




    }

    private void getList(DataSnapshot dataSnapshot){

        DetailedDateAdapter detailedDateAdapter = new DetailedDateAdapter(DetailedDateActivity.this, list);
        recyclerView.setAdapter(detailedDateAdapter);
    }

    @Override
    public void onClick(View v) {

        if(btn.getText().toString().toLowerCase().equals("approve attendance")) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DateStatus");
            databaseReference.orderByChild("date").equalTo(date).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Status status = new Status("Checked", date);
                    String key = dataSnapshot.getKey();
                    databaseReference.child(key).setValue(status);
                    Toast.makeText(DetailedDateActivity.this, "Attendance status has been set to approved!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(DetailedDateActivity.this, AdminDateActivity.class);
                    startActivity(i);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if(btn.getText().toString().toLowerCase().equals("uncheck attendance")){
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DateStatus");
            databaseReference.orderByChild("date").equalTo(date).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Status status = new Status("Unchecked", date);
                    String key = dataSnapshot.getKey();
                    databaseReference.child(key).setValue(status);
                    Toast.makeText(DetailedDateActivity.this, "Attendance status has been unchecked!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(DetailedDateActivity.this, AdminDateActivity.class);
                    startActivity(i);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void init(){

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.MyAppbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvDate = (TextView)findViewById(R.id.tvDate);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        btn = (Button)findViewById(R.id.btnCheck);
        btn.setOnClickListener(this);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        btn.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.INVISIBLE);
        appBarLayout.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                } // Just catch the InterruptedException

                // Now we use the Handler to post back to the main thread
                handler.post(new Runnable() {
                    public void run() {
                        // Set the View's visibility back on the main UI Thread
                        relativeLayout.setVisibility(View.INVISIBLE);
                        appBarLayout.setVisibility(View.VISIBLE);
                        toolbar.setVisibility(View.VISIBLE);
                        btn.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void getDateAndStatus(){

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Status status = snapshot.getValue(Status.class);
                    if(status != null) {
                        if(status.getDate().equals(datekey)) {
                            date = status.getDate();
                            status2 = status.getStatus();

                            tvDate.setText(date);
                            tvStatus.setText(status2);

                            if(status2.equals("Checked")){
                                btn.setText("Uncheck attendance");
                                tvStatus.setTextColor(Color.GREEN);
                            }
                            else{
                                tvStatus.setTextColor(Color.RED);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void read(){

        list = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if(checkIn.getDate().equals(date)) {
                            list.add(checkIn);
                            getList(snapshot);
                        }
                    }
                }

                if(list.isEmpty()){
                    Toast.makeText(DetailedDateActivity.this, "There is working days", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
