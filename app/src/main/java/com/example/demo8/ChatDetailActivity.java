package com.example.demo8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.demo8.Adapter.MessageAdapter;
import com.example.demo8.Fragment.ChatFragment;
import com.example.demo8.MyModels.ChatLastMessageModel;
import com.example.demo8.MyModels.ChatUnreadMessageModel;
import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.MessageModel;
import com.example.demo8.Services.SendMediaService;
import com.example.demo8.Utils.FirebaseUtil;
import com.example.demo8.Utils.Util;
import com.example.demo8.databinding.ActivityChatDetailBinding;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Util util;
    String myId, senderId, receiverId, userName, profilePic, senderRoom, receiverRoom, mobile, token, currentUserName, currentUserId;
    FirebaseUser firebaseUser;
    ArrayList<MessageModel> messageModels;
    MessageAdapter messageAdapter;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        currentUserId = auth.getCurrentUser().getUid();

        // Retrieve the current user's data from the database
        DatabaseReference currentUserRef = database.getReference("UsersDetails").child(FirebaseAuth.getInstance().getUid());
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the current user's name from the snapshot
                currentUserName = snapshot.child("fullname").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });


        util = new Util();
        myId = util.getUID();


        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");
        mobile = getIntent().getStringExtra("userMobile");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails");
        reference.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String receiverToken = snapshot.child("token").getValue(String.class);
                    if (receiverToken != null) {
                        token = receiverToken;

                        Log.d("Receiver Token: ", token);
                    }
                }
                // Check if this is the last member, then send notifications
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatMessage", "Error fetching user details: " + error.getMessage());
            }
        });

        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("receiverId", receiverId);
        chatFragment.setArguments(args);


        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, ChatInfoActivity.class);
                intent.putExtra("chatImage", profilePic);
                intent.putExtra("chatName", userName);
                intent.putExtra("id", receiverId);
                intent.putExtra("mobile", mobile);
                //intent.putExtra("key",key);
                startActivity(intent);


            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, ChatInfoActivity.class);
                intent.putExtra("chatImage", profilePic);
                intent.putExtra("chatName", userName);
                intent.putExtra("id", receiverId);
                //intent.putExtra("key",key);
                startActivity(intent);

            }
        });

        messageModels = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageModels, this, receiverId,binding.chatRecyclerView);

        binding.chatRecyclerView.setAdapter(messageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        //Retrieving data (read message)
        database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid())
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);
                        }
                        // messageAdapter.notifyItemInserted(messageModels.size()-1);
                        messageAdapter.notifyDataSetChanged();
                        messageAdapter.scrollToBottom();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.chatRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Scroll to the last item in the RecyclerView
                binding.chatRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
                // Remove the listener to avoid multiple calls
                binding.chatRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        binding.chatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ChatDetailActivity.this, v);

                // Inflate the menu resource
                popupMenu.inflate(R.menu.chat_message_menu);

                // Set item click listener for the menu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle menu item clicks here
                        switch (item.getItemId()) {
                            case R.id.btnBlockContact:

                                // Handle edit action
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                // Show the PopupMenu
                popupMenu.show();
            }
        });

        binding.btnAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnAudioCall.setVisibility(View.INVISIBLE);
                setVoiceCall(mobile);
            }
        });

        binding.btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnVideoCall.setVisibility(View.INVISIBLE);
                setVideoCall(mobile);
            }
        });



        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = String.valueOf(System.currentTimeMillis());
                String message = binding.enterMessage.getText().toString();
                final MessageModel model = new MessageModel(senderId, message,"text");
                model.setTimestamp(new Date().getTime());
                binding.enterMessage.setText("");


                DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference("Chats")
                        .child(receiverId).child("UnreadMessages");
                unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ChatUnreadMessageModel unreadMessageModel = new ChatUnreadMessageModel();
                        unreadMessageModel.senderId = senderId;
                        int unreadCount = 0;
                        if (snapshot.exists()) {
                            // Check if the value is an Integer or a Map
                            Object value = snapshot.getValue();
                            if (value instanceof Integer) {
                                // If it's an Integer, use it directly
                                unreadCount = (Integer) value;
                            } else if (value instanceof Map) {
                                // If it's a Map, extract the integer value
                                try {
                                    unreadCount = Integer.parseInt(((Map<String, Object>) value).get("unreadMessageCount").toString());
                                } catch (NumberFormatException e) {
                                    // Handle parsing error
                                    e.printStackTrace();
                                }
                            }
                        }
                        // Increment the unread count
                        unreadCount++;
                        unreadMessageModel.unreadMessageCount = unreadCount;
                        unreadRef.setValue(unreadMessageModel); // Update unread count in Firebase
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });

                if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter Message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendNotification(message);
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
            }

            private void updateChat(String userId, String room, MessageModel messageModel) {
                database.getReference().child("Chats").child(userId).child(room).push()
                        .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                messageAdapter.notifyDataSetChanged();
                messageAdapter.scrollToBottom();

            }
        });

        binding.btnImgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification("Photo");
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    // Permission has already been granted, proceed with your code
                    // For example, call your getGalleryImage() method here
                    getGalleryImage();
                    messageAdapter.notifyDataSetChanged();
                    messageAdapter.scrollToBottom();

                }

            }
        });


    }

    private void getGalleryImage() {

        Options options = Options.init()
                .setRequestCode(300)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setExcludeVideos(true)                                       //Option to exclude videos
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/QrToContact/Media/Individual");                                       //Custom Path For media Storage


        Pix.start(this, options);
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // Check if the permission has been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your code
                // For example, call your getGalleryImage() method here
                getGalleryImage();
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable functionality)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void setVoiceCall (String targetId) {

        binding.btnAudioCall.setIsVideoCall(false);
        binding.btnAudioCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        binding.btnAudioCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetId)));

    }
    void setVideoCall(String targetId) {

        binding.btnVideoCall.setIsVideoCall(true);
        binding.btnVideoCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        binding.btnVideoCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetId)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            if (data != null) {
                ArrayList<String> selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                Toast.makeText(this, "Sending Images", Toast.LENGTH_SHORT).show();

                // Create an intent to start the SendMediaService
                Intent intent = new Intent(ChatDetailActivity.this, SendMediaService.class);
                intent.putExtra("senderId", senderId);
                intent.putExtra("senderRoom", senderRoom);
                intent.putExtra("receiverRoom", receiverRoom);
                intent.putExtra("receiverId", receiverId);
                intent.putStringArrayListExtra("media", selectedImages);

                // Start the service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
                messageAdapter.notifyDataSetChanged();
                messageAdapter.scrollToBottom();
            }
        }
    }


    private void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject notificationObject = new JSONObject();
                    notificationObject.put("title", currentUserName);
                    notificationObject.put("body", message);

                    JSONObject dataobject = new JSONObject();
                    dataobject.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    jsonObject.put("notification", notificationObject);
                    jsonObject.put("data", dataobject);

                    // Check if FCM token is not null before adding it to the JSON object
                    if (token != null) {
                        // Use the FCM token from otherUsers
                        jsonObject.put("to", token);
                        Log.i("FCM Token", token);
                        callApi(jsonObject);
                    } else {
                        Log.e("Notification", "FCM Token is null or empty");
                    }

                } catch (Exception e) {
                    Log.e("Notification", "Exception: " + e.getMessage());
                }
            }
        });
    }


    // current username,meaasge,currentuserid,otherusertoken


    void callApi(JSONObject jsonObject) {
        okhttp3.MediaType JSON = MediaType.get("application/json");

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAEWwTDeE:APA91bGCtq4CAHoLoRDaqxwBH9H6xrCfxorGt-B6lQkrJfTXIggXhElMb7YtvlRAenVujMuzMKthohxmnSPfV6sqLzfyJNapkwHWW9CIykkkKJehGNfPysa2mkQZF7BXCjL57d2TozjZ")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Notification", "Failed to send notification: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatDetailActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Notification", "Notification sent successfully");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(ChatDetailActivity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("Notification", "Failed to send notification. Response code: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatDetailActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        binding.btnAudioCall.setVisibility(View.VISIBLE);
        binding.btnAudioCall.setBackground(ContextCompat.getDrawable(this, R.drawable.phone_108));
        binding.btnVideoCall.setVisibility(View.VISIBLE);
        binding.btnVideoCall.setBackground(ContextCompat.getDrawable(this, R.drawable.video_call_10));

    }
}