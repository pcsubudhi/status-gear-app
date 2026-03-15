package com.steplit.statusgear;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends BridgeActivity {
    private static final String TAG = "StatusGear";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.w(TAG, "Notification permission denied — opening settings");
                openNotificationSettings();
            }
            registerFCMToken();
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannels();
        requestNotificationPermission();

        // Add JavaScript interface so web code can open settings
        getBridge().getWebView().addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void openNotificationSettings() {
                MainActivity.this.openNotificationSettings();
            }
            @JavascriptInterface
            public void openBatterySettings() {
                try {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not open battery settings", e);
                }
            }
        }, "StatusGearNative");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerFCMToken();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted");
                registerFCMToken();
                checkFullScreenPermission();
            } else {
                Log.d(TAG, "Requesting notification permission...");
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            registerFCMToken();
        }
    }

    private void checkFullScreenPermission() {
        if (Build.VERSION.SDK_INT >= 34) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (!nm.canUseFullScreenIntent()) {
                Log.d(TAG, "Full screen intent not allowed — requesting");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.w(TAG, "Could not open full screen intent settings", e);
                }
            }
        }
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "Could not open settings", e2);
            }
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            Uri customSound = Uri.parse("android.resource://" + getPackageName() + "/raw/statusgear_notify");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

            NotificationChannel reminders = new NotificationChannel(
                "status_gear_reminders_v2",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            reminders.setDescription("Hourly pending task reminders");
            reminders.setSound(customSound, audioAttributes);
            reminders.enableVibration(true);
            reminders.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
            reminders.enableLights(true);
            reminders.setLightColor(0xFFFF4444);
            reminders.setShowBadge(true);
            reminders.setBypassDnd(true);
            reminders.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(reminders);

            NotificationChannel assignments = new NotificationChannel(
                "status_gear_assignments_v2",
                "New Assignments",
                NotificationManager.IMPORTANCE_HIGH
            );
            assignments.setDescription("New ticket and task alerts");
            assignments.setSound(customSound, audioAttributes);
            assignments.enableVibration(true);
            assignments.setVibrationPattern(new long[]{0, 300, 100, 300, 100, 300, 100, 500});
            assignments.enableLights(true);
            assignments.setLightColor(0xFF2563EB);
            assignments.setShowBadge(true);
            assignments.setBypassDnd(true);
            assignments.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(assignments);

            // Delete old channels
            manager.deleteNotificationChannel("status_gear_reminders");
            manager.deleteNotificationChannel("status_gear_assignments");
        }
    }

    private void registerFCMToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "FCM token fetch failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.d(TAG, "FCM Token: " + token);

                getBridge().getWebView().postDelayed(() -> {
                    getBridge().getWebView().evaluateJavascript(
                        "window._FCM_TOKEN = '" + token + "';" +
                        "console.log('FCM token injected from native');" +
                        "if(window._onFCMToken) window._onFCMToken('" + token + "');",
                        null
                    );
                }, 3000);
            });
    }
}
