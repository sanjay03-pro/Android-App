package com.may25.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.may25.app.R;
import com.may25.app.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENT     = 1;
    private static final int VIEW_RECEIVED = 2;
    private static final int VIEW_SENT_IMG = 3;
    private static final int VIEW_RECV_IMG = 4;

    private List<Message> messages;
    private String currentUid;

    public MessageAdapter(List<Message> messages, String currentUid) {
        this.messages   = messages;
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int pos) {
        Message m = messages.get(pos);
        boolean isMe = m.getSenderId().equals(currentUid);
        boolean isImg = "image".equals(m.getType());
        if (isMe)  return isImg ? VIEW_SENT_IMG : VIEW_SENT;
        else       return isImg ? VIEW_RECV_IMG : VIEW_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case VIEW_SENT:
                view = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new TextViewHolder(view);
            case VIEW_RECEIVED:
                view = inflater.inflate(R.layout.item_message_received, parent, false);
                return new TextViewHolder(view);
            case VIEW_SENT_IMG:
                view = inflater.inflate(R.layout.item_image_sent, parent, false);
                return new ImageViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_image_received, parent, false);
                return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        Message msg = messages.get(pos);
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(msg.getTimestamp()));

        if (holder instanceof TextViewHolder) {
            TextViewHolder tvh = (TextViewHolder) holder;
            tvh.tvMessage.setText(msg.getMessage());
            tvh.tvTime.setText(time);
        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder ivh = (ImageViewHolder) holder;
            Glide.with(ivh.ivImage.getContext())
                 .load(msg.getMessage())
                 .placeholder(R.drawable.ic_image_placeholder)
                 .into(ivh.ivImage);
            ivh.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        TextViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime;
        ImageViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_image);
            tvTime  = v.findViewById(R.id.tv_time);
        }
    }
}
