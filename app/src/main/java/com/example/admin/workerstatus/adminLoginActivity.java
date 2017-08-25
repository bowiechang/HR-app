package com.example.admin.workerstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class adminLoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText etId, etPw;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        etId = (EditText)findViewById(R.id.login_emailid);
        etPw = (EditText)findViewById(R.id.login_password);

        btnLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        login();
    }

    public void login() {

        String name = etId.getText().toString().trim().toLowerCase() +"@gmail.com";
        String editname = name.replace(" ", "");
        String password = etPw.getText().toString().trim().toLowerCase();

        // use method given by Fb, add listener so it could be track when its done
        firebaseAuth.signInWithEmailAndPassword(editname, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Intent intent = new Intent(adminLoginActivity.this, AdminDateActivity.class);
                    startActivity(intent);
                    Toast.makeText(adminLoginActivity.this, "Validating....", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(adminLoginActivity.this, "Please check ur admin id again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
