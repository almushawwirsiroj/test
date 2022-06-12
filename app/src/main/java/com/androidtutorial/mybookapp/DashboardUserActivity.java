package com.androidtutorial.mybookapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.androidtutorial.mybookapp.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardUserBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initial firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //handle tombol keluar/logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            //tidak login, masuk ke main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else{
            //lgoin , dapatkan info user
            String email = firebaseUser.getEmail();
            //memasukkna textview kedalam toolbar
            binding.subTitleTv.setText(email);
        }
    }
}