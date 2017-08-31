package com.example.admin.workerstatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by admin on 20/7/17.
 */

public class AdminMainAdapter extends RecyclerView.Adapter<AdminMainHolder> {

    protected List<String> list;
    protected Context context;

    private String date, NoOfWork, NoOfMc;


    public AdminMainAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public AdminMainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdminMainHolder adminMainHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_main_list, parent, false);
        adminMainHolder = new AdminMainHolder(view, list, context);

        return adminMainHolder;
    }

    @Override
    public void onBindViewHolder(final AdminMainHolder holder, final int position) {
        final String date2 = list.get(position);
        holder.tvDate.setText(date2);

        //set list.get(pos) for tvDate, tvWork and tvMC set it at a dbref call

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CheckIns");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int noOfWork = 0;
                int noOfMc = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = snapshot.getValue(CheckIn.class);
                    if(checkIn != null) {
                        if (checkIn.getDate().equals(date2)) {
                            if (checkIn.getMc().equals("working")) {
                                noOfWork++;
                            } else if (checkIn.getMc().toLowerCase().equals("on mc")) {
                                noOfMc++;
                            }
                        }
                    }
                }

                holder.tvMc.setText(String.format("MC: %s", noOfMc));
                holder.tvWork.setText(String.format("Working: %s", noOfWork));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("DateStatus");
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int colorChecked = ContextCompat.getColor(context ,R.color.checked);
                int colorUnchecked = ContextCompat.getColor(context,R.color.unchecked);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Status status = snapshot.getValue(Status.class);
                    if(status != null) {
                        if (status.getDate().equals(date2)){
                            if(status.getStatus().equals("Checked")) {
                                holder.itemView.setBackgroundColor(colorChecked);
                                System.out.println("checked");
                            }
                            else if(status.getStatus().equals("Unchecked")) {
                                holder.itemView.setBackgroundColor(colorUnchecked);
                                System.out.println("unchecked");
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
                Intent i = new Intent(context, DetailedDateActivity.class);
                Bundle extras = new Bundle();
                extras.putString("date", date2);
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
