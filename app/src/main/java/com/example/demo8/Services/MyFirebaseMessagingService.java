/*package com.example.demo8.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.demo8.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Extract data from the message
        String senderName = remoteMessage.getData().get("senderName");
        String messageBody = remoteMessage.getData().get("messageBody");
        String imageUrl = remoteMessage.getData().get("imageUrl");
        String timestamp = remoteMessage.getData().get("timestamp");

        // Convert the timestamp to a readable format if needed
        Date date = new Date(Long.parseLong(timestamp));
        String dateString = DateFormat.getDateTimeInstance().format(date);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);
        notificationLayout.setImageViewResource(R.id.sender_image, R.drawable.avatar3);
        notificationLayout.setTextViewText(R.id.sender_name, senderName);
        notificationLayout.setTextViewText(R.id.message_text, messageBody);
        notificationLayout.setTextViewText(R.id.timestamp_text, dateString);


        // Load sender image
        Bitmap senderImageBitmap = null;
        try {
            senderImageBitmap = getBitmapFromUrl(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        if (senderImageBitmap != null) {
            builder.setLargeIcon(senderImageBitmap);
        }

        // Add the timestamp as a subtext
        builder.setSubText(dateString);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }

            // Notify
            notificationManager.notify(0, builder.build());
        }
    }
    private Bitmap getBitmapFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        return BitmapFactory.decodeStream(input);
    }
}
*/