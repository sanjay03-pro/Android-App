package com.may25.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.may25.app.R;

public class PhotoViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        String url = getIntent().getStringExtra("url");
        PhotoView photoView = findViewById(R.id.photo_view);

        if (url != null) {
            Glide.with(this).load(url).into(photoView);
        }
    }
}
