package com.may25.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.may25.app.R;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

import java.util.concurrent.TimeUnit;

public class CallActivity extends AppCompatActivity {

    // ⚠️ REPLACE THIS WITH YOUR OWN AGORA APP ID
    private static final String AGORA_APP_ID = "YOUR_AGORA_APP_ID_HERE";
    private static final String CHANNEL_NAME = "may25_private_channel";

    private TextView tvFriendName, tvCallStatus, tvCallTimer;
    private ImageButton btnMute, btnSpeaker, btnEndCall;

    private RtcEngine mRtcEngine;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean callConnected = false;
    private long callStartTime;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private String friendName;
    private boolean isIncoming;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private static final int PERMISSION_REQ = 200;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        mAuth  = FirebaseAuth.getInstance();
        dbRef  = FirebaseDatabase.getInstance().getReference();

        friendName = getIntent().getStringExtra("friendName");
        isIncoming = getIntent().getBooleanExtra("isIncoming", false);
        if (friendName == null) friendName = "My Love ❤️";

        tvFriendName = findViewById(R.id.tv_friend_name);
        tvCallStatus = findViewById(R.id.tv_call_status);
        tvCallTimer  = findViewById(R.id.tv_call_timer);
        btnMute      = findViewById(R.id.btn_mute);
        btnSpeaker   = findViewById(R.id.btn_speaker);
        btnEndCall   = findViewById(R.id.btn_end_call);

        tvFriendName.setText(friendName);
        tvCallStatus.setText(isIncoming ? "Incoming call..." : "Calling...");

        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());

        if (hasPermissions()) {
            initAgora();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQ);
        }
    }

    private boolean hasPermissions() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == PERMISSION_REQ && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            initAgora();
        } else {
            Toast.makeText(this, "Microphone permission required for calls", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initAgora() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext   = getApplicationContext();
            config.mAppId     = AGORA_APP_ID;
            config.mEventHandler = new IRtcEngineEventHandler() {
                @Override
                public void onUserJoined(int uid, int elapsed) {
                    runOnUiThread(() -> {
                        callConnected = true;
                        callStartTime = System.currentTimeMillis();
                        tvCallStatus.setText("Connected ❤️");
                        startTimer();
                    });
                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    runOnUiThread(() -> {
                        Toast.makeText(CallActivity.this, friendName + " ended the call", Toast.LENGTH_SHORT).show();
                        endCall();
                    });
                }

                @Override
                public void onError(int err) {
                    runOnUiThread(() ->
                        Toast.makeText(CallActivity.this, "Call error: " + err, Toast.LENGTH_SHORT).show());
                }
            };

            mRtcEngine = RtcEngine.create(config);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
            mRtcEngine.enableAudio();
            mRtcEngine.joinChannel(null, CHANNEL_NAME, 0, null);

        } catch (Exception e) {
            Toast.makeText(this, "Agora init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void toggleMute() {
        isMuted = !isMuted;
        if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(isMuted);
        btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        Toast.makeText(this, isMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        if (mRtcEngine != null) mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
        btnSpeaker.setImageResource(isSpeakerOn ? R.drawable.ic_speaker_on : R.drawable.ic_speaker);
        Toast.makeText(this, isSpeakerOn ? "Speaker On" : "Earpiece", Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - callStartTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
                tvCallTimer.setText(String.format("%02d:%02d", minutes, seconds));
                tvCallTimer.setVisibility(View.VISIBLE);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endCall();
    }
}
