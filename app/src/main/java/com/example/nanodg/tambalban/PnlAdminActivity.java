package com.example.nanodg.tambalban;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.nanodg.tambalban.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PnlAdminActivity extends AppCompatActivity implements View.OnClickListener{

    CardView btntambah,btntambal,btnuser,btnaduan,btnlogout,btnbengkel,btnaksesoris;
    FirebaseAuth firebaseAuth;
    TextView tvslmt;
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnl_admin);

        btntambah = (CardView)findViewById(R.id.btntambah);
        btnbengkel = (CardView)findViewById(R.id.btnbengkel);
        btnaksesoris = (CardView)findViewById(R.id.btnaksesoris);
        btntambal = (CardView) findViewById(R.id.btntambal);
        btnuser = (CardView) findViewById(R.id.btnuser);
        btnaduan = (CardView) findViewById(R.id.btnaduan);
        btnlogout = (CardView) findViewById(R.id.btnlogout);
        tvslmt = (TextView) findViewById(R.id.tvslmt);
        initToolbar();
        btnlogout.setOnClickListener(this);
        btnbengkel.setOnClickListener(this);
        btnaksesoris.setOnClickListener(this);
        btnaduan.setOnClickListener(this);
        btnuser.setOnClickListener(this);
        btntambah.setOnClickListener(this);
        btntambal.setOnClickListener(this);

        /**
         * FIREBASE LOGIN
         */
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginUserActivity.class));
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String alias = user.getEmail();

        /**
         * Merubah Email ke Username
         */
        DatabaseReference mUserContactsRef =  FirebaseDatabase.getInstance().getReference().child("Users");
        mUserContactsRef.orderByChild("email").equalTo(alias).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                //Log.e("barang1", dataSnapshot.toString());
                for (DataSnapshot userContact : dataSnapshot.getChildren()) {

                    User user = userContact.getValue(User.class);
                    tvslmt.setText("Selamat Datang : "+ user.getUsername());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Panel Admin");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                onBackPressed();

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == btnbengkel) {
            startActivity(new Intent(this, TambahActivity.class));
        }
        if (view == btnaksesoris) {
            startActivity(new Intent(this, TambahActivity.class));
        }
        if (view == btntambah) {
            startActivity(new Intent(this, TambahActivity.class));
        }if (view == btnlogout) {
            //logging out the user
            firebaseAuth.signOut();
            //closing activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginUserActivity.class));
        } if (view == btntambal) {
            startActivity(new Intent(this, ListTambalActivity.class));
        } if (view == btnaduan) {
            startActivity(new Intent(this, ListAduanActivity.class));

        } if (view == btnuser) {
            startActivity(new Intent(this, ListUserActivity.class));

        }
    }
}
