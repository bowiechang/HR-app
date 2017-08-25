package com.example.admin.workerstatus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDateActivity extends AppCompatActivity implements OnClickListener {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DateStatus");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_date);

        final ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.d("ref", databaseReference.getRef().toString());

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Status, StatusHolder>(Status.class, R.layout.date_list, StatusHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(StatusHolder viewHolder, Status model, int position) {

                Log.d("reached", "1");
                if(model.getStatus().equals("Unchecked")){
                    viewHolder.tvStatus.setTextColor(Color.RED);
                }
                else{
                    viewHolder.tvStatus.setTextColor(Color.GREEN);
                }
                viewHolder.tvStatus.setText(model.getStatus());
                viewHolder.tvDate.setText(model.getDate());

                Log.d("getStatus", model.getStatus());
                Log.d("getdate", model.getDate());

            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onClick(View v) {
        firebaseAuth.signOut();
        Intent i = new Intent(AdminDateActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {}
}
