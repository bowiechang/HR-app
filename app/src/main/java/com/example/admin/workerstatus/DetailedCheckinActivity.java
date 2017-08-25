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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class DetailedCheckinActivity extends AppCompatActivity implements OnClickListener {

    private String name, date, checkin, status, flag, location;
    private Button btnFlag;
    private ImageView imageView;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private ValueEventListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_checkin);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            date = getIntent().getExtras().getString("date");
            status = getIntent().getExtras().getString("status");
            name = getIntent().getExtras().getString("name");
            location = getIntent().getExtras().getString("location");
            flag = getIntent().getExtras().getString("flag");
            checkin = getIntent().getExtras().getString("checkin");

            Log.d("flag", flag);
        }

        init();
    }

    @Override
    public void onClick(View v) {

        if(btnFlag.getText().toString().toLowerCase().equals("revert")){
            //revert flag to false

            listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CheckIn checkIn = snapshot.getValue(CheckIn.class);
                        if(checkIn != null) {
                            if(checkIn.getDate().equals(date) && checkIn.getName().equals(name)){
                                String key = snapshot.getKey();
                                databaseReference.child(key).child("flag").setValue(false);
                                Toast.makeText(DetailedCheckinActivity.this, "Flag has been removed!", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(DetailedCheckinActivity.this, DetailedDateActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString("date", date);
                                i.putExtras(extras);
                                startActivity(i);
                                finish();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            databaseReference.addValueEventListener(listener);

        }
        else if(btnFlag.getText().toString().toLowerCase().equals("flag")){
            //flag to true

            listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CheckIn checkIn = snapshot.getValue(CheckIn.class);
                        if(checkIn != null) {
                            if(checkIn.getDate().equals(date) && checkIn.getName().equals(name)){
                                String key = snapshot.getKey();
                                databaseReference.child(key).child("flag").setValue(true);
                                Toast.makeText(DetailedCheckinActivity.this, "Flag successful!", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(DetailedCheckinActivity.this, DetailedDateActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString("date", date);
                                i.putExtras(extras);
                                startActivity(i);
                                finish();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(listener);
        }
    }

    public void readImage(String date, String name){

        //reading of image
        final StorageReference pathref = storageReference.child(date +"/" + name +".jpg");
        pathref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("success uri", uri.toString());

                // Load the image using Glide
                Glide.with(DetailedCheckinActivity.this)
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

        TextView tvDate = (TextView)findViewById(R.id.tvDate);
        TextView tvName = (TextView)findViewById(R.id.tvName);
        TextView tvStatus = (TextView)findViewById(R.id.tvStatus);
        TextView tvLocation = (TextView)findViewById(R.id.tvLocation);
        TextView tvCheckin = (TextView)findViewById(R.id.tvCheckin);
        TextView tvFlag = (TextView)findViewById(R.id.tvFlag);
        imageView = (ImageView) findViewById(R.id.iv);
        btnFlag = (Button)findViewById(R.id.btnFlag);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        btnFlag.setVisibility(View.INVISIBLE);
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
                        btnFlag.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        tvDate.setText(String.format("Date: %s", date));
        tvName.setText(String.format("Name: %s", name));
        tvStatus.setText(String.format("Status: %s", status));
        tvLocation.setText(String.format("Location: %s", location));
        tvCheckin.setText(String.format("Time in: %s", checkin));
        tvFlag.setText(String.format("Flag: %s", flag));

        btnFlag.setOnClickListener(this);
        if(flag.equals("true")){
            btnFlag.setText("revert");
        }
        readImage(date, name);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(listener != null) {
            databaseReference.removeEventListener(listener);
        }
    }
}
