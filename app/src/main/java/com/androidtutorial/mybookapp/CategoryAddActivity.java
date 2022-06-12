package com.androidtutorial.mybookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.androidtutorial.mybookapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;


    //firebease auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        //initial firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Tunggu Sebentar ya ...");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle button kemabli
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        //handle click, upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String category = "";

    private void validateData() {
        /*validasi data sebelum menambahkan*/

        //get data

        category = binding.categoryEt.getText().toString().trim();
        //validate jika kosong
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Please enter Category", Toast.LENGTH_SHORT).show();
        }
        else{
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
    //show progress dialog
        progressDialog.setMessage("Menambah Category ...");
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        //setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //add to firebase db ...... database root > categories > category id > categoru info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Category add succse
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Category berhasil ditambahkan ... ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //category gagal menambahkan
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}