package com.steplit.statusgear;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SGFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "StatusGearFCM";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Push received from: " + remoteMessage.getFrom());

        String title = "Status Gear";
        String body = "You have pending tasks";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null ?
                    remoteMessage.getNotification().getTitle() : title;
            body = remoteMessage.getNotification().getBody() != null ?
                    remoteMessage.getNotification().getBody() : body;
        }

        if (remoteMessage.getData().containsKey("title")) {
            title = remoteMessage.getData().get("title");
        }
        if (remoteMessage.getData().containsKey("body")) {
            body = remoteMessage.getData().get("body");
        }

        // Wake up the screen
        wakeScreen();

        // Show full-screen notification
        showFullScreenNotification(title, body);
    }

    private void wakeScreen() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
            "statusgear:notification"
        );
        wakeLock.acquire(10 * 1000L); // 10 seconds
    }

    private void showFullScreenNotification(String title, String body) {
        // Intent to open full-screen alert activity
        Intent fullScreenIntent = new Intent(this, NotificationAlertActivity.class);
        fullScreenIntent.putExtra("title", title);
        fullScreenIntent.putExtra("body", body);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
            this, (int) System.currentTimeMillis(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent to open main app
        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Custom sound
        Uri customSound = Uri.parse("android.resource://" + getPackageName() + "/raw/statusgear_notify");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "status_gear_reminders_v2")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(body)
                            .setBigContentTitle("⏰ " + title))
                        .setAutoCancel(true)
                        .setSound(customSound)
                        .setVibrate(new long[]{0, 500, 200, 500, 200, 500, 200, 1000})
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setContentIntent(tapPendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Full-screen notification shown: " + title);
    }
}
