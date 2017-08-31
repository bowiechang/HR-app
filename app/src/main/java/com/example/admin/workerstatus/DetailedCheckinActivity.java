package com.example.admin.workerstatus;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
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

import java.io.File;

public class DetailedCheckinActivity extends AppCompatActivity implements OnClickListener {

    private String name, date, checkin, status, flag, location, uri2;
    private Button btnFlag, btnDLImage;
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
                uri2 = uri.toString();
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
        ConstraintLayout constraintLayout = (ConstraintLayout)findViewById(R.id.detailed_checkin_activity);

        imageView = (ImageView) findViewById(R.id.iv);
        btnFlag = (Button)findViewById(R.id.btnFlag);
        btnDLImage = (Button)findViewById(R.id.btnDLImage);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.spinnerkitloader);
        btnFlag.setVisibility(View.INVISIBLE);
        btnDLImage.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.INVISIBLE);
        appBarLayout.setVisibility(View.INVISIBLE);

        int colorflag = ContextCompat.getColor(DetailedCheckinActivity.this, R.color.flag);
        int colorunflag = ContextCompat.getColor(DetailedCheckinActivity.this, R.color.background);
        int checked = ContextCompat.getColor(DetailedCheckinActivity.this, R.color.checked2);

        if(flag.equals("true")){
            constraintLayout.setBackgroundColor(colorflag);
        }
        else{
            constraintLayout.setBackgroundColor(colorunflag);
        }

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
                        btnDLImage.setVisibility(View.VISIBLE);
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
        btnDLImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(uri2);
            }
        });

        if(flag.equals("true")){
            btnFlag.setText("revert");
            btnFlag.setBackgroundColor(checked);
        }
        else{
            btnFlag.setBackgroundColor(colorflag);
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

    private void downloadFile(String uri) {
        if(isFileExists()){
            File folder1 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/image.jpg");
            folder1.delete();
        }

        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File direct = new File(directory_path);

        direct.mkdirs();

        DownloadManager mgr = (DownloadManager) DetailedCheckinActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uri);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Downloading and exporting")
                .setDescription("Ready in a bit")
                .setDestinationInExternalPublicDir("/Download", "image.jpg");

        mgr.enqueue(request);


        //emailing
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

                        if(isFileExists()) {
                            String filename="image.jpg";
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
                            emailIntent .putExtra(Intent.EXTRA_SUBJECT, name + " on " + date );
                            startActivity(Intent.createChooser(emailIntent , "Send email..."));
                        }

                    }
                });
            }
        }).start();





    }

    private boolean isFileExists(){
        File folder1 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/image.jpg");
        return folder1.exists();
    }
}
