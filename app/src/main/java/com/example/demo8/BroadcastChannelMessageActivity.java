package com.example.demo8;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.Adapter.BroadcastChannelMessageAdapter;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.example.demo8.MyModels.BroadcastLastMessageModel;
import com.example.demo8.Permissions.Permissions;
import com.example.demo8.Services.SendMediaService;
import com.example.demo8.Utils.FirebaseUtil;
import com.example.demo8.Utils.Util;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BroadcastChannelMessageActivity extends AppCompatActivity {

    Util util;
    String myId;
    BroadcastChannelModel currentModel;
    FirebaseDatabase database;
    FirebaseAuth auth;
    BroadChannelMessageModel broadChannelMessageModel;
    DatabaseReference databaseReference;
    ArrayList<BroadChannelMessageModel> broadChannelMessageModelArrayList;

    EditText edtBroadCastMessage;
    ImageView btnBroadcastSend,brBack,btnBroadcastDataSend,broadcastMenu;
    CircleImageView broadcastProfileImage;
    TextView txtBroadCastName;
    FirebaseUser firebaseUser;
    String currentUserId,currentUserName;
    RecyclerView broadcastMessageActivity;

    BroadcastChannelMessageAdapter messageAdapter;
    Permissions appPermissions;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_channel_message);

        util=new Util();
        myId=util.getUID();
        appPermissions = new Permissions();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        broadcastMenu = findViewById(R.id.broadcastmenu);

        broadChannelMessageModelArrayList = new ArrayList<>();
        //View rootView = findViewById(android.R.id.content);

        edtBroadCastMessage = findViewById(R.id.edtBroadCastMessage);
        btnBroadcastSend = findViewById(R.id.btnBroadCastSend);
        btnBroadcastDataSend = findViewById(R.id.btnBroadCastDataSend);
        txtBroadCastName = findViewById(R.id.broadCastName);
        broadcastProfileImage = findViewById(R.id.broadCast_profile_image);
        brBack = findViewById(R.id.brBack);
        broadcastMessageActivity = findViewById(R.id.broadCastMessageActivity);

        brBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        broadcastProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BroadcastChannelMessageActivity.this,BroadcastInfoActivity.class);
                intent.putExtra("channelModel",currentModel);
                startActivityForResult(intent,90);
                // Handle edit action
            }
        });

        txtBroadCastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BroadcastChannelMessageActivity.this,BroadcastInfoActivity.class);
                intent.putExtra("channelModel",currentModel);

                startActivityForResult(intent,90);
                // Handle edit action
            }
        });

        broadcastMessageActivity.setLayoutManager(new LinearLayoutManager(this));

        String broadcastChannelId = getIntent().getStringExtra("channelId");
        messageAdapter=new BroadcastChannelMessageAdapter(broadChannelMessageModelArrayList,this,myId,broadcastChannelId,broadcastMessageActivity);
        broadcastMessageActivity.setAdapter(messageAdapter);


        String broadcastChannelName = getIntent().getStringExtra("channelName");
        String profilePic = getIntent().getStringExtra("channelImage");
        currentModel = getIntent().getParcelableExtra("channelModel");

        DatabaseReference currentUserRef = database.getReference("UsersDetails").child(FirebaseAuth.getInstance().getUid());
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the current user's name from the snapshot
                currentUserName = snapshot.child("fullname").getValue(String.class);
                // Toast.makeText(ChatDetailActivity.this, "For Current User name:" + currentUserName   , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });

        txtBroadCastName.setText(broadcastChannelName);
        Glide.with(this).load(profilePic).into(broadcastProfileImage);

        broadcastMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(BroadcastChannelMessageActivity.this, v);

                // Inflate the menu resource
                popupMenu.inflate(R.menu.broadcast_message_activity_menu);

                // Set item click listener for the menu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle menu item clicks here
                        switch (item.getItemId()) {
                            case R.id.btnBroadcastInfo:
                                Intent intent = new Intent(BroadcastChannelMessageActivity.this,BroadcastInfoActivity.class);
                                intent.putExtra("channelModel",currentModel);
                                startActivityForResult(intent,90);
                                // Handle edit action
                                return true;

                            case R.id.btnExitBroadcast:
                                // Handle Exit action
                                exitBroadcast();
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

        //}

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference()
                .child("Broadcast Channel")
                .child(currentModel.getChannelId());

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String adminId = dataSnapshot.child("adminId").getValue(String.class);
                    // Toast.makeText(BroadcastChannelMessageActivity.this, "id: " + adminId, Toast.LENGTH_SHORT).show();
                    if (adminId != null && adminId.equals(firebaseUser.getUid())) {
                        // The current user is the admin, allow message sending
                        btnBroadcastSend.setEnabled(true);
                        btnBroadcastDataSend.setEnabled(true);
                    } else {
                        // The current user is not the admin, disable message sending
                        btnBroadcastSend.setVisibility(View.GONE);
                        btnBroadcastDataSend.setVisibility(View.GONE);
                        edtBroadCastMessage.setVisibility(View.GONE);
                        // Toast.makeText(BroadcastChannelMessageActivity.this, "Only admin can send messages.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Admin data does not exist, handle accordingly
                    // For example, you might want to disable message sending by default
                    btnBroadcastSend.setVisibility(View.GONE);
                    btnBroadcastDataSend.setVisibility(View.GONE);
                    edtBroadCastMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        btnBroadcastSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = edtBroadCastMessage.getText().toString();
                edtBroadCastMessage.setText("");

                if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter Message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);

                    getBroadcastMemberTokens(broadcastChannelId,message);

                }
            }
        });

        btnBroadcastDataSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBroadcastMemberTokens(broadcastChannelId,"Photo");
                if (ContextCompat.checkSelfPermission(BroadcastChannelMessageActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission
                    ActivityCompat.requestPermissions(BroadcastChannelMessageActivity.this,
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

        /*DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference("Broadcast Channel")
                .child(broadcastChannelId).child("UnreadMessages");
        unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object unreadData = snapshot.getValue();
                int unreadCount = 0;
                if (unreadData instanceof Integer) {
                    // If the data is a Long, directly cast it to int
                    unreadCount = (Integer) unreadData;
                }

                //if (unreadCount > 0) {
                //     unreadCount=0; // Decrement unread count
                unreadRef.setValue(unreadCount); // Update unread count in Firebase
                // }// Update unread count in Firebase

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });*/


    }
    private void exitBroadcast() {

        new AlertDialog.Builder(this)
                .setTitle("Leave Broadcast Channel")
                .setMessage("Are you sure you want to leave the broadcast channel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Broadcast Channel")
                                .child(currentModel.channelId)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());

                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(BroadcastChannelMessageActivity.this, "Broadcast Channel Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }else {
                                    Toast.makeText(BroadcastChannelMessageActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }


    public void getBroadcastMemberTokens(String channelId,String message) {
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference()
                .child("Broadcast Channel")
                .child(channelId)
                .child("Members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> memberTokens = new ArrayList<>();
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        // Check if the member is the same as the current user
                        if (!memberId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails");
                            reference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String memberToken = snapshot.child("token").getValue(String.class);
                                        if (memberToken != null) {
                                            memberTokens.add(memberToken);
                                            Log.d("Member Token: ", memberToken);
                                        }
                                    }
                                    // Check if this is the last member, then send notifications
                                    if (memberTokens.size() == dataSnapshot.getChildrenCount() - 1) {
                                        sendNotifications(memberTokens, message);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("BroadcastMessage", "Error fetching user details: " + error.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    Log.d("BroadcastMessage", "Members list does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("BroadcastMessage", "Error retrieving members: " + databaseError.getMessage());
            }
        });
    }

    public void sendMessage(String message) {

        String date=String.valueOf(System.currentTimeMillis());
        String broadcastChannelId = getIntent().getStringExtra("channelId");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails")
                .child(FirebaseAuth.getInstance().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Users users = snapshot.getValue(Users.class);
                    String name = users.getFullname();

                    databaseReference= FirebaseDatabase.getInstance().getReference("Broadcast Message").child(broadcastChannelId);
                    DatabaseReference newMessageRef = databaseReference.push();
                    String messageId = newMessageRef.getKey(); // Get the key generated by push()

                    broadChannelMessageModel=new BroadChannelMessageModel("text",message,date,myId,name,messageId);

                    // Push the message to the database
                    newMessageRef.setValue(broadChannelMessageModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        BroadcastLastMessageModel lastMessageModel=new BroadcastLastMessageModel();
        lastMessageModel.date=date;
        lastMessageModel.message=message;
        lastMessageModel.senderId=myId;
        lastMessageModel.type="text";

        databaseReference=FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(broadcastChannelId)
                .child("lastMessageModel");
        databaseReference.setValue(lastMessageModel);

        messageAdapter.notifyDataSetChanged();
        messageAdapter.scrollToBottom();
    }

    public void readMessage(){

        Query query = FirebaseDatabase.getInstance().getReference("Broadcast Message")
                .child(currentModel.getChannelId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    broadChannelMessageModelArrayList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        BroadChannelMessageModel md = ds.getValue(BroadChannelMessageModel.class);

                        md.setMessageId(ds.getKey());
                        broadChannelMessageModelArrayList.add(md);
                    }
                    messageAdapter.notifyDataSetChanged();
                    messageAdapter.scrollToBottom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
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
                .setPath("/ChatMe/Media/Broadcast");                                       //Custom Path For media Storage


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            if (data != null) {
                ArrayList<String> selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                Toast.makeText(this, "Sending Images", Toast.LENGTH_SHORT).show();

                // Create an intent to start the SendMediaService
                Intent intent = new Intent(BroadcastChannelMessageActivity.this, SendMediaService.class);
                intent.putExtra("channelId", currentModel.getChannelId());
                intent.putExtra("messageType", "broadcast");
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
        } else if (requestCode == 90 && resultCode == Activity.RESULT_OK) {
            currentModel = data.getParcelableExtra("channelModel");
            txtBroadCastName.setText(currentModel.getChannelName());
            Glide.with(this).load(currentModel.getChannelImage()).into(broadcastProfileImage);
        }
        else if (requestCode == 101 && resultCode == RESULT_OK) {
            currentModel = data.getParcelableExtra("channelModel");
        }
    }


    @Override
    protected void onResume() {
        readMessage();
        broadcastMessageActivity.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Scroll to the last item in the RecyclerView
                broadcastMessageActivity.scrollToPosition(messageAdapter.getItemCount() - 1);
                // Remove the listener to avoid multiple calls
                broadcastMessageActivity.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        super.onResume();
    }

    public void sendNotifications(List<String> tokens, String message) {
        FirebaseUtil.groupDetailsReference(currentModel.getChannelId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    for (String token : tokens) {
                        JSONObject jsonObject = new JSONObject();
                        JSONObject notificationObject = new JSONObject();
                        notificationObject.put("title", currentModel.getChannelName());
                        notificationObject.put("body", currentUserName + ": " + message);

                        JSONObject dataobject = new JSONObject();
                        dataobject.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        dataobject.put("channelId", currentModel.getChannelId());

                        jsonObject.put("notification", notificationObject);
                        jsonObject.put("data", dataobject);
                        jsonObject.put("to", token); // Set the token for each member

                        Log.d("Notification", "Sending notification to token: " + token); // Log the token
                        callApi(jsonObject);
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
                        Toast.makeText(BroadcastChannelMessageActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
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
                            //Toast.makeText(BroadcastChannelMessageActivity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("Notification", "Failed to send notification. Response code: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BroadcastChannelMessageActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}