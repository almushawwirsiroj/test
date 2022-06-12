package com.androidtutorial.mybookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.androidtutorial.mybookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {


    //veiw bingin
    private ActivityLoginBinding binding;

    //firebase Auth
    private FirebaseAuth firebaseAuth;

    //progress Dialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initial kan firebase aut
        firebaseAuth = FirebaseAuth.getInstance();

        //Mempersiapkan progrees dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(" Tunggu Sebentar ");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle tomobol untuk ke menu register
        binding.noAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //atur klik untuk button login masuk
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email = "", password = "";
    private void validateData() {
        //sebelum lgin validasi data terlebih dahulu

        //ambil data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //Validasi data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            //email tidak dimasukkan atau pola email tidak valid, jangan izinkan untuk melanjutkan jika demikian
            Toast.makeText(this, "Gagal memasukan email .... !", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){

            // Password dalam edit text kosong, harus memasukkan password
            Toast.makeText(this, "Masukkan Password .... !", Toast.LENGTH_SHORT).show();
        }
        else {
            // jika data valid , mulai login
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Sedang Masuk ... ");
        progressDialog.show();

        //login User
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //lgoin berhasil, check apakah user atau admin yang masuk
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //lgoingagal
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User ... ");
        //check apakah admin atau user yang masuk dari realtime database
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check dalam database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //mengambil user type
                        String userType = ""+snapshot.child("userType").getValue();
                        //chck user tipe
                        if (userType.equals("user")){
                            //jika user,membuka dashboard user
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        }
                        else if (userType.equals("admin")){
                            //jika adminn, membuak dashboar admin
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}