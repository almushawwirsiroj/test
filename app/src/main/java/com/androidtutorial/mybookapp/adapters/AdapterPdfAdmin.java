package com.androidtutorial.mybookapp.adapters;

import static com.androidtutorial.mybookapp.Constans.MAX_BYTES_PDF;
import static java.lang.String.format;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtutorial.mybookapp.MyApplication;
import com.androidtutorial.mybookapp.PdfListAdminActivity;
import com.androidtutorial.mybookapp.databinding.RowPdfAdminBinding;
import com.androidtutorial.mybookapp.filters.FilterPdfAdmin;
import com.androidtutorial.mybookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    //context
    private Context context;
    //array list to hold list of data of tyep model pdf
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    //binding tampilan

    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //construster
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding tampilan dengan row pdf admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        //Get data, set data, handle clikk dan lain lain

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDesctiption();
        Long timestamp =  model.getTimestamp();

        //we nedd to convert timestamps to dd/mm/yyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set.data
        holder.titleTv.setText(title);
        holder.desciptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //load further details like category, pdf form urel pdf size in seprate functions
        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);


    }

    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file aand its metadata form firebase storage
        String pdfUrl = model.getUrl();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in byters
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+model.getTitle() +""+bytes);

                        //convert byters to kb, dari mb
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb>= 1){
                            holder.sizeTv.setText(format("%.2f", mb)+" MB");
                        }
                        else if (kb>= 1){
                            holder.sizeTv.setText(format("%.2f", kb)+" KB");
                        }
                        else {
                            holder.sizeTv.setText(format("%.2f", bytes)+" KB");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failled getting metada, gagal mengambil
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file and its emtada form firebse storage

        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+model.getTitle()+ "suscces git the file");

                        // set to pdf view
                        holder.pdfView.fromBytes(bytes)
                                .pages(0) //selalu manumpilkan halaman pertama
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {

                                    @Override
                                    public void onPageError(int page, Throwable t) {

                                        //hide progress
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded
                                        //hide progress
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf berhasil di loaded  ");
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed getting file form url due to"+e.getMessage());

                    }
                });
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
     //get category using category id

     String categoryId = model.getCategoryId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get cateogory
                        String category = ""+snapshot.child("category").getValue();
                        //set to category text view
                        holder.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
      //return number of redorcs | list size
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    /*View Holder class for row pdf admin*/
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //UI View of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, desciptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            //init ui view
            pdfView= binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            desciptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;

        }
    }
}
