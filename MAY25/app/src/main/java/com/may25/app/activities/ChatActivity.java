package com.may25.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.may25.app.R;
import com.may25.app.adapters.MessageAdapter;
import com.may25.app.models.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnCall, btnImage;
    private TextView tvFriendName, tvFriendStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private String currentUid;
    private String friendUid;
    private String friendName;

    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    private static final String CHAT_NODE = "chats/may25_private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUid = mAuth.getCurrentUser().getUid();

        // Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        tvFriendName   = findViewById(R.id.tv_friend_name);
        tvFriendStatus = findViewById(R.id.tv_friend_status);
        rvMessages     = findViewById(R.id.rv_messages);
        etMessage      = findViewById(R.id.et_message);
        btnSend        = findViewById(R.id.btn_send);
        btnCall        = findViewById(R.id.btn_call);
        btnImage       = findViewById(R.id.btn_image);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messages, currentUid);
        rvMessages.setAdapter(adapter);

        // Find friend UID
        loadFriendInfo();

        // Send message
        btnSend.setOnClickListener(v -> sendMessage());

        // Image share
        btnImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 100);
        });

        // Voice call
        btnCall.setOnClickListener(v -> {
            if (friendUid == null) {
                Toast.makeText(this, "Friend not found yet", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent callIntent = new Intent(this, CallActivity.class);
            callIntent.putExtra("friendUid", friendUid);
            callIntent.putExtra("friendName", friendName);
            callIntent.putExtra("isIncoming", false);
            startActivity(callIntent);
        });

        // Listen for messages
        listenForMessages();
    }

    private void loadFriendInfo() {
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.child("uid").getValue(String.class);
                    if (uid != null && !uid.equals(currentUid)) {
                        friendUid  = uid;
                        friendName = userSnap.child("name").getValue(String.class);
                        if (friendName == null) friendName = "My Love ❤️";
                        tvFriendName.setText(friendName);

                        // Listen for friend's online status
                        dbRef.child("users").child(friendUid).child("online")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    Boolean online = snap.getValue(Boolean.class);
                                    tvFriendStatus.setText(Boolean.TRUE.equals(online) ? "Online ✅" : "Offline");
                                }
                                @Override public void onCancelled(@NonNull DatabaseError e) {}
                            });
                        break;
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String msgId = dbRef.child(CHAT_NODE).push().getKey();
        HashMap<String, Object> msgMap = new HashMap<>();
        msgMap.put("messageId", msgId);
        msgMap.put("senderId", currentUid);
        msgMap.put("message", text);
        msgMap.put("type", "text");
        msgMap.put("timestamp", System.currentTimeMillis());

        dbRef.child(CHAT_NODE).child(msgId).setValue(msgMap);
        etMessage.setText("");
    }

    private void listenForMessages() {
        dbRef.child(CHAT_NODE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                Message msg = snapshot.getValue(Message.class);
                if (msg != null) {
                    messages.add(msg);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 100 && res == RESULT_OK && data != null) {
            // Upload image to Firebase Storage and send as message
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadAndSendImage(imageUri);
            }
        }
    }

    private void uploadAndSendImage(android.net.Uri imageUri) {
        String imgName = "chat_images/" + System.currentTimeMillis() + ".jpg";
        com.google.firebase.storage.FirebaseStorage.getInstance()
            .getReference(imgName)
            .putFile(imageUri)
            .addOnSuccessListener(taskSnapshot ->
                taskSnapshot.getStorage().getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        String msgId = dbRef.child(CHAT_NODE).push().getKey();
                        HashMap<String, Object> msgMap = new HashMap<>();
                        msgMap.put("messageId", msgId);
                        msgMap.put("senderId", currentUid);
                        msgMap.put("message", uri.toString());
                        msgMap.put("type", "image");
                        msgMap.put("timestamp", System.currentTimeMillis());
                        dbRef.child(CHAT_NODE).child(msgId).setValue(msgMap);
                    }))
            .addOnFailureListener(e ->
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
