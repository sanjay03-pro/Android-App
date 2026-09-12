package com.may25.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.may25.app.R;
import com.may25.app.utils.TimerUtils;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth   = FirebaseAuth.getInstance();
        dbRef   = FirebaseDatabase.getInstance().getReference();

        toolbar       = findViewById(R.id.toolbar);
        drawerLayout  = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Update timer in nav header
        updateNavTimer();

        // Set user online, and let Firebase automatically flip it back to
        // offline the moment this device truly disconnects (app killed,
        // network lost, etc.) — far more reliable than only relying on onDestroy().
        setUserOnline(true);
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            dbRef.child("users").child(uid).child("online").onDisconnect().setValue(false);
        }

        // Default: open Chat fragment
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_chat);
            openChat();
        }
    }

    private void updateNavTimer() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvTimer = headerView.findViewById(R.id.tv_timer);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                String elapsed = TimerUtils.getElapsedTime();
                tvTimer.setText(elapsed);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            openChat();
        } else if (id == R.id.nav_timer) {
            // Opens a dedicated live-updating screen (not a popup)
            startActivity(new Intent(this, OurTimeActivity.class));
        } else if (id == R.id.nav_photos) {
            startActivity(new Intent(this, GalleryActivity.class));
        } else if (id == R.id.nav_logout) {
            setUserOnline(false);
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openChat() {
        startActivity(new Intent(this, ChatActivity.class));
    }

    private void showTimerDialog() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("❤️ Together Since 25 May 2023")
                .setMessage(TimerUtils.getElapsedTimeFull())
                .setPositiveButton("OK", null)
                .create();

        // Keep the message ticking live every second while the dialog is open
        Handler dialogHandler = new Handler();
        Runnable dialogRunnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.setMessage(TimerUtils.getElapsedTimeFull());
                    dialogHandler.postDelayed(this, 1000);
                }
            }
        };
        dialog.setOnDismissListener(d -> dialogHandler.removeCallbacks(dialogRunnable));

        dialog.show();
        dialogHandler.post(dialogRunnable);
    }

    private void setUserOnline(boolean online) {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            dbRef.child("users").child(uid).child("online").setValue(online);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-mark online every time the app comes back to the foreground
        setUserOnline(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        setUserOnline(false);
    }
}
