package com.androidtutorial.mybookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //initial firebase auth
        firebaseAuth = FirebaseAuth.getInstance();



        //masuk ke menu utama setelah splashscreen 2 detik
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        },2000);//2000 means, sama dengan 2 detik
    }

    private void checkUser() {
        //get current user, jika login
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            //user note logged in
            //start main screen/masuk ke activity menu
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();//activity berakhir
        }
        else {
            //ketika login mengeceheck usertype, ketika selesai masuk ke login screen
            //check dalam database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //mengambil user type
                            String userType = ""+snapshot.child("userType").getValue();
                            //chck user tipe
                            if (userType.equals("user")){
                                //jika user,membuka dashboard user
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();
                            }
                            else if (userType.equals("admin")){
                                //jika adminn, membuak dashboar admin
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}