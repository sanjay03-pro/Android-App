package com.may25.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.may25.app.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<String> photoUrls;

    public PhotoAdapter(Context context, List<String> photoUrls) {
        this.context   = context;
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int pos) {
        String url = photoUrls.get(pos);
        Glide.with(context).load(url)
             .centerCrop()
             .placeholder(R.drawable.ic_image_placeholder)
             .into(holder.ivPhoto);

        holder.ivPhoto.setOnClickListener(v -> {
            // Open full-screen photo viewer
            Intent intent = new Intent(context, com.may25.app.activities.PhotoViewerActivity.class);
            intent.putExtra("url", url);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return photoUrls.size(); }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        PhotoViewHolder(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_photo);
        }
    }
}
