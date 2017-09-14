package com.example.admin.workerstatus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.admin.workerstatus.R.id.ivOut;

public class DetailedUserActivity extends AppCompatActivity{

    private String name, date, uri2;
    private ImageView imageView, imageView2, ivFlag;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_user);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            date = getIntent().getExtras().getString("date");
            name = getIntent().getExtras().getString("name");
        }

        init();
    }

    public void read(final String date, final String name, final OnGetDataListener listener){

        //read data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if (checkIn.getDate().equals(date)) {
                            if(checkIn.getName().equals(name)){

                                String timeout = (String) snapshot.child("checkout").getValue();
                                String hours = (String) snapshot.child("hours").getValue();
                                System.out.println(timeout + ":timeout");
                                listener.onSuccess(checkIn, timeout, hours);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //reading of image
        final StorageReference pathref = storageReference.child(date +"/" + "checkin-" + name +".jpg");
        pathref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("success uri", uri.toString());
                uri2 = uri.toString();
                // Load the image using Glide
                Glide.with(DetailedUserActivity.this)
                        .load(uri.toString())
                        .into(imageView);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failed uri", "failed: " + e.toString());
            }
        });

        //reading of image
        final StorageReference pathref2 = storageReference.child(date +"/" + "checkout-" + name +".jpg");
        pathref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("success uri", uri.toString());
                uri2 = uri.toString();
                // Load the image using Glide
                Glide.with(DetailedUserActivity.this)
                        .load(uri.toString())
                        .into(imageView2);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failed uri", "failed: " + e.toString());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void init(){

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.MyAppbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final TextView tvDate = (TextView)findViewById(R.id.tvDate);
        final TextView tvStatus = (TextView)findViewById(R.id.tvStatus);
        final TextView tvCheckin = (TextView)findViewById(R.id.tvCheckin);
        final TextView tvCheckout = (TextView)findViewById(R.id.tvCheckout);
        final TextView tvHours = (TextView)findViewById(R.id.tvHours);
        final TextView tvWarning = (TextView)findViewById(R.id.tvWarning);

        imageView = (ImageView) findViewById(R.id.iv);
        imageView2 = (ImageView) findViewById(ivOut);
        ivFlag = (ImageView) findViewById(R.id.ivFlag);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        toolbar.setVisibility(View.INVISIBLE);
        appBarLayout.setVisibility(View.INVISIBLE);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3400);
                } catch (Exception e) {
                } // Just catch the InterruptedException

                // Now we use the Handler to post back to the main thread
                handler.post(new Runnable() {
                    public void run() {
                        // Set the View's visibility back on the main UI Thread
                        relativeLayout.setVisibility(View.INVISIBLE);
                        appBarLayout.setVisibility(View.VISIBLE);
                        toolbar.setVisibility(View.VISIBLE);

                        read(date, name, new OnGetDataListener() {
                            @Override
                            public void onSuccess(CheckIn checkIn, String timeout, String hours) {

                                if(timeout!=null){
                                    tvCheckout.setText(String.format("Time out: %s", timeout));
                                }
                                if(hours!=null){
                                    tvHours.setText(String.format("Hours clocked: %s", hours));
                                }

                                tvDate.setText(String.format("Date: %s", date));
                                tvStatus.setText(String.format("Status: %s", checkIn.getMc()));
                                tvCheckin.setText(String.format("Time in: %s", checkIn.getCheckin()));

                                if(tvDate.getText()!=null){
                                    tvWarning.setVisibility(View.GONE);
                                }


                                if(checkIn.getFlag().equals(true)){
                                    ivFlag.setImageResource(R.drawable.calendarnotokflag);
                                }
                                else if(checkIn.getFlag().equals(false)){
                                    ivFlag.setImageResource(R.drawable.calendarokflag);
                                }
                            }
                        });
                    }
                });
            }
        }).start();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(CheckIn checkIn, String timeout, String hours);
    }
}
