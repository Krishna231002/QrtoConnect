package com.example.demo8;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.demo8.Adapter.BroadcastMemberAdapter;
import com.example.demo8.Adapter.GroupMemberAdapter;
import com.example.demo8.Interface.AllConstants;
import com.example.demo8.Interface.MemberItemInterface;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.example.demo8.Permissions.Permissions;
import com.example.demo8.databinding.ActivityBroadcastInfoBinding;
import com.example.demo8.databinding.ActivityGroupInfoBinding;
import com.example.demo8.databinding.AdminDialogLayoutBinding;
import com.example.demo8.databinding.BroadcastAdminDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BroadcastInfoActivity extends AppCompatActivity implements MemberItemInterface{

    ArrayList<Users> arrayList;
    ActivityBroadcastInfoBinding binding;
    BroadcastChannelModel currentBroadcastChannel;
    AlertDialog alertDialog;
    Permissions permissions;
    Uri imageUri;
    BroadcastMemberAdapter memberAdapter;
    long membersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBroadcastInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.brinfoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        memberAdapter = new BroadcastMemberAdapter(this);



        binding.broadcastMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.broadcastMemberRecyclerView.setNestedScrollingEnabled(false);
        binding.broadcastMemberRecyclerView.setAdapter(memberAdapter);

        permissions=new Permissions();
        currentBroadcastChannel = getIntent().getParcelableExtra("channelModel");

        binding.brcollapsingToolbar.setTitle(currentBroadcastChannel.getChannelName());
        Glide.with(this).load(currentBroadcastChannel.getChannelImage()).into(binding.brexpandedImage);

        if (currentBroadcastChannel.getAdminId().equals(FirebaseAuth.getInstance().getUid())) {
            // If the current user is the admin, make the delete button visible
            binding.brcardDeleteChannel.setVisibility(View.VISIBLE);
        } else {
            // If the current user is not the admin, hide the delete button
            binding.brcardDeleteChannel.setVisibility(View.GONE);
            binding.btnEditBroadcastName.setVisibility(View.GONE);
        }

        arrayList = new ArrayList<>();

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference()
                .child("Broadcast Channel")
                .child(currentBroadcastChannel.getChannelId())
                .child("Members");


        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    membersCount = dataSnapshot.getChildrenCount();
                    if (membersCount==1)
                    {
                        binding.txtBroadcastTotalMembers.setText(membersCount + " Member");
                    }else {
                        binding.txtBroadcastTotalMembers.setText(membersCount + " Members");
                    }
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        String memberRole = memberSnapshot.child("role").getValue(String.class);

                        // Fetch user details for each member
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(memberId);
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Users users = snapshot.getValue(Users.class);
                                    users.setTyping(memberRole);
                                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currentBroadcastChannel.getAdminId())) {
                                        Log.d("BroadcastInfoActivity", "Current user is an admin");
                                        currentBroadcastChannel.setAdmin(true);
                                        arrayList.add(users); // Add all members
                                    } else {
                                        // If current user is not the admin, only add admin to list
                                        if (memberRole.equals("Admin")) {
                                            arrayList.add(users);
                                        }
                                    }

                                    memberAdapter.setArrayList(arrayList); // Update adapter here
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("BroadcastInfoActivity", "Error fetching user details: " + error.getMessage());
                            }
                        });
                    }
                } else {
                    Log.d("BroadcastInfoActivity", "Members list does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("BroadcastInfoActivity", "Error retrieving members: " + databaseError.getMessage());
            }
        });





        binding.brexpandedImage.setOnClickListener(view -> {
            // Check if the current user is the admin
            if (currentBroadcastChannel.getAdminId().equals(FirebaseAuth.getInstance().getUid())) {
                if(permissions.isStorageOk(this)){
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                } else {
                    permissions.requestStorage(this);
                }
            } else {
                // If the current user is not the admin, hide the options to edit the broadcast
                binding.brexpandedImage.setClickable(false); // Disable image click
                binding.brexpandedImage.setFocusable(false); // Disable image focus
            }
        });

        binding.btnEditBroadcastName.setOnClickListener(view ->{
            // Check if the current user is the admin
            if (currentBroadcastChannel.getAdminId().equals(FirebaseAuth.getInstance().getUid())) {
                showEditBroadcastNameDialog();
            } else {
                Toast.makeText(this, "Only Admin can edit name", Toast.LENGTH_SHORT).show();
            }
        });

        binding.brcardExitChannel.setOnClickListener(view -> {
            exitBroadcast();
        });

        binding.brcardDeleteChannel.setOnClickListener(view ->{
            deleteBroadcast();
        });



    }
    private void showEditBroadcastNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.broadcast_dialog_layout, null); // Inflate dialog_layout
        EditText edtBroadcastName = dialogView.findViewById(R.id.edtBroadcastName);
        Button btnDone = dialogView.findViewById(R.id.btnEditBroadcast);

        builder.setView(dialogView); // Set inflated view to AlertDialog

        alertDialog = builder.create();
        alertDialog.show();

        btnDone.setOnClickListener(done -> {
            String channelName = edtBroadcastName.getText().toString();
            if (!channelName.isEmpty()) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(currentBroadcastChannel.channelId);

                Map<String, Object> map = new HashMap<>();
                map.put("channelName", channelName);

                reference.updateChildren(map);
                currentBroadcastChannel.setChannelName(channelName);

                binding.brcollapsingToolbar.setTitle(channelName);
                alertDialog.dismiss();
            }
        });
    }


    private void deleteBroadcast() {
        // Check if the current user is the admin
        if (currentBroadcastChannel.getAdminId().equals(FirebaseAuth.getInstance().getUid())) {
            // If the current user is the admin, allow the deletion
            new AlertDialog.Builder(this)
                    .setTitle("Delete Broadcast Channel")
                    .setMessage("Are you sure you want to delete the broadcast channel?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Broadcast Channel")
                                    .child(currentBroadcastChannel.channelId);

                            reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Broadcast Message")
                                                .child(currentBroadcastChannel.channelId);
                                        databaseReference.removeValue();

                                        Toast.makeText(BroadcastInfoActivity.this, "Broadcast Channel Deleted", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }else {
                                        Toast.makeText(BroadcastInfoActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing if the user cancels deletion
                        }
                    }).create().show();
        } else {
            // If the current user is not the admin, show a message indicating that only the admin can delete the channel
            Toast.makeText(this, "Only the admin can delete the broadcast channel", Toast.LENGTH_SHORT).show();
        }
    }



    private void exitBroadcast() {

        new AlertDialog.Builder(this)
                .setTitle("Leave Broadcast Channel")
                .setMessage("Are you sure you want to leave the broadcast channel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Broadcast Channel")
                                .child(currentBroadcastChannel.channelId)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());

                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(BroadcastInfoActivity.this, "Broadcast Channel Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }else {
                                    Toast.makeText(BroadcastInfoActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
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



    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return super.onSupportNavigateUp();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== AllConstants.STORAGE_REQUEST_CODE){
            if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
                pickImage();
            }else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void pickImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                imageUri=result.getUri();
                updateBroadcastImage();
            }

        }
    }

    private void updateBroadcastImage() {

        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Broadcast Channel Image");
        storageReference.child(currentBroadcastChannel.channelId+AllConstants.BROADCAST_CHANNEL_IMAGE).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(BroadcastInfoActivity.this, "Uploading Image.....", Toast.LENGTH_SHORT).show();
                Task<Uri> image=taskSnapshot.getStorage().getDownloadUrl();
                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            String url = task.getResult().toString();
                            currentBroadcastChannel.setChannelImage(url);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(currentBroadcastChannel.channelId);

                            Map<String,Object> map = new HashMap<>();
                            map.put("channelImage",url);

                            reference.updateChildren(map);

                            Glide.with(BroadcastInfoActivity.this).load(url).into(binding.brexpandedImage);
                            Toast.makeText(BroadcastInfoActivity.this, "Image Updated", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("TAG","onComplete :"+task);
                            Toast.makeText(BroadcastInfoActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onMemberClick(@NonNull Users userModel, int position) {
        if (currentBroadcastChannel.getAdminId().equals(FirebaseAuth.getInstance().getUid())) {
            // Only show the dialog if the current user is the admin
            if (!userModel.getId().equals(FirebaseAuth.getInstance().getUid())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                BroadcastAdminDialogLayoutBinding layoutBinding = BroadcastAdminDialogLayoutBinding.inflate(LayoutInflater.from(this));

                builder.setView(layoutBinding.getRoot());

                if (!currentBroadcastChannel.isAdmin) {
                    layoutBinding.txtRemove.setVisibility(View.GONE);
                }

                if (userModel.getTyping().equals("Admin")) {
                    if (currentBroadcastChannel.getAdminName().equals(userModel.fullname)) {
                        layoutBinding.txtRemove.setVisibility(View.GONE);
                    } else {
                        layoutBinding.txtRemove.setVisibility(View.VISIBLE);
                    }
                }

                layoutBinding.txtRemove.setOnClickListener(view -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel")
                            .child(currentBroadcastChannel.channelId).child("Members").child(userModel.getId());

                    reference.removeValue();
                    alertDialog.dismiss();
                    arrayList.remove(position);
                    memberAdapter.setArrayList(arrayList);
                });

                alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("channelModel",currentBroadcastChannel);
        setResult(RESULT_OK,intent);


        finish();
        super.onBackPressed();
    }
}