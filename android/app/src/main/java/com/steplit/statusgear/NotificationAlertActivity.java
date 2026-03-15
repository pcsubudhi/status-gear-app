package com.steplit.statusgear;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NotificationAlertActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wake screen + show over lock screen
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        String title = getIntent().getStringExtra("title");
        String body = getIntent().getStringExtra("body");
        if (title == null) title = "Status Gear";
        if (body == null || body.trim().isEmpty()) {
            finish();
            return;
        }

        // ═══ BUILD UI ═══
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF0F172A); // Dark navy
        root.setPadding(dp(24), dp(50), dp(24), dp(30));

        // "STATUS GEAR" label
        TextView appLabel = new TextView(this);
        appLabel.setText("S T A T U S   G E A R");
        appLabel.setTextColor(0xFF64748B);
        appLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        appLabel.setGravity(Gravity.CENTER);
        appLabel.setLetterSpacing(0.3f);
        root.addView(appLabel);

        // Bell icon
        TextView bellIcon = new TextView(this);
        bellIcon.setText("🔔");
        bellIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
        bellIcon.setGravity(Gravity.CENTER);
        bellIcon.setPadding(0, dp(20), 0, dp(10));
        root.addView(bellIcon);

        // Title — large
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, 0, 0, dp(16));
        root.addView(titleView);

        // Body — scrollable, large readable font
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        scrollView.setFillViewport(false);

        TextView bodyView = new TextView(this);
        bodyView.setText(body);
        bodyView.setTextColor(0xFFE2E8F0);
        bodyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bodyView.setLineSpacing(dp(4), 1f);
        bodyView.setPadding(dp(8), 0, dp(8), dp(20));
        scrollView.addView(bodyView);
        root.addView(scrollView);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, dp(16), 0, 0);

        // DISMISS button
        Button dismissBtn = new Button(this);
        dismissBtn.setText("✕  DISMISS");
        dismissBtn.setTextColor(0xFFFFFFFF);
        dismissBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dismissBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        dismissBtn.setBackgroundColor(0xFF475569);
        dismissBtn.setPadding(dp(24), dp(14), dp(24), dp(14));
        dismissBtn.setOnClickListener(v -> {
            stopSound();
            finish();
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnParams.setMargins(0, 0, dp(8), 0);
        dismissBtn.setLayoutParams(btnParams);
        btnRow.addView(dismissBtn);

        // OPEN APP button
        Button openBtn = new Button(this);
        openBtn.setText("📱  OPEN APP");
        openBtn.setTextColor(0xFFFFFFFF);
        openBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        openBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        openBtn.setBackgroundColor(0xFF3B82F6);
        openBtn.setPadding(dp(24), dp(14), dp(24), dp(14));
        openBtn.setOnClickListener(v -> {
            stopSound();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        openParams.setMargins(dp(8), 0, 0, 0);
        openBtn.setLayoutParams(openParams);
        btnRow.addView(openBtn);

        root.addView(btnRow);
        setContentView(root);

        // ═══ PLAY SOUND — 3 seconds only, then stop ═══
        playSound();

        // ═══ VIBRATE ═══
        try {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(new long[]{0, 500, 200, 500, 200, 500}, -1); // vibrate once, don't repeat
        } catch (Exception e) {}

        // ═══ AUTO-DISMISS after 60 seconds ═══
        handler.postDelayed(() -> {
            stopSound();
            finish();
        }, 60000);
    }

    private void playSound() {
        try {
            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/raw/statusgear_notify");
            mediaPlayer = MediaPlayer.create(this, soundUri);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(false); // Play ONCE, not looping
                mediaPlayer.start();
                // Stop after 3 seconds as extra safety
                handler.postDelayed(this::stopSound, 3000);
            }
        } catch (Exception e) {
            // Fallback: no sound
        }
    }

    private void stopSound() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {}
        try {
            if (vibrator != null) vibrator.cancel();
        } catch (Exception e) {}
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        stopSound();
        finish();
    }
}
