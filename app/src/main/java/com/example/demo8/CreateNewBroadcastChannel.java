package com.example.demo8;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo8.Interface.AllConstants;
import com.example.demo8.MyModels.BroadcastChannelMemberModel;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateNewBroadcastChannel extends AppCompatActivity {

    CircleImageView imgGroup;
    EditText broadcastChannelName;
    Button btnBroadcastChannelCreate;
    Uri imageUri;

    //permission constant
    public static final int GALLERY_IMAGE_CODE =100;
    public static final int CAMERA_IMAGE_CODE =200;

    //string array for permission..
    String[] cameraPermission;
    String[] storagePermission;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    ImageView back;

    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    ProgressDialog pd;
    DatabaseReference channelRef = FirebaseDatabase.getInstance().getReference("Broadcast Channel");
    String channelId = channelRef.push().getKey(); // Generate a single key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_broadcast_channel);

        imgGroup = findViewById(R.id.imgCreateGroupB);
        broadcastChannelName = findViewById(R.id.edtCreateGroupNameB);
        btnBroadcastChannelCreate = findViewById(R.id.btnCreateDoneB);
        back = findViewById(R.id.backFromCreateBroadcast);

        pd = new ProgressDialog(this);

        Permissions();
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        btnBroadcastChannelCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    Toast.makeText(CreateNewBroadcastChannel.this, "Please select a Broadcast image", Toast.LENGTH_SHORT).show();
                    return;
                }

                String channelName = broadcastChannelName.getText().toString();

                if(channelName.isEmpty()) {
                    broadcastChannelName.setError("Field is Required");
                    broadcastChannelName.requestFocus();
                } else {
                    if (imageUri == null) {
                        Toast.makeText(CreateNewBroadcastChannel.this, "Select Broadcast Image", Toast.LENGTH_SHORT).show();
                    } else {

                        createBroadcastChannelForAll();
                        createBroadcastChannelForMe();

                    }
                }
            }
        });
    }

    private void createBroadcastChannelForMe() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Users user = snapshot.getValue(Users.class);

                    String fullName = user.getFullname();
                    String token = user.getToken();


                    pd.setMessage("Creating...");
                    pd.show();

                    DatabaseReference ChannelRef = FirebaseDatabase.getInstance().getReference("Broadcast Channel");

                    String adminID = firebaseUser.getUid();
                    String adminName = fullName;
                    String channelName = broadcastChannelName.getText().toString();
                    String role = "Admin";


                    if (channelId != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Broadcast Channel Image");

                        storageReference.child(channelId + AllConstants.BROADCAST_CHANNEL_IMAGE).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();
                                Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String channelImage = task.getResult().toString();

                                            HashMap<Object, Object> hashMap = new HashMap<>();
                                            hashMap.put("adminid", adminID);
                                            hashMap.put("adminName", adminName);
                                            hashMap.put("channelName", channelName);
                                            hashMap.put("channelImage", channelImage);
                                            hashMap.put("channelId", channelId);
                                            hashMap.put("role", role);

                                           // BroadcastChannelModel channelModel = new BroadcastChannelModel(adminID,adminName,channelId,channelImage,channelName,role);
                                            BroadcastChannelModel broadcastChannelModel= new BroadcastChannelModel();
                                            broadcastChannelModel.adminId =adminID;
                                            broadcastChannelModel.adminName =adminName;
                                            broadcastChannelModel.channelName =channelName;
                                            broadcastChannelModel.channelId =channelId;
                                            broadcastChannelModel.channelImage =channelImage;
                                            broadcastChannelModel.role =role;



                                            ChannelRef.child(channelId).setValue(broadcastChannelModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.isSuccessful()) {
                                                            // Additional code to add group member
                                                            DatabaseReference channelMembersRef = FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(channelId).child("Members");

                                                            HashMap<String, Object> map1 = new HashMap<>();
                                                            map1.put("id", firebaseAuth.getUid());
                                                            map1.put("role", "Admin");
                                                            map1.put("token",token);

                                                            BroadcastChannelMemberModel memberModel = new BroadcastChannelMemberModel();
                                                            memberModel.id = firebaseAuth.getUid();
                                                            memberModel.role = "Admin";
                                                            memberModel.token = token;

                                                            channelMembersRef.child(firebaseAuth.getUid()).setValue(memberModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(CreateNewBroadcastChannel.this, channelName + " Created...", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                      //  Toast.makeText(CreateNewBroadcastChannel.this, channelName + " Created...", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void createBroadcastChannelForAll() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Users user = snapshot.getValue(Users.class);

                    String fullName = user.getFullname();

                    pd.setMessage("Creating...");
                    pd.show();

                    DatabaseReference AllChannelRef = FirebaseDatabase.getInstance().getReference("All Broadcast Channel");

                    String adminID = firebaseUser.getUid();
                    String adminName = fullName;
                    String channelName = broadcastChannelName.getText().toString();
                    String role = "Admin";


                    if (channelId != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Broadcast Channel Image");

                        storageReference.child(channelId + AllConstants.BROADCAST_CHANNEL_IMAGE).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();
                                Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String channelImage = task.getResult().toString();

                                            HashMap<Object, Object> hashMap = new HashMap<>();
                                            hashMap.put("adminid", adminID);
                                            hashMap.put("adminName", adminName);
                                            hashMap.put("channelName", channelName);
                                            hashMap.put("channelImage", channelImage);
                                            hashMap.put("channelId", channelId);
                                            hashMap.put("role", role);

                                            AllBroadcastChannelModel allBroadcastChannelModel = new AllBroadcastChannelModel();
                                            allBroadcastChannelModel.adminId = adminID;
                                            allBroadcastChannelModel.adminName = adminName;
                                            allBroadcastChannelModel.channelName = channelName;
                                            allBroadcastChannelModel.channelImage = channelImage;
                                            allBroadcastChannelModel.channelId = channelId;
                                            allBroadcastChannelModel.role = role;

                                            AllChannelRef.child(channelId).setValue(allBroadcastChannelModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

    }

    private void imagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                if (i==0) {
                    openCamera();
                }
                if (i==1) {
                    openGallery();
                }
            }
        });
        builder.create().show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_CODE);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.TITLE, "Temp Desc");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_IMAGE_CODE);

    }

    private void Permissions() {
        Dexter.withContext(this)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }

                }).check();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_IMAGE_CODE){
                //pick image from gallery
                //crop image..
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            } else if (requestCode == CAMERA_IMAGE_CODE) {
                //pick image from camera
                //crop image..
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                //cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                imgGroup.setImageURI(imageUri);
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Toast.makeText(getApplicationContext(),"Something wrong",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}