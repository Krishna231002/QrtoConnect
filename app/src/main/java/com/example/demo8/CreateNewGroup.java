package com.example.demo8;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import de.hdodenhof.circleimageview.CircleImageView;
import com.example.demo8.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

public class CreateNewGroup extends AppCompatActivity {

    CircleImageView imgGroup;
    EditText groupName;
    Button btnGroupCreate;
    Uri imageUri;
    ImageView back;

    //permission constant
    public static final int GALLERY_IMAGE_CODE =100;
    public static final int CAMERA_IMAGE_CODE =200;

    //string array for permission..
    String[] cameraPermission;
    String[] storagePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        imgGroup = findViewById(R.id.imgCreateGroup);
        groupName = findViewById(R.id.edtCreateGroupName);
        btnGroupCreate = findViewById(R.id.btnCreateDone);
        back = findViewById(R.id.backFromCreateGroup);


        Permissions();
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
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

        btnGroupCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    Toast.makeText(CreateNewGroup.this, "Please select a group image", Toast.LENGTH_SHORT).show();
                    return;
                }

                String groupNameText = groupName.getText().toString();

                if(groupNameText.isEmpty()) {
                    groupName.setError("Required");
                    groupName.requestFocus();
                } else {
                    if (imageUri == null) {
                        Toast.makeText(CreateNewGroup.this, "Select Group Image", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(CreateNewGroup.this,SelectGroupMembers.class);
                        intent.putExtra("GroupName", groupNameText);
                        intent.putExtra("GroupImage", imageUri.toString());
                        startActivity(intent);
                        finish();

                        //Bundle bundle = new Bundle();
                        //bundle.putString("GroupName", groupNameText);
                        //bundle.putString("GroupImage", imageUri.toString());

                        //GroupMemberFragment memberFragment = new GroupMemberFragment();
                        //memberFragment.setArguments(bundle);
                        //getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, memberFragment).commit();
                    }
                }
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
