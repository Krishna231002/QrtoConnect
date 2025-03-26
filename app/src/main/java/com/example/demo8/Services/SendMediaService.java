package com.example.demo8.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.demo8.BroadChannelMessageModel;
import com.example.demo8.GroupMessageModel;
import com.example.demo8.MyModels.BroadcastLastMessageModel;
import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.MessageModel;
import com.example.demo8.R;
import com.example.demo8.Users;
import com.example.demo8.Utils.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class SendMediaService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private String groupId,senderId,receiverId,senderRoom,receiverRoom,channelId;
    private final Util util = new Util();
    private ArrayList<String> images;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser=auth.getCurrentUser();


    public SendMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the service in the foreground with an empty notification
        startForeground(600, getEmptyNotification());

        images = intent.getStringArrayListExtra("media");

        if (intent.hasExtra("type")) {
            groupId = intent.getStringExtra("groupId");
            sendGroupImage();
        } else if (intent.hasExtra("messageType")) {
            channelId = intent.getStringExtra("channelId");
            sendBroadcastImage();
        } else {
            senderId = intent.getStringExtra("senderId");
            receiverId = intent.getStringExtra("receiverId");
            receiverRoom = intent.getStringExtra("receiverRoom");
            senderRoom = intent.getStringExtra("senderRoom");

            sendImage();
        }

        return START_NOT_STICKY;
    }

    private Notification getEmptyNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("empty", "Empty Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "empty")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("") // Empty title
                .setContentText("") // Empty text
                .setOngoing(true); // Make it ongoing so it doesn't get swiped away
        return builder.build();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel("android", "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setLightColor(R.color.colorPrimary);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setDescription("Sending Media");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }
    private void sendImage() {
        for (String imageUri : images) {
            uploadImage(imageUri);
        }
        stopSelf();
    }

    private void uploadImage(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        String fileName = uri.getLastPathSegment();

        // Use a unique file name to avoid overwriting existing files
        String uniqueFileName = util.getUID() + "_" + System.currentTimeMillis() + "_" + fileName;

        File imageFile = new File(uri.getPath()); // Convert the file Uri into a File object

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Chat Images")
                .child(senderId).child(senderRoom).child("Media").child(util.getUID()).child(uniqueFileName);

        storageReference.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> downloadUriTask = taskSnapshot.getStorage().getDownloadUrl();
            downloadUriTask.addOnSuccessListener(uriResult -> {

                String imageUrl = uriResult.toString();
                final MessageModel model = new MessageModel(senderId, imageUrl,"image");
                model.setTimestamp(new Date().getTime());

                if (imageUrl == null) {
                    Toast.makeText(getApplicationContext(), "Select Image", Toast.LENGTH_SHORT).show();
                } else {

                    if (senderId.equals(firebaseUser.getUid())) {
                        if (senderId.equals(receiverId)) {
                            // If sender and receiver are the same, update only sender's chat room
                            updateChat(senderId, senderRoom, model);
                        } else {
                            // If sender and receiver are different, update both sender's and receiver's chat rooms
                            updateChat(senderId, senderRoom, model);
                            updateChat(receiverId, receiverRoom, model);
                        }
                    } else {
                        // If sender is not the current user, update sender's chat room only
                        updateChat(senderId, senderRoom, model);
                    }

                }

            });
        });


    }
    private void updateChat(String userId, String room, MessageModel messageModel) {
        database.getReference().child("Chats").child(userId).child(room).push()
                .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //  Toast.makeText(ChatDetailActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendGroupImage() {
        for (String imageUri : images) {
            uploadGroupImage(imageUri);
        }
        stopSelf();
    }

    private void uploadGroupImage(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        String fileName = uri.getLastPathSegment();

        // Use a unique file name to avoid overwriting existing files
        String uniqueFileName = util.getUID() + "_" + System.currentTimeMillis() + "_" + fileName;

        File imageFile = new File(uri.getPath()); // Convert the file Uri into a File object

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Group Chat Images")
                .child(groupId).child("Media").child(util.getUID()).child(uniqueFileName);

        storageReference.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> downloadUriTask = taskSnapshot.getStorage().getDownloadUrl();
            downloadUriTask.addOnSuccessListener(uriResult -> {
                String imageUrl = uriResult.toString();


                DatabaseReference userreference = FirebaseDatabase.getInstance().getReference("UsersDetails")
                        .child(FirebaseAuth.getInstance().getUid());
                userreference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Users users = snapshot.getValue(Users.class);
                            String name = users.getFullname();

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group Message").child(groupId);
                            DatabaseReference newMessageRef = databaseReference.push();
                            String messageId = newMessageRef.getKey(); // Get the key generated by push()

                            // Set the xmessageId to the generated key
                            GroupMessageModel messageModel = new GroupMessageModel("image", imageUrl, String.valueOf(System.currentTimeMillis()), util.getUID(),name,messageId);

                            // Push the message to the database
                            newMessageRef.setValue(messageModel);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors
                    }
                });

                String date=String.valueOf(System.currentTimeMillis());

                GroupLastMessageModel lastMessageModel=new GroupLastMessageModel();
                lastMessageModel.date=date;
                lastMessageModel.message=imageUrl;
                lastMessageModel.senderId=FirebaseAuth.getInstance().getUid();
                lastMessageModel.type="image";

                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Group Details").child(groupId)
                        .child("lastMessageModel");
                databaseReference.setValue(lastMessageModel);
            });
        });
    }

    private void sendBroadcastImage() {
        for (String imageUri : images) {
            uploadBroadcastImage(imageUri);
        }
        stopSelf();
    }

    private void uploadBroadcastImage(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        String fileName = uri.getLastPathSegment();

        // Use a unique file name to avoid overwriting existing files
        String uniqueFileName = util.getUID() + "_" + System.currentTimeMillis() + "_" + fileName;

        File imageFile = new File(uri.getPath()); // Convert the file Uri into a File object

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Broadcast Chat Images")
                .child(channelId).child("Media").child(util.getUID()).child(uniqueFileName);

        storageReference.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> downloadUriTask = taskSnapshot.getStorage().getDownloadUrl();
            downloadUriTask.addOnSuccessListener(uriResult -> {
                String imageUrl = uriResult.toString();

                DatabaseReference userreference = FirebaseDatabase.getInstance().getReference("UsersDetails")
                        .child(FirebaseAuth.getInstance().getUid());
                userreference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Users users = snapshot.getValue(Users.class);
                            String name = users.getFullname();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Message").child(channelId);
                            DatabaseReference newMessageRef = reference.push();
                            String messageId = newMessageRef.getKey(); // Get the key generated by push()

                            // Set the messageId to the generated key
                            BroadChannelMessageModel messageModel = new BroadChannelMessageModel("image", imageUrl, String.valueOf(System.currentTimeMillis()), util.getUID(),name,messageId);

                            // Push the message to the database
                            newMessageRef.setValue(messageModel);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors
                    }
                });

                String date=String.valueOf(System.currentTimeMillis());

                BroadcastLastMessageModel lastMessageModel=new BroadcastLastMessageModel();
                lastMessageModel.date=date;
                lastMessageModel.message=imageUrl;
                lastMessageModel.senderId=FirebaseAuth.getInstance().getUid();
                lastMessageModel.type="image";

                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(channelId)
                        .child("lastMessageModel");
                databaseReference.setValue(lastMessageModel);
            });
        });
    }

}
