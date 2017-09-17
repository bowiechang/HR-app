package com.example.admin.workerstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.res.Configuration.KEYBOARD_12KEY;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText etLoginId;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(firebaseAuth.getCurrentUser() != null){

            String[] split = firebaseUser.getEmail().split("@");
            String name = split[0];

            if(name.contains("bv.")){
                Intent intent = new Intent(this, AdminMainActivity.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(this, AttendanceActivity.class);
                startActivity(intent);
            }
        }

        TextView tvAdmin = (TextView) findViewById(R.id.tvAdmin);

        etLoginId = (EditText) findViewById(R.id.etLoginId);
        etLoginId.setRawInputType(KEYBOARD_12KEY);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this);
        tvAdmin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btnLogin:

                Toast.makeText(LoginActivity.this, "Please wait a moment", Toast.LENGTH_SHORT).show();
                login();
                break;

            case R.id.tvAdmin:
                Intent i = new Intent(LoginActivity.this, adminLoginActivity.class);
                startActivity(i);
                break;
        }

    }

    public void login() {

        String name = etLoginId.getText().toString().trim().toLowerCase() +"@gmail.com";
        String editname = name.replace(" ", "");
        String password = "123456";

        // use method given by Fb, add listener so it could be track when its done
        firebaseAuth.signInWithEmailAndPassword(editname, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Please check the entered name again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onBackPressed(){}
}
