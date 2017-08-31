package com.example.admin.workerstatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by admin on 20/7/17.
 */

public class UserCalendarAdapter extends RecyclerView.Adapter<UserCalendarHolder> {

    protected List<String> list;
    protected Context context;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private String[] split;
    private String name;


    public UserCalendarAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public UserCalendarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserCalendarHolder userCalendarHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_calendar_list, parent, false);
        userCalendarHolder = new UserCalendarHolder(view, list, context);

        return userCalendarHolder;
    }

    @Override
    public void onBindViewHolder(final UserCalendarHolder holder, final int position) {

        if(firebaseUser != null){
            split = firebaseUser.getEmail().split("@");
            name = split[0];
        }

        final String date2 = list.get(position);
        holder.tvDate.setText(date2);

        //set list.get(pos) for tvDate, tvWork and tvMC set it at a dbref call

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if (checkIn.getDate().equals(date2)) {
                            if(checkIn.getName().equals(name)){
                                holder.tvStatus.setText(String.format("Status: %s", checkIn.getMc()));
                                if(checkIn.getFlag().equals(true)){
                                    holder.ivFlag.setImageResource(R.drawable.calendarnotokflag);
                                }
                                else{
                                    holder.ivFlag.setImageResource(R.drawable.calendarokflag);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailedUserActivity.class);
                Bundle extras = new Bundle();
                extras.putString("date", date2);
                extras.putString("name", name);
                i.putExtras(extras);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }
}
