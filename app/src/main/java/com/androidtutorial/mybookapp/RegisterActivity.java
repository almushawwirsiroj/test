package com.androidtutorial.mybookapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidtutorial.mybookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //menampilkan binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*Masukkan kedalam dependencies firebase dekat gradle
        * 1. firebase Auth
        * 2. Firebase Realtime
        * 3. Firebase Storage
         */

        //initial kan firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //menyiapkan progres tdialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Tunggu Sebentar");

        // handle tomobol back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //handle tombol untuk register/mendafrtar
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String name = "", email = "", password = "";
    /* sebelum membuat akun baru, lakukan beberapa validasi data*/
    private void validateData() {

        // mengambil data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();


        //jika data valid atau benar

        if (TextUtils.isEmpty(name)){

            // Nama dalam edit text kosong, harus memasukkan nama
            Toast.makeText(this, "Masukkan nama mu .... ", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            //email tidak dimasukkan atau pola email tidak valid, jangan izinkan untuk melanjutkan jika demikian
            Toast.makeText(this, "Gagal memasukan email .... !", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){

            // Password dalam edit text kosong, harus memasukkan password
            Toast.makeText(this, "Masukkan Password .... !", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cPassword)){

            //konfirmasi dalam edit text kosong, harus memasukkan konfirmasi password
            Toast.makeText(this, "Konfirmasi Password.... !", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(cPassword)){

            //password dan konfirmasi password tidak sama, tidak boleh melanjutkan, keduanya harus sama
            Toast.makeText(this, "Password tidak sama ", Toast.LENGTH_SHORT).show();
        }
        else {

            //semua data harus valit benar, saat memulai membuat akun
            createUserAccount();
        }
    }

    private void createUserAccount() {

        //Menampilkan progress
        progressDialog.setMessage(" Membuat Akun ");
        progressDialog.show();

        //membuat user dalam firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account berhasil dibuat, sekarang masukkan ke firebase realtime database
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                         //account gagal dibuat
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateUserInfo() {
        progressDialog.setMessage(" Menyimpan info User ....");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //dapatkan user uid, karena User terdaftar, jadi bisa mendapatkan
        String uid = firebaseAuth.getUid();

        //Menyiapkan data untuk dimasukkan ke database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", email);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user"); //value/pemilihan uuntuk user,untuk menjadi admin filakukan manual di firebase dengan mengganti values
        hashMap.put("timestamp", timestamp);

        //set data ke database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data dimasukkan ke database
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account berhasil dibuat .....", Toast.LENGTH_SHORT).show();
                        //setelah akun berhasil dibuat, memuali masuk ke login
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data gagal dimasukkan ke database
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}