package com.example.demo8;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditMyDetails extends AppCompatActivity {

    CircleImageView myAddImage;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    TextInputEditText myTxtFirstName,myTxtLastName,myTxtMobile,myTxtEmail,myTxtCompanyName;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    Button myBtnSave,myBtnCancel;
    View myViewAddImg;
    //create variable for store user's input..
    String firstName,lastName,mobile,email,companyName,id;

    //permission constant
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    //string array for permission..
    String[] cameraPermission;
    String[]  storagePermission;

    boolean isMyData;
    Uri imageUri;

    String fcmToken;
    String image;
    ImageView edt_profileBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_details);

        //get intent..


        myAddImage = findViewById(R.id.my_update_image);
        myTxtFirstName = findViewById(R.id.my_txt_first_name);
        myTxtLastName = findViewById(R.id.my_txt_last_name);
        myTxtMobile = findViewById(R.id.my_txt_mobile_number);
        myTxtEmail = findViewById(R.id.my_txt_email);
        myTxtCompanyName = findViewById(R.id.my_txt_company_name);
        myBtnSave = findViewById(R.id.my_save_btn);
        myBtnCancel = findViewById(R.id.my_cancel_btn);
        myViewAddImg = findViewById(R.id.my_view_add_img);
        edt_profileBack=findViewById(R.id.edt_profileBack);


        retrieveTokenFromDatabase();

        Intent intent = getIntent();
        isMyData = intent.getBooleanExtra("isEdit",true);
        String firstname= intent.getStringExtra("first_name");
        String lastname= intent.getStringExtra("last_name");
        String email= intent.getStringExtra("email");
        String mobile= intent.getStringExtra("mobile");
        String company_name= intent.getStringExtra("company_name");
        image=intent.getStringExtra("image");

        myTxtFirstName.setText(firstname);
        myTxtLastName.setText(lastname);
        myTxtEmail.setText(email);
        myTxtMobile.setText(mobile);
        myTxtCompanyName.setText(company_name);

        myTxtEmail.setInputType(InputType.TYPE_NULL);

        myTxtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditMyDetails.this, "You Can't Edit Email Address", Toast.LENGTH_LONG).show();
            }
        });

        Glide.with(EditMyDetails.this).load(image).into(myAddImage);

        //
        //init permission..
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        edt_profileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        myBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {

                   String firstName_1 = myTxtFirstName.getText().toString();
                    String lastname_1 = myTxtLastName.getText().toString();
                    String mobile_1 = myTxtMobile.getText().toString();
                    String email_1 = myTxtEmail.getText().toString();
                    if(imageUri ==null){
                        Toast.makeText(EditMyDetails.this, "Please select new image", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(firstName_1))
                    {
                        myTxtFirstName.setError("Required");
                    } else if (TextUtils.isEmpty(lastname_1)) {
                        myTxtLastName.setError("Required");
                    } else if (TextUtils.isEmpty(email_1)) {
                        myTxtEmail.setError("Required");
                    } else if (TextUtils.isEmpty(mobile_1)) {
                        myTxtMobile.setError("Required");
                    } else if (!isValidEmail(email_1)) {
                        Toast.makeText(EditMyDetails.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    }else {
                        saveData();
                        // Pass updated data back to AccountFragment
                        Intent intent = new Intent();
                        intent.putExtra("updatedImageUri", imageUri.toString());
                        setResult(RESULT_OK, intent);
                        finish();
                        Toast.makeText(EditMyDetails.this, "Profile Updated...", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(EditMyDetails.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });


        myBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImagePickerDialog();
            }
        });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void retrieveTokenFromDatabase() {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        fcmToken = user.getToken();
                        // Use the retrieved FCM token for login

                    }
                } else {
                    Toast.makeText(EditMyDetails.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(EditMyDetails.this, "Failed to retrieve token: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void saveData() {

        firstName = myTxtFirstName.getText().toString();
        lastName = myTxtLastName.getText().toString();
        mobile = myTxtMobile.getText().toString();
        email = myTxtEmail.getText().toString();
        companyName = myTxtCompanyName.getText().toString();
        id=FirebaseAuth.getInstance().getUid();

        FirebaseUser userdetail = FirebaseAuth.getInstance().getCurrentUser();

        if (userdetail != null) {

            userdetail.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "Email updated successfully");
                                // Update successful, you can notify the user or take other actions
                            } else {
                                Log.d("TAG", "Failed to update email", task.getException());
                                // Handle the error
                            }
                        }
                    });
        } else {
            // User is not signed in, handle this case accordingly
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());
        // Create a Users object with the updated data


        String fullname=firstName + " " +lastName;

        // Create a Users object with the updated data
        Users user = new Users(firstName,lastName,mobile,email,"",id,"",fcmToken,fullname,companyName); // imageURL will be updated later
        // imageURL will be updated later

        // Store user data in Firebase Realtime Database
        databaseReference.setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data stored successfully, now upload profile image to Firebase Storage
                        uploadProfileImage();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to edit profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Method to upload profile image to Firebase Storage
    private void uploadProfileImage() {
        if (imageUri != null) {
            // Reference to Firebase Storage
            StorageReference storageRef = storage.getReference();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());

            String userId =UUID.randomUUID().toString();
            String filepath = "UserImages/" + "Photos/" + "User_" + firebaseUser.getUid() + "/" +userId;
            // Create a reference to 'profile_images/[userId].jpg'
            StorageReference profileImageRef = storageRef.child(filepath + ".jpg");

            // Upload the file to Firebase Storage
            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully, get the download URL
                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String,Object> map = new HashMap<>();
                                    map.put("imageURL",uri.toString());
                                    // Update the imageURL field in Firebase Realtime Database with the download URL
                                    databaseReference.updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Users users=new Users();

                                                    //   Toast.makeText(getApplicationContext(), "Profile Edited", Toast.LENGTH_SHORT).show();
                                                    // Intent intent1 = new Intent(EditMyDetails.this, MainActivity.class);
                                                    // startActivity(intent1);
                                                    //finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Failed to edit profile", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            finish();
        }

    }

    private void ShowImagePickerDialog() {
        //option for dialog
        String option[] = {"Camera","Gallery"};

        //alert dialog builder..
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose an Option");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //camera selected
                    if(!checkCameraPermission()){
                        //request camera permission..
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
                else if(which == 1){
                    //gallery selected..
                    if(!checkStoragePermission()){
                        //request camera permission..
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        }).create().show();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_PERMISSION_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_PERMISSION_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

    private void pickFromGallery() {
        //intent to gallery..
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");//only image choose..

        startActivityForResult(galleryIntent,IMAGE_FROM_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //ContactValues for image info..
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE,"IMAGE_TITLE");
        values.put(MediaStore.Video.Media.DESCRIPTION,"IMAGE_DETAIL");

        //save Uri
        imageUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);

        //intent camera open.
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        startActivityForResult(cameraIntent,IMAGE_FROM_CAMERA_CODE);
    }

    //handel request permission..
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if(grantResults.length >0){

                    //if all permission allowed return true, otherwise false..
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        //both permission granted..
                        pickFromCamera();
                    }else {
                        //permission not granted..
                        Toast.makeText(getApplicationContext(),"Allow Camera And Storage Permission..",Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case STORAGE_PERMISSION_CODE:
                if(grantResults.length >0){

                    //if all permission allowed return true, otherwise false..
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        //permission granted..
                        pickFromGallery();
                    }else {
                        //permission not granted..
                        Toast.makeText(getApplicationContext(),"Allow Storage Permission..",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_FROM_GALLERY_CODE) {
                // Pick image from gallery, crop it, and set it to myAddImage
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else if (requestCode == IMAGE_FROM_CAMERA_CODE) {
                // Pick image from camera, crop it, and set it to myAddImage
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                // Cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                myAddImage.setImageURI(imageUri);
                if (myAddImage != null) {
                    myViewAddImg.setVisibility(View.INVISIBLE);
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == IMAGE_FROM_GALLERY_CODE) {
            // If no image is picked from the gallery, load the existing image into myAddImage
            if (image != null && !image.isEmpty()) {
                Glide.with(EditMyDetails.this).load(image).into(myAddImage);
                myViewAddImg.setVisibility(View.INVISIBLE);
            }
        }
    }

}