package com.androidtutorial.mybookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidtutorial.mybookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;

    //uri of picked pdf
    private Uri pdfUri = null;

    //arraylist to hold pdf categories/ memilih categories yang dibuat setelah menekan tombol arrow bawah
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //tag for debugging
    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 10000;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup progress doalog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait ...");
        progressDialog.setCanceledOnTouchOutside(false);



        //handle button kembali
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handel tombol memasukkan pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();

            }
        });

        //handel tomobol pick kategori
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //handel tombol upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String title= "", description ="";

    private void validateData() {
        //langkap 1 = validatasi data
        Log.d(TAG, "validateData: Validating data ...");

        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        
        //validasi data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Masukkan Judul ...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Masukkan Deskripsi ...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Pilih Category ...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null){
            Toast.makeText(this, "Pilih Pdf ...", Toast.LENGTH_SHORT).show();
        }
        else {
            // ketika semua data valid, baru bisa upload
            uploadPdfToStorage();
        }
        
    }

    private void uploadPdfToStorage() {
        //langkap 2 = Upload pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storeage");

        //show progress
        progressDialog.setMessage("Mengupload Pdf ...");
        progressDialog.show();

        //timestamps
        long timestamp = System.currentTimeMillis();

        //path/ jalur of pdf in firegbase storage
        String filePathAndName = "Book/" + timestamp;
        //storegae refrence
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Pdf uploaded to storage ...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //uplaod to firebase db
                        uploadedPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF uplaod fialed due to" + e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Pdf gagal di upload"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void uploadedPdfInfoToDb(String uploadedPdfUrl, long timestamp) {

        //langkap 3 = Upload pdf info firebase db
        Log.d(TAG, "uploadPdfToStorage: uploading pdf info firebase db ...");

        progressDialog.setMessage("Uploading pdf info ...");

        String uid = firebaseAuth.getUid();

        //Setup data to uplaod
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("desciption", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);


        // db reference : DB > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Berhasil Upload");
                        Toast.makeText(PdfAddActivity.this, "Berhasil Upload ", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf Categories.....");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //mengambil refernsi dari Categories database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();//clear before adding data
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){


                    // get in and title of category
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    //add to respective array list
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {



            }
        });
    }

    //selected category id and category title
    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get stirng array of categores from arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dilaog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clik
                        //get clik item form/dapatkan item yang diklik dari daftar
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        //set to category textview
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category"+selectedCategoryId+""+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent  : starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF PICKED");
                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: Uri: "+pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "Batal memilih PDF", Toast.LENGTH_SHORT).show();

        }
    }
}