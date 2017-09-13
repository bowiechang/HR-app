package com.example.admin.workerstatus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AttendanceActivity extends AppCompatActivity implements OnClickListener {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private String[] split;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm aaa");
    private String todayDate = dateformat.format(c.getTime());
    private String time = timeformat.format(c.getTime());
    private int checker = 0;

    private FloatingActionButton fab;

    private DatabaseReference dbrefCheckIn = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initDrawer();

    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }
//
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnAttendance) {
            Log.d("attendance", "reached");
            Intent intent = new Intent(this, AndroidCameraApi.class);
            intent.putExtra("mc", "no mc");
            startActivity(intent);
        }
        else if(v.getId() == R.id.btnMc){
            Intent intent = new Intent(this, AndroidCameraApi.class);
            intent.putExtra("mc", "mc");
            startActivity(intent);
        }

        else if(v.getId() == R.id.btnCheckout){
            Intent intent = new Intent(this, AndroidCameraApi.class);
            intent.putExtra("checkout", "checkout");
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();

        statusCheck();
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(ArrayList arrayList);
    }

    private void pushCheckin(final OnGetDataListener listener){

        //check if it exists first then push

        dbrefCheckIn.orderByChild("date").equalTo(todayDate).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> arrayList = new ArrayList<String>();

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    User user = childDataSnapshot.getValue(User.class);
                    if(user.getName().equals(split[0]) && user.getTime().equals(time)){
                        arrayList.add(childDataSnapshot.getKey());
                        System.out.println(arrayList.size());
                    }
                }
                listener.onSuccess(arrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    Intent i = new Intent(AttendanceActivity.this, UserCalendarActivity.class);
                    startActivity(i);
                }
                else if(position == 4){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signOut();

                    Intent i = new Intent(AttendanceActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void init(){

        final DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("DateStatus");
        final Query querydbref = databaseReferenceUser.orderByChild("date").equalTo(todayDate);
        querydbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Status status = dataSnapshot.getValue(Status.class);
                if (status == null) {
                    Status status2 = new Status("Unchecked", todayDate);
                    databaseReferenceUser.push().setValue(status2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TextView tvName = (TextView) findViewById(R.id.tvName);
        final TextView tvStatus = (TextView) findViewById(R.id.tvStatus);

        final Button btnAttendance = (Button) findViewById(R.id.btnAttendance);
        final Button btnMc = (Button) findViewById(R.id.btnMc);
        final Button btnCheckOut = (Button) findViewById(R.id.btnCheckout);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        btnAttendance.setOnClickListener(this);
        btnMc.setOnClickListener(this);
        btnCheckOut.setOnClickListener(this);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        btnAttendance.setVisibility(View.INVISIBLE);
        btnMc.setVisibility(View.INVISIBLE);
        btnCheckOut.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);

        final ImageView imageView = (ImageView) findViewById(R.id.iv);
        imageView.setVisibility(View.GONE);

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                } // Just catch the InterruptedException

                // Now we use the Handler to post back to the main thread
                handler.post(new Runnable() {
                    public void run() {
                        // Set the View's visibility back on the main UI Thread
                        relativeLayout.setVisibility(View.INVISIBLE);
                        btnAttendance.setVisibility(View.VISIBLE);
                        btnMc.setVisibility(View.VISIBLE);
                        btnCheckOut.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);

                    }
                });
            }
        }).start();

        if(firebaseUser != null){
            split = firebaseUser.getEmail().split("@");
            tvName.setText(split[0]);
        }


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if(checkIn.getDate().equals(todayDate) && checkIn.getName().equals(split[0])) {

                            String checkout = (String)snapshot.child("checkout").getValue();
                            int colorChecked = ContextCompat.getColor(AttendanceActivity.this,R.color.checked2);
                            int colorBtn = ContextCompat.getColor(AttendanceActivity.this,R.color.colorPrimaryDark);

                            //its either on MC or working
                            if(checkIn.getMc().toLowerCase().equals("on mc")){
                                int colormc = ContextCompat.getColor(AttendanceActivity.this,R.color.colorPrimary);
                                tvStatus.setText("MC Attendance taken");
                                tvStatus.setTextColor(colormc);

                                btnAttendance.setBackgroundColor(colorBtn);
                                btnMc.setBackgroundColor(colorBtn);
                                btnMc.setText("Retake mc");
                            }
                            else if(checkout != null){
                                tvStatus.setText("Check Out Successful!");
                                tvStatus.setTextColor(colorBtn);

                                btnAttendance.setBackgroundColor(colorBtn);
                                btnMc.setBackgroundColor(colorBtn);
                                btnCheckOut.setBackgroundColor(colorBtn);
                                btnAttendance.setText("Retake photo");
                            }
                            else{
                                tvStatus.setText("Attendance taken!");
                                tvStatus.setTextColor(colorChecked);

                                btnAttendance.setBackgroundColor(colorBtn);
                                btnMc.setBackgroundColor(colorBtn);
                                btnAttendance.setText("Retake photo");
                            }

                            //reading of image
                            final StorageReference pathref = storageReference.child(todayDate +"/" + "checkin-" + split[0]+".jpg");
                            pathref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("success uri", uri.toString());

                                        // Load the image using Glide
                                        Glide.with(AttendanceActivity.this)
                                                .load(uri.toString())
                                                .into(imageView);


                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("failed uri", "failed: " + e.toString());
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


        Intent intent = getIntent();
        if( getIntent().getExtras() != null)
        {
            String key = intent.getStringExtra("key");
            if (key.equals("fromCam")) {

                pushCheckin(new OnGetDataListener() {

                    @Override
                    public void onSuccess(ArrayList arrayList) {
                        if(arrayList.size() > 1){
                            if(checker == 0) {
                                System.out.println("exist delete it");
                                dbrefCheckIn.child(arrayList.get(0).toString()).removeValue();
                                checker = 1;
                            }
                        }
                    }
                });


            }
        }
    }

}
