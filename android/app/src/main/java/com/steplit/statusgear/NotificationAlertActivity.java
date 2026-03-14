package com.steplit.statusgear;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

public class NotificationAlertActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen and wake up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        String title = getIntent().getStringExtra("title");
        String body = getIntent().getStringExtra("body");
        if (title == null) title = "Status Gear Alert";
        if (body == null) body = "You have pending tasks";

        // Build UI programmatically
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(60, 80, 60, 80);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xFF0F172A, 0xFF1E3A5F}
        );
        root.setBackground(bg);

        // App name
        TextView appName = new TextView(this);
        appName.setText("STATUS GEAR");
        appName.setTextSize(12);
        appName.setTextColor(0xFF64748B);
        appName.setTypeface(null, Typeface.BOLD);
        appName.setGravity(Gravity.CENTER);
        appName.setLetterSpacing(0.3f);
        root.addView(appName);

        // Spacer
        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(1, 40));
        root.addView(spacer1);

        // Alert icon
        TextView alertIcon = new TextView(this);
        alertIcon.setText("🔔");
        alertIcon.setTextSize(60);
        alertIcon.setGravity(Gravity.CENTER);
        root.addView(alertIcon);

        // Spacer
        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(1, 30));
        root.addView(spacer2);

        // Title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(24);
        titleView.setTextColor(Color.WHITE);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        root.addView(titleView);

        // Spacer
        View spacer3 = new View(this);
        spacer3.setLayoutParams(new LinearLayout.LayoutParams(1, 24));
        root.addView(spacer3);

        // Body
        TextView bodyView = new TextView(this);
        bodyView.setText(body);
        bodyView.setTextSize(16);
        bodyView.setTextColor(0xFFCBD5E1);
        bodyView.setGravity(Gravity.CENTER);
        bodyView.setLineSpacing(8, 1);
        root.addView(bodyView);

        // Spacer
        View spacer4 = new View(this);
        spacer4.setLayoutParams(new LinearLayout.LayoutParams(1, 50));
        root.addView(spacer4);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        // Dismiss button
        Button dismissBtn = new Button(this);
        dismissBtn.setText("✕  DISMISS");
        dismissBtn.setTextSize(14);
        dismissBtn.setTextColor(Color.WHITE);
        dismissBtn.setTypeface(null, Typeface.BOLD);
        GradientDrawable dismissBg = new GradientDrawable();
        dismissBg.setColor(0xFF64748B);
        dismissBg.setCornerRadius(50);
        dismissBtn.setBackground(dismissBg);
        dismissBtn.setPadding(60, 30, 60, 30);
        dismissBtn.setOnClickListener(v -> {
            stopAlarm();
            finish();
        });

        // Open App button
        Button openBtn = new Button(this);
        openBtn.setText("📱  OPEN APP");
        openBtn.setTextSize(14);
        openBtn.setTextColor(Color.WHITE);
        openBtn.setTypeface(null, Typeface.BOLD);
        GradientDrawable openBg = new GradientDrawable();
        openBg.setColor(0xFF3B82F6);
        openBg.setCornerRadius(50);
        openBtn.setBackground(openBg);
        openBtn.setPadding(60, 30, 60, 30);
        openBtn.setOnClickListener(v -> {
            stopAlarm();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        btnParams.setMargins(10, 0, 10, 0);
        dismissBtn.setLayoutParams(btnParams);
        openBtn.setLayoutParams(btnParams);

        btnRow.addView(dismissBtn);
        btnRow.addView(openBtn);
        root.addView(btnRow);

        setContentView(root);

        // Play alarm sound
        playAlarm();

        // Vibrate
        startVibration();
    }

    private void playAlarm() {
        try {
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/statusgear_notify");
            mediaPlayer = MediaPlayer.create(this, sound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // fallback - ignore
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 500, 200, 500, 200, 500, 1000, 500, 200, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
