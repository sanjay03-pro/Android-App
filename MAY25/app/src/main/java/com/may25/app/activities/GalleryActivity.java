package com.may25.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.may25.app.R;
import com.may25.app.adapters.PhotoAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView rvPhotos;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddPhoto;

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private PhotoAdapter adapter;
    private List<String> photoUrls = new ArrayList<>();

    private static final String PHOTOS_NODE = "gallery/may25_private";
    private static final int PICK_IMAGE_REQ = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Our Photos 📸❤️");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbRef      = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        rvPhotos     = findViewById(R.id.rv_photos);
        progressBar  = findViewById(R.id.progress_bar);
        fabAddPhoto  = findViewById(R.id.fab_add_photo);

        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PhotoAdapter(this, photoUrls);
        rvPhotos.setAdapter(adapter);

        fabAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Photo"), PICK_IMAGE_REQ);
        });

        loadPhotos();
    }

    private void loadPhotos() {
        progressBar.setVisibility(View.VISIBLE);
        dbRef.child(PHOTOS_NODE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoUrls.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String url = snap.child("url").getValue(String.class);
                    if (url != null) photoUrls.add(url);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE_REQ && res == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) uploadPhoto(imageUri);
        }
    }

    private void uploadPhoto(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        String imgName = "gallery/" + System.currentTimeMillis() + ".jpg";

        storageRef.child(imgName).putFile(uri)
            .addOnSuccessListener(task ->
                task.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String photoId = dbRef.child(PHOTOS_NODE).push().getKey();
                    HashMap<String, Object> photoMap = new HashMap<>();
                    photoMap.put("url", downloadUri.toString());
                    photoMap.put("timestamp", System.currentTimeMillis());
                    dbRef.child(PHOTOS_NODE).child(photoId).setValue(photoMap);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Photo added ❤️", Toast.LENGTH_SHORT).show();
                }))
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
