package com.example.admin.workerstatus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class setupAccountActivity extends AppCompatActivity implements OnClickListener {

    private EditText etUserid, etEmpNo, etName, etWpNo, etPosition, etNationality, etWp;
    private Button btnCreateUser;

    String userid, empNo, name, wpNo, position, nationality, wp;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        init();
    }

    @Override
    public void onClick(View v) {

        userid = etUserid.getText().toString();
        empNo = etEmpNo.getText().toString();
        name = etName.getText().toString();
        wpNo = etWpNo.getText().toString();
        position = etPosition.getText().toString();
        nationality = etNationality.getText().toString();
        wp = etWp.getText().toString();

        if(userid.isEmpty() || empNo.isEmpty() || name.isEmpty() || wpNo.isEmpty() || position.isEmpty() || nationality.isEmpty() || wp.isEmpty()){
            Toast.makeText(setupAccountActivity.this, "One of the field is empty, please check again", Toast.LENGTH_SHORT).show();
        }
        else{

            firebaseAuth.createUserWithEmailAndPassword(userid + "@gmail.com", "123456").addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    System.out.println("created");

                    Account account = new Account(userid, empNo, name, wpNo, position, nationality, wp);
                    dbref.push().setValue(account);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("failed");
                    Toast.makeText(setupAccountActivity.this, "USERID ALREADY EXIST", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void init(){

        etUserid = (EditText) findViewById(R.id.etUserid);
        etEmpNo = (EditText) findViewById(R.id.etEmpNo);
        etName = (EditText) findViewById(R.id.etName);
        etWpNo = (EditText) findViewById(R.id.etWpNo);
        etPosition = (EditText) findViewById(R.id.etPosition);
        etNationality = (EditText) findViewById(R.id.etNationality);
        etWp = (EditText) findViewById(R.id.etWp);
        btnCreateUser = (Button) findViewById(R.id.btnCreateUser);
        btnCreateUser.setOnClickListener(this);

    }

}
