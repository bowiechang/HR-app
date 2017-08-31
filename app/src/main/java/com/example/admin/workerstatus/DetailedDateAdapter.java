package com.example.admin.workerstatus;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by admin on 20/7/17.
 */

public class DetailedDateAdapter extends RecyclerView.Adapter<DetailedDateHolder> {

    protected List<CheckIn> list;
    protected Context context;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private String ll;

    private String date;
    private String name;
    private String checkin;
    private String location;
    private String flag;
    private String status;


    public DetailedDateAdapter(Context context, List<CheckIn> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public DetailedDateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DetailedDateHolder detailedDateHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_date_list, parent, false);
        detailedDateHolder = new DetailedDateHolder(view, list, context);

        return detailedDateHolder;
    }

    @Override
    public void onBindViewHolder(final DetailedDateHolder holder, final int position) {
        holder.tvName.setText(String.format("Name: %s", list.get(position).getName().toUpperCase()));
        holder.tvCheckin.setText(String.format("Clock in: %s", list.get(position).getCheckin()));
        holder.tvStatus.setText(String.format("Status: %s", list.get(position).getMc()));

        //reading of latlng and conversion
        date = list.get(position).getDate();
        name = list.get(position).getName();
        checkin = list.get(position).getCheckin();
        flag = list.get(position).getFlag().toString();
        status = list.get(position).getMc();

        int colorChecked = ContextCompat.getColor(context, R.color.flag);

        if(flag.equals("true")){
            holder.itemView.setBackgroundColor(colorChecked);
        }

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("LocationTrackings");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if(user != null) {
                        if(user.getDate().equals(date) && user.getName().equals(name) && user.getTime().equals(checkin)){
                            ll = user.getLatlng();

                            try {
                                holder.tvLocation.setText(getAddress(ll));
                                location = getAddress(ll);
                            } catch (IOException e) {
                                e.printStackTrace();
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
        final StorageReference pathref = storageReference.child(list.get(position).getDate() +"/" + list.get(position).getName()+".jpg");
        pathref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("success uri", uri.toString());

                // Load the image using Glide
                Glide.with(context)
                        .load(uri.toString())
                        .into(holder.iv);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failed uri", "failed: " + e.toString());
            }
        });

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailedCheckinActivity.class);
                Bundle extras = new Bundle();
                extras.putString("date", list.get(position).getDate());
                extras.putString("name", list.get(position).getName());
                extras.putString("checkin", list.get(position).getCheckin());
                extras.putString("location", holder.tvLocation.getText().toString());
                extras.putString("flag", list.get(position).getFlag().toString());
                extras.putString("status", list.get(position).getMc());
                i.putExtras(extras);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public String getAddress(String latlng) throws IOException {

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
