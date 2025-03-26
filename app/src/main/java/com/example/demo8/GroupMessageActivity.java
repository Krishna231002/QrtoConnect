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
import com.example.demo8.Adapter.GroupMessageAdapter;

import com.example.demo8.MyModels.GroupLastMessageModel;

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
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

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


public class GroupMessageActivity extends AppCompatActivity {

    Util util;
    String myId,senderId;
    GroupModels currentModel;
    FirebaseDatabase database;
    FirebaseAuth auth;
    GroupMessageModel groupMessageModel;
    DatabaseReference databaseReference;
    ArrayList<GroupMessageModel> groupMessageModelsList;
    EditText edtGroupMessage;
    ImageView btnGroupSend,grBack,btnGroupDataSend,imgMenu;
    CircleImageView groupProfileImage;
    TextView txtGroupName;
    FirebaseUser firebaseUser;
    String currentUserName;
    RecyclerView groupMessageActivity;

    GroupMessageAdapter messageAdapter;
    Permissions appPermissions;
    ZegoSendCallInvitationButton btnVideoCall,btnAudioCall;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        util=new Util();
        myId=util.getUID();
        appPermissions = new Permissions();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        imgMenu = findViewById(R.id.menuImageView);

        groupMessageModelsList = new ArrayList<>();

        //View rootView = findViewById(android.R.id.content);

        edtGroupMessage = findViewById(R.id.edtGroupMessage);
        btnGroupSend = findViewById(R.id.btnGroupSend);
        btnGroupDataSend = findViewById(R.id.btnGroupDataSend);
        txtGroupName = findViewById(R.id.groupName);
        groupProfileImage = findViewById(R.id.group_profile_image);
        grBack = findViewById(R.id.grBack);
        groupMessageActivity = findViewById(R.id.groupMessageActivity);
        btnVideoCall = findViewById(R.id.btn_group_video_call);
        btnAudioCall = findViewById(R.id.btn_group_audio_call);

        btnVideoCall.setVisibility(View.VISIBLE);

        btnVideoCall.post(new Runnable() {
            @Override
            public void run() {
                btnVideoCall.setBackground(ContextCompat.getDrawable(GroupMessageActivity.this, R.drawable.video_call_10));
            }
        });


        String groupId = getIntent().getStringExtra("groupIdToMessage");

        initializeVideoButton(groupId);
        initializeAudioButton(groupId);

        grBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupMessageActivity.this,GroupInfoActivity.class);
                intent.putExtra("groupModel",currentModel);
                startActivityForResult(intent,90);
                // Handle edit action
            }
        });

        txtGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupMessageActivity.this,GroupInfoActivity.class);
                intent.putExtra("groupModel",currentModel);
                startActivityForResult(intent,90);
                // Handle edit action
            }
        });

        groupMessageActivity.setLayoutManager(new LinearLayoutManager(this));

        messageAdapter=new GroupMessageAdapter(groupMessageModelsList,this,myId,groupId,groupMessageActivity);
        groupMessageActivity.setAdapter(messageAdapter);


        String groupName1 = getIntent().getStringExtra("groupNameToMessage");
        String profilePic = getIntent().getStringExtra("groupImageToMessage");
        currentModel = getIntent().getParcelableExtra("groupModel");

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

        txtGroupName.setText(groupName1);
        Glide.with(this).load(profilePic).into(groupProfileImage);

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(GroupMessageActivity.this, v);

                // Inflate the menu resource
                popupMenu.inflate(R.menu.group_message_activity_menu);

                // Set item click listener for the menu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle menu item clicks here
                        switch (item.getItemId()) {
                            case R.id.btnGroupInfo:
                                Intent intent = new Intent(GroupMessageActivity.this,GroupInfoActivity.class);
                                intent.putExtra("groupModel",currentModel);
                                startActivityForResult(intent,90);
                                // Handle edit action
                                return true;
                            case R.id.btnAddMember:
                                Intent intent1 = new Intent(GroupMessageActivity.this,AddMembers.class);
                                intent1.putExtra("groupModel",currentModel);
                                startActivityForResult(intent1,101);
                                return true;

                            case R.id.btnExitGroup:
                                exitGroup();
                                // Handle delete action
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

        btnGroupSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = edtGroupMessage.getText().toString();
                edtGroupMessage.setText("");

                if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter Message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);

                    getGroupMemberTokens(groupId,message);

                    //Toast.makeText(GroupMessageActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGroupDataSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroupMemberTokens(groupId,"Photo");
                if (ContextCompat.checkSelfPermission(GroupMessageActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission
                    ActivityCompat.requestPermissions(GroupMessageActivity.this,
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

        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference("Group Details")
                .child(groupId).child("UnreadMessages");
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
        });


    }

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details");
    FirebaseUser currentUserId = FirebaseAuth.getInstance().getCurrentUser();

    private void initializeAudioButton(String groupId) {
        reference.child(groupId).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    btnAudioCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.

                    btnAudioCall.setBackground(ContextCompat.getDrawable(GroupMessageActivity.this, R.drawable.phone_108));

                    btnAudioCall.setOnClickListener(v -> {

                        btnAudioCall.setVisibility(View.INVISIBLE);

                        List<ZegoUIKitUser> users = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()){

                            String targetUserID  = ds.child("mobile").getValue(String.class);
                            String targetName  = ds.child("id").getValue(String.class);

                            if (!targetName.contains(currentUserId.getUid())){
                                users.add(new ZegoUIKitUser(targetUserID, targetName));
                            }
                            //Toast.makeText(GroupMessageActivity.this, targetUserID, Toast.LENGTH_LONG).show();
                        }
                        btnAudioCall.setInvitees(users);

                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeVideoButton(String groupId) {
        reference.child(groupId).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    btnVideoCall.setIsVideoCall(true);
                    btnVideoCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.

                    btnAudioCall.setBackground(ContextCompat.getDrawable(GroupMessageActivity.this, R.drawable.video_call_10));

                    btnVideoCall.setOnClickListener(v -> {

                        btnVideoCall.setVisibility(View.INVISIBLE);

                        List<ZegoUIKitUser> users = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()){

                            String targetUserID  = ds.child("mobile").getValue(String.class);
                            String targetName  = ds.child("id").getValue(String.class);

                            if (!targetName.contains(currentUserId.getUid())){
                                users.add(new ZegoUIKitUser(targetUserID, targetName));
                            }
                            //Toast.makeText(GroupMessageActivity.this, targetUserID, Toast.LENGTH_LONG).show();
                        }
                        btnVideoCall.setInvitees(users);

                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exitGroup() {

        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Group Details")
                                .child(currentModel.id)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());

                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(GroupMessageActivity.this, "Group Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }else {
                                    Toast.makeText(GroupMessageActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
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


    public void getGroupMemberTokens(String groupId, String message) {

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference()
                .child("Group Details")
                .child(groupId)
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
                                    Log.e("GroupMessage", "Error fetching user details: " + error.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    Log.d("GroupMessage", "Members list does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GroupMessage", "Error retrieving members: " + databaseError.getMessage());
            }
        });
    }


    public void sendMessage(String message) {

        String date=String.valueOf(System.currentTimeMillis());
        String groupId = getIntent().getStringExtra("groupIdToMessage");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails")
                .child(FirebaseAuth.getInstance().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Users users = snapshot.getValue(Users.class);
                    String name = users.getFullname();

                    databaseReference = FirebaseDatabase.getInstance().getReference("Group Message").child(groupId);
                    DatabaseReference newMessageRef = databaseReference.push();
                    String messageId = newMessageRef.getKey(); // Get the key generated by push()

                    groupMessageModel = new GroupMessageModel("text", message, date, myId,name,messageId);
                    // Push the message to the database
                    newMessageRef.setValue(groupMessageModel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        GroupLastMessageModel lastMessageModel=new GroupLastMessageModel();
        lastMessageModel.date=date;
        lastMessageModel.message=message;
        lastMessageModel.senderId=myId;
        lastMessageModel.type="text";

        databaseReference=FirebaseDatabase.getInstance().getReference("Group Details").child(groupId)
                .child("lastMessageModel");
        databaseReference.setValue(lastMessageModel);

        messageAdapter.notifyDataSetChanged();
        messageAdapter.scrollToBottom();
    }

    public void readMessage(){

        Query query = FirebaseDatabase.getInstance().getReference("Group Message")
                .child(currentModel.getId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMessageModelsList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    GroupMessageModel md = ds.getValue(GroupMessageModel.class);

                    md.setMessageId(ds.getKey());
                    groupMessageModelsList.add(md);
                }
                messageAdapter.notifyDataSetChanged();
                messageAdapter.scrollToBottom();
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
                .setPath("/ChatMe/Media/Group");                                       //Custom Path For media Storage


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
                Intent intent = new Intent(GroupMessageActivity.this, SendMediaService.class);
                intent.putExtra("groupId", currentModel.id);
                intent.putExtra("type", "group");
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
            currentModel = data.getParcelableExtra("groupModel");
            txtGroupName.setText(currentModel.name);
            Glide.with(this).load(currentModel.image).into(groupProfileImage);
        }
        else if (requestCode == 101 && resultCode == RESULT_OK) {
            currentModel = data.getParcelableExtra("groupModel");
        }
    }


    @Override
    protected void onResume() {
        readMessage();

        btnVideoCall.setVisibility(View.VISIBLE);
        btnVideoCall.setBackground(ContextCompat.getDrawable(GroupMessageActivity.this, R.drawable.video_call_10));
        btnAudioCall.setVisibility(View.VISIBLE);
        btnAudioCall.setBackground(ContextCompat.getDrawable(GroupMessageActivity.this, R.drawable.phone_108));

        groupMessageActivity.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Scroll to the last item in the RecyclerView
                groupMessageActivity.scrollToPosition(messageAdapter.getItemCount() - 1);
                // Remove the listener to avoid multiple calls
                groupMessageActivity.getViewTreeObserver().removeOnGlobalLayoutListener(this);


            }
        });
        super.onResume();
    }

    public void sendNotifications(List<String> tokens, String message) {
        FirebaseUtil.groupDetailsReference(currentModel.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    for (String token : tokens) {
                        JSONObject jsonObject = new JSONObject();
                        JSONObject notificationObject = new JSONObject();
                        notificationObject.put("title", currentModel.getName());
                        notificationObject.put("body", currentUserName + ": " + message);

                        JSONObject dataobject = new JSONObject();
                        dataobject.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        dataobject.put("groupId", currentModel.getId());

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
                        Toast.makeText(GroupMessageActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
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
                            // Toast.makeText(GroupMessageActivity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("Notification", "Failed to send notification. Response code: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupMessageActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}

