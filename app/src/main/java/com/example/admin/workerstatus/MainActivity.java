package com.example.admin.workerstatus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import br.com.safety.locationlistenerhelper.core.LocationTracker;

import static com.example.admin.workerstatus.R.id.iv;

public class MainActivity extends AppCompatActivity implements OnClickListener {

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

    private DatabaseReference dbrefCheckIn = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        final Button btnAttendance = (Button) findViewById(R.id.btnAttendance);
        final Button btnMc = (Button) findViewById(R.id.btnMc);
        final ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        btnAttendance.setOnClickListener(this);
        btnMc.setOnClickListener(this);
        imageButton.setOnClickListener(this);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        btnAttendance.setVisibility(View.INVISIBLE);
        btnMc.setVisibility(View.INVISIBLE);

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
                    }
                });
            }
        }).start();

        if(firebaseUser != null){
            split = firebaseUser.getEmail().split("@");
            tvName.setText(split[0]);
        }

        final ImageView imageView = (ImageView) findViewById(iv);
        imageView.setVisibility(View.GONE);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if(checkIn.getDate().equals(todayDate) && checkIn.getName().equals(split[0])) {

                            imageView.setVisibility(View.VISIBLE);
                            //reading of image
                            final StorageReference pathref = storageReference.child(todayDate +"/" + split[0]+".jpg");
                            pathref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("success uri", uri.toString());

                                    // Load the image using Glide
                                    Glide.with(MainActivity.this)
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
        else if(v.getId() == R.id.imageButton){
            firebaseAuth.signOut();
            LocationTracker locationTracker = new LocationTracker(".myAction");
            locationTracker.stopLocationService(MainActivity.this);

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }
        else if(v.getId() == R.id.btnMc){
            Intent intent = new Intent(this, AndroidCameraApi.class);
            intent.putExtra("mc", "mc");
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
}
