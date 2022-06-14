package com.androidtutorial.mybookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidtutorial.mybookapp.adapters.AdapterCategory;
import com.androidtutorial.mybookapp.databinding.ActivityDashboardAdminBinding;
import com.androidtutorial.mybookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    //View binding di layout xml
    private ActivityDashboardAdminBinding binding;

    private DatabaseReference reference;


    //firebase auth
    private FirebaseAuth firebaseAuth;

    //array list to store category
    private ArrayList<ModelCategory> categoryArrayList;
    //adapter
    private AdapterCategory adapterCategory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initial firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        checkUser();
        loadCategories();

        //edittext change listern, search
        binding.serachEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when usertype each letter
                try {
                    adapterCategory.getFilter().filter(s);
                }
                catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle tombol keluar/logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();

            }
        });



        // Hnadel tomobol tambah categoru

        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        // Handel tombl start category add screen
        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {



        //ambil semua kategori dari feribase > Categories

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //initial arraylist
                categoryArrayList = new ArrayList<>();
                categoryArrayList.clear(); //clear arraylist befor addingdata into it
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);  //Mengambli data
                    categoryArrayList.add(model);    //add ti array list
                }
                //setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                //setadapter to recycler cirew
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Data Gagal Dimuat", Toast.LENGTH_LONG).show();
                Log.e("DashboardAdmin", databaseError.getDetails()+" "+databaseError.getMessage());
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