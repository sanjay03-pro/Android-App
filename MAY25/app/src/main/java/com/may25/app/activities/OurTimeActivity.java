package com.may25.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.may25.app.R;
import com.may25.app.utils.TimerUtils;

/**
 * A permanent, always-live "Our Time" screen — behaves like a digital clock
 * that keeps counting days/hours/minutes/seconds since 25 May 2023.
 * Replaces the old popup dialog approach.
 */
public class OurTimeActivity extends AppCompatActivity {

    private TextView tvOurTime;
    private final Handler handler = new Handler();
    private Runnable tickRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_our_time);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Our Time");
        }

        tvOurTime = findViewById(R.id.tv_our_time_live);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start ticking every second while this screen is visible
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                tvOurTime.setText(TimerUtils.getElapsedTimeFull());
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(tickRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop ticking when the screen isn't visible, to save battery
        handler.removeCallbacks(tickRunnable);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
