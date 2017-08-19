package com.example.admin.workerstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvName = (TextView) findViewById(R.id.tvName);

        Button btnAttendance = (Button) findViewById(R.id.btnAttendance);
        btnAttendance.setOnClickListener(this);

        if(firebaseUser != null){
            String[] split = firebaseUser.getEmail().split("@");
            tvName.setText(split[0]);
        }
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(this, AndroidCameraApi.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
