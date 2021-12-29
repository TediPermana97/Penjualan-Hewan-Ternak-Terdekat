package com.example.nanodg.tambalban;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginAdminActivity extends AppCompatActivity {

    Button loginUser;
    EditText loginusername,loginpassword;
    TextView loginval_user,loginval_pass;
    FirebaseAuth loginauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);


        loginUser = (Button) findViewById(R.id.loginButton);
        loginusername = (EditText) findViewById(R.id.loginUsername);
        loginpassword = (EditText) findViewById(R.id.loginPassword);
        loginval_user = (TextView) findViewById(R.id.loginvalidation3);
        loginval_pass = (TextView) findViewById(R.id.loginvalidation4);
        loginauth = FirebaseAuth.getInstance();
        this.loginUser.setOnClickListener(new View.OnClickListener() {
            String user,pass;
            @Override
            public void onClick(View v) {
                user = loginusername.getText().toString();
                pass = loginpassword.getText().toString();

                if(user.equals("") && pass.equals("")){
                    loginval_user.setText("Enter Username");
                    loginval_pass.setText("Enter Password");
                }else{
                    LoginUser(user,pass);
                }
            }
        });
    }

    private void LoginUser(String name,String password){
        loginauth.signInWithEmailAndPassword(name,password).addOnSuccessListener(LoginAdminActivity.this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(LoginAdminActivity.this,"Login Successfull",Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginAdminActivity.this, PnlAdminActivity.class));
            }
        });
    }
}