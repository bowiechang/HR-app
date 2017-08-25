package com.example.admin.workerstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.safety.locationlistenerhelper.core.SettingsLocationTracker;

public class LocationReceiver extends BroadcastReceiver {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private String[] split;

    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm aaa");
    private String todayDate = dateformat.format(c.getTime());
    private String time = timeformat.format(c.getTime());

    String address;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (null != intent && intent.getAction().equals("my.action")) {
            Location locationData = (Location) intent.getParcelableExtra(SettingsLocationTracker.LOCATION_MESSAGE);
            Log.d("Location: ", "Latitude: " + locationData.getLatitude() + "Longitude:" + locationData.getLongitude());
            //send your call to api or do any things with the of location data


            if(firebaseUser != null){
                split = firebaseUser.getEmail().split("@");
            }

            final DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
            final String latlng = locationData.getLatitude() + "," + locationData.getLongitude();

            try {
                address = getAddress(latlng, context);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final User user = new User(latlng, time, split[0], todayDate, address);
            databaseReferenceUser.push().setValue(user);
        }
    }

    public String getAddress(String latlng, Context context) throws IOException {

        String[] split = latlng.split(",");

        Double lat = Double.parseDouble(split[0]);
        Double lng = Double.parseDouble(split[1]);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        return address;
    }
}
