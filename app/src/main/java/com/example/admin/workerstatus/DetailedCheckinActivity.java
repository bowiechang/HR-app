package com.example.admin.workerstatus;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import static org.apache.poi.sl.draw.binding.STRectAlignment.R;

public class DetailedCheckinActivity extends AppCompatActivity implements OnClickListener {

    private String name, date, checkin, status, flag, location, uri2, uri3, checkoutTime, checkoutLocation;
    private Button btnFlag, btnDLImage;
    private ImageView imageView, imageViewOut;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private ValueEventListener listener;

    private TextView tvCheckOutLocation;

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
            checkoutTime = getIntent().getExtras().getString("checkoutTime");
            checkoutLocation = getIntent().getExtras().getString("checkoutLocation");

            init();
            readImage(date, name);
        }
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
//
//    public void retrieveCheckOutLocation(){
//
//        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    if(user != null) {
//                        if(user.getDate().equals(date) && user.getName().equals(name) && user.getTime().equals(checkoutTime)){
//
//                            String ll = user.getLatlng();
//
//                            try {
//                                location = getAddress(ll);
//                                tvCheckOutLocation.setText(String.format("Location: %s", location));
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
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
//
//    }

    public void readImage(String date, String name){

        //reading of image
        final StorageReference pathref = storageReference.child(date +"/" + "checkin-" + name +".jpg");
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

        //reading of image
        final StorageReference pathref2 = storageReference.child(date +"/" + "checkout-" + name +".jpg");
        pathref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("success uri", uri.toString());
                uri3 = uri.toString();
                // Load the image using Glide
                Glide.with(DetailedCheckinActivity.this)
                        .load(uri.toString())
                        .into(imageViewOut);

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
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Name: " + name.toUpperCase());

        TextView tvDate = (TextView)findViewById(R.id.tvDate);
        TextView tvStatus = (TextView)findViewById(R.id.tvStatus);
        TextView tvLocation = (TextView)findViewById(R.id.tvLocation);
        TextView tvCheckin = (TextView)findViewById(R.id.tvCheckin);
        TextView tvFlag = (TextView)findViewById(R.id.tvFlag);
        tvCheckOutLocation = (TextView)findViewById(R.id.tvCheckoutLocation);
        TextView tvCheckOutTime = (TextView)findViewById(R.id.tvCheckOut);
        ConstraintLayout constraintLayout = (ConstraintLayout)findViewById(R.id.detailed_checkin_activity);

        imageView = (ImageView) findViewById(R.id.detailed_checkin_activity_iv);
        imageViewOut = (ImageView) findViewById(R.id.detailed_checkin_activity_ivOut);
        btnFlag = (Button)findViewById(R.id.btnFlag);
        btnDLImage = (Button)findViewById(R.id.btnDLImage);

//        retrieveCheckOutLocation();

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
        tvStatus.setText(String.format("Status: %s", status));
        tvLocation.setText(String.format("Checkin Location: %s", location));
        tvCheckin.setText(String.format("Checkin Time: %s", checkin));
        tvFlag.setText(String.format("Flag: %s", flag));
        tvCheckOutTime.setText(String.format("Checkout Time: %s", checkoutTime));
        tvCheckOutLocation.setText(String.format("Checkout Location: %s", checkoutLocation));

        btnFlag.setOnClickListener(this);
        btnDLImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailedCheckinActivity.this, "Getting it up....", Toast.LENGTH_SHORT).show();
                downloadFile(uri2, uri3);
            }
        });

        if(flag.equals("true")){
            btnFlag.setText("revert");
            btnFlag.setBackgroundColor(checked);
        }
        else{
            btnFlag.setBackgroundColor(colorflag);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(listener != null) {
            databaseReference.removeEventListener(listener);
        }
    }

    private void downloadFile(final String uri, String uri2) {
        if(isFileExists()){
            File folder1 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/checkin.jpg");
            folder1.delete();

            File folder2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/checkout.jpg");
            folder2.delete();
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
                .setDestinationInExternalPublicDir("/Download", "checkin.jpg");

        //image 2
        Uri downloadUri2 = Uri.parse(uri2);
        DownloadManager.Request request2 = new DownloadManager.Request(
                downloadUri2);

        request2.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Downloading and exporting")
                .setDescription("Ready in a bit")
                .setDestinationInExternalPublicDir("/Download", "checkout.jpg");

        mgr.enqueue(request);
        mgr.enqueue(request2);


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
                            ArrayList<Uri> uris = new ArrayList<Uri>();

                            String filename="checkin.jpg";
                            String filename2="checkout.jpg";

                            File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
                            File filelocation2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename2);

                            Uri path = Uri.fromFile(filelocation);
                            Uri path2 = Uri.fromFile(filelocation2);

                            uris.add(path);
                            uris.add(path2);

                            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
// set the type to 'email'
                            emailIntent .setType("vnd.android.cursor.dir/email");
                            String to[] = {"bv.master01@gmail.com"};
                            emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
//                            emailIntent .putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
                            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            emailIntent .putExtra(Intent.EXTRA_SUBJECT, name + " on " + date );
                            startActivity(Intent.createChooser(emailIntent , "Send email..."));
                        }

                    }
                });
            }
        }).start();

    }

    private boolean isFileExists(){
        File folder1 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/checkin.jpg");
        return folder1.exists();
    }

    public String getAddress(String latlng) throws IOException {

        String[] split = latlng.split(",");

        Double lat = Double.parseDouble(split[0]);
        Double lng = Double.parseDouble(split[1]);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(DetailedCheckinActivity.this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        return address;
    }
}
