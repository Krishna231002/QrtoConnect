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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.demo8.Adapter.GroupMemberAdapter;
import com.example.demo8.Interface.AllConstants;
import com.example.demo8.Interface.MemberItemInterface;
import com.example.demo8.Permissions.Permissions;
import com.example.demo8.databinding.ActivityGroupInfoBinding;
import com.example.demo8.databinding.AdminDialogLayoutBinding;
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

public class GroupInfoActivity extends AppCompatActivity implements MemberItemInterface {
    ArrayList<Users> arrayList;
    ActivityGroupInfoBinding binding;
    GroupModels currentGroup;
    AlertDialog alertDialog;
    Permissions permissions;
    Uri imageUri;
    GroupMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
      setContentView(binding.getRoot());
      setSupportActionBar(binding.infoToolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      memberAdapter = new GroupMemberAdapter(this);



      binding.memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
      binding.memberRecyclerView.setNestedScrollingEnabled(false);
        binding.memberRecyclerView.setAdapter(memberAdapter);

      permissions=new Permissions();
      currentGroup = getIntent().getParcelableExtra("groupModel");



      Glide.with(this).load(currentGroup.image).into(binding.expandedImage);
      binding.collapsingToolbar.setTitle(currentGroup.name);
      binding.txtTotalMembers.setText(currentGroup.members.size() + " Members");
      




      arrayList = new ArrayList<>();

      for (int i = 0 ;i<currentGroup.members.size();i++){

          DatabaseReference reference= FirebaseDatabase.getInstance().getReference("UsersDetails")
                  .child(currentGroup.members.get(i).id);

          int finalI = i;
          reference.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  if (snapshot.exists()){
                      Users users=snapshot.getValue(Users.class);
                      users.setTyping(currentGroup.members.get(finalI).role);
                      if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currentGroup.adminId)) {
                          Log.d("GroupInfoActivity", "Current user is an admin");
                          currentGroup.setAdmin(true);
                      }

                      if(currentGroup.isAdmin()){
                          binding.cardDeleteGroup.setVisibility(View.VISIBLE);
                      }else{
                          binding.cardDeleteGroup.setVisibility(View.GONE);
                      }

                      arrayList.add(users);


                  }
                  if (finalI==currentGroup.members.size()-1){
                      memberAdapter.setArrayList(arrayList);
                  }
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
      }




      binding.expandedImage.setOnClickListener(view -> {
          if(permissions.isStorageOk(this)){
              CropImage.activity()
                      .setGuidelines(CropImageView.Guidelines.ON)
                      .setAspectRatio(1,1)
                      .start(this);
          }else{
              permissions.requestStorage(this);
          }
      });

      binding.btnEditGroupName.setOnClickListener(view ->{

         showEditGroupNameDialog();

      });

      binding.cardExitGroup.setOnClickListener(view -> {
          exitGroup();
      });

      binding.cardDeleteGroup.setOnClickListener(view ->{
          deleteGroup();
      });



    }
    private void showEditGroupNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null); // Inflate dialog_layout
        EditText edtGroupName = dialogView.findViewById(R.id.edtGroupName);
        Button btnDone = dialogView.findViewById(R.id.btnEditGroup);

        builder.setView(dialogView); // Set inflated view to AlertDialog

        alertDialog = builder.create();
        alertDialog.show();

        btnDone.setOnClickListener(done -> {
            String groupName = edtGroupName.getText().toString();
            if (!groupName.isEmpty()) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details").child(currentGroup.id);

                Map<String, Object> map = new HashMap<>();
                map.put("name", groupName);

                reference.updateChildren(map);
                currentGroup.name = groupName;

                binding.collapsingToolbar.setTitle(currentGroup.name);
                alertDialog.dismiss();
            }
        });
    }

    private void deleteGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Group Details")
                                .child(currentGroup.id);

                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Group Message")
                                            .child(currentGroup.id);
                                    databaseReference.removeValue();

                                    Toast.makeText(GroupInfoActivity.this, "Group Deleted", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }else {
                                    Toast.makeText(GroupInfoActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
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

    private void exitGroup() {

        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Group Details")
                                .child(currentGroup.id)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());

                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(GroupInfoActivity.this, "Group Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }else {
                                    Toast.makeText(GroupInfoActivity.this, "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
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
                    updateGroupImage();
                }

        }
    }

    private void updateGroupImage() {

        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Group Images");
        storageReference.child(currentGroup.id+AllConstants.GROUP_IMAGE).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(GroupInfoActivity.this, "Uploading Image.....", Toast.LENGTH_SHORT).show();
                Task<Uri> image=taskSnapshot.getStorage().getDownloadUrl();
                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            String url = task.getResult().toString();
                            currentGroup.image = url;
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details").child(currentGroup.id);

                            Map<String,Object> map = new HashMap<>();
                            map.put("image",url);

                            reference.updateChildren(map);

                            Glide.with(GroupInfoActivity.this).load(url).into(binding.expandedImage);
                            Toast.makeText(GroupInfoActivity.this, "Image Updated", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("TAG","onComplete :"+task);
                            Toast.makeText(GroupInfoActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onMemberClick(@NonNull Users userModel, int position) {

        if (!userModel.getId().equals(FirebaseAuth.getInstance().getUid())) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AdminDialogLayoutBinding layoutBinding = AdminDialogLayoutBinding.inflate(LayoutInflater.from(this));

            builder.setView(layoutBinding.getRoot());



            if (!currentGroup.isAdmin) {
                layoutBinding.txtInfo.setVisibility(View.VISIBLE);
                layoutBinding.txtAdmin.setVisibility(View.GONE);
                layoutBinding.txtRAdmin.setVisibility(View.GONE);
                layoutBinding.txtRemove.setVisibility(View.GONE);
            }

            if (userModel.getTyping().equals("Admin")) {
                if(currentGroup.adminName.equals(userModel.fullname))
                {
                    layoutBinding.txtInfo.setVisibility(View.VISIBLE);
                    layoutBinding.txtAdmin.setVisibility(View.GONE);
                    layoutBinding.txtRAdmin.setVisibility(View.GONE);
                    layoutBinding.txtRemove.setVisibility(View.GONE);
                }else {
                    layoutBinding.txtInfo.setVisibility(View.VISIBLE);
                    layoutBinding.txtRAdmin.setVisibility(View.VISIBLE);
                    layoutBinding.txtRemove.setVisibility(View.VISIBLE);
                    layoutBinding.txtAdmin.setVisibility(View.GONE);}
            } else {
                layoutBinding.txtRAdmin.setVisibility(View.GONE);
            }


            layoutBinding.txtInfo.setOnClickListener(view -> {
                Intent intent = new Intent(GroupInfoActivity.this, ChatInfoActivity.class);
                intent.putExtra("userID", userModel.getId());
                intent.putExtra("groupToChatName",userModel.getFullname());
                intent.putExtra("groupToChatImage",userModel.getImageURL());
                intent.putExtra("groupToChatMobile",userModel.getMobile());
                alertDialog.dismiss();
                startActivity(intent);
            });

            layoutBinding.txtAdmin.setOnClickListener(view -> {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details")
                        .child(currentGroup.id).child("Members").child(userModel.getId());

                Map<String, Object> map = new HashMap<>();
                map.put("role", "Admin");
                reference.updateChildren(map);
                alertDialog.dismiss();
                arrayList.get(position).setTyping("Admin");
                currentGroup.members.get(position).role = "Admin";
                memberAdapter.setArrayList(arrayList);
            });

            layoutBinding.txtRAdmin.setOnClickListener(view -> {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details")
                        .child(currentGroup.id).child("Members").child(userModel.getId());

                Map<String, Object> map = new HashMap<>();
                map.put("role", "member");
                reference.updateChildren(map);
                alertDialog.dismiss();
                arrayList.get(position).setTyping("member");
                currentGroup.members.get(position).role = "member";
                memberAdapter.setArrayList(arrayList);

            });


            layoutBinding.txtRemove.setOnClickListener(view -> {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details")
                        .child(currentGroup.id).child("Members").child(userModel.getId());


                reference.removeValue();

                alertDialog.dismiss();
                arrayList.remove(position);
                currentGroup.members.remove(position);
                memberAdapter.setArrayList(arrayList);

            });

            alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("groupModel",currentGroup);
        setResult(RESULT_OK,intent);


        finish();
        super.onBackPressed();
    }
}