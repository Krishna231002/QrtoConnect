package com.example.demo8;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Sign_Up_Page extends AppCompatActivity {

    EditText txtFirstName,txtLastName,txtEmail,txtMobile,txtPass;
    Button btn_sign_up;
    CircleImageView imageView;
    FirebaseAuth Auth;
    TextView btn_login;
    FirebaseUser firebaseUser;
    String firstname,lastname,email,mobile,pass,id,fcmToken,fullName;
    ProgressDialog pd;

    Uri imageUri = null;

    public static final int GALLERY_IMAGE_CODE =100;
    public static final int CAMERA_IMAGE_CODE =200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        txtFirstName = findViewById(R.id.sign_up_txt_first_name);
        txtLastName = findViewById(R.id.sign_up_txt_last_name);
        txtEmail = findViewById(R.id.sign_up_txt_email);
        txtMobile = findViewById(R.id.sign_up_txt_mobile);
        txtPass = findViewById(R.id.sign_up_txt_pass);
        btn_sign_up =findViewById(R.id.btn_sign_up);
        btn_login =findViewById(R.id.sign_up_login_txt);
        imageView= findViewById(R.id.sing_Up_Image);

        FirebaseApp.initializeApp(this);

        Auth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);

        Permissions();
        getFCMToken();

        ImageView togglePassword = findViewById(R.id.toggle_password);
        final EditText passwordEditText = findViewById(R.id.sign_up_txt_pass);

        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Password is currently visible, hide it
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePassword.setImageResource(R.drawable.close_eye); // Set your closed eye icon
                } else {
                    // Password is currently hidden, show it
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePassword.setImageResource(R.drawable.open_eye); // Set your open eye icon
                }
                // Move cursor to the end of the text
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerDialog();
            }
        });

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstname = txtFirstName.getText().toString();
                lastname = txtLastName.getText().toString();
                email = txtEmail.getText().toString();
                mobile= txtMobile.getText().toString();
                pass = txtPass.getText().toString();
                fullName = firstname + lastname;
                if (imageUri == null) {
                    // If no image is selected, use a default image
                    int defaultImageResource = R.drawable.avatar_4; // Replace default_image with your default image resource
                    imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + defaultImageResource);
                }

                if(imageUri==null ){
                    Toast.makeText(Sign_Up_Page.this, "Image is required", Toast.LENGTH_SHORT).show();
                }
                else if(!isValidEmail(email)){
                    Toast.makeText(Sign_Up_Page.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(firstname) ){
                    txtFirstName.setError("Required");
                }
                else if(TextUtils.isEmpty(lastname)){
                    txtLastName.setError("Required");
                }
                else if(TextUtils.isEmpty(email)){
                    txtEmail.setError("Required");
                }
                else if(TextUtils.isEmpty(mobile)){
                    txtMobile.setError("Required");
                }
                else if(TextUtils.isEmpty(pass)){
                    txtPass.setError("Required");
                }
                else if (!isValidPassword(pass)){
                    Toast.makeText(Sign_Up_Page.this, "Please enter a valid password (at least 8 characters)", Toast.LENGTH_SHORT).show();
                }
                else {

                    if(isNetworkConnected()) {
                        SignUpUser(firstname, lastname, email, mobile, pass);
                    }else {
                        Toast.makeText(Sign_Up_Page.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }


            }

        });



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            // Request SYSTEM_ALERT_WINDOW permission using PermissionX
                            //requestSystemAlertWindowPermission();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();

    }



    private String generateUniqueFilePath(){
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        String userId = txtFirstName.getText().toString()+ "_" + UUID.randomUUID().toString();

        //generate a unique for the user
        String filepath = "UserImages/" + "Photos/" + "User_" + firebaseUser1.getUid() + "/" +userId;
        return filepath;
    }

    private void SignUpUser(String firstname, String lastname, String email, String mobile, String pass) {
        pd.setMessage("Registering");
        pd.show();

        // Check if the email address is already registered
        Auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                                // Email already registered
                                pd.dismiss();
                                Toast.makeText(Sign_Up_Page.this, "Email already registered", Toast.LENGTH_SHORT).show();
                            } else {
                                // Email not registered, proceed with user registration
                                Auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // Proceed with user registration
                                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        id = firebaseUser.getUid();
                                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


                                        // Check if imageUri is not null before proceeding
                                        if (imageUri != null) {
                                            // Upload image to Firebase Storage
                                            String filepath = generateUniqueFilePath();
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepath);

                                            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    // Image uploaded successfully
                                                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                                                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            // Image download URL obtained
                                                            pd.dismiss();
                                                            String imageURL = uri.toString();
                                                            String fullname = firstname + " " + lastname;

                                                            // Create a new Users object with user data
                                                            Users user = new Users(firstname, lastname, mobile, email, imageURL, "", id, "", fcmToken, fullname, pass,"");

                                                            // Upload user data to Firebase Database
                                                            reference.child("UsersDetails").child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    // User data uploaded successfully
                                                                    
                                                                    sendVerificationEmail();
                                                                    FirebaseAuth.getInstance().signOut();
                                                                    finish();
                                                                  /* pd.dismiss();
                                                                    Toast.makeText(Sign_Up_Page.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(Sign_Up_Page.this, MainActivity.class);
                                                                    startActivity(intent); */
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Failed to upload user data
                                                                    pd.dismiss();
                                                                    Toast.makeText(Sign_Up_Page.this, "Failed to upload user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Image upload failed
                                                    pd.dismiss();
                                                    Toast.makeText(Sign_Up_Page.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            // If imageUri is null, proceed without uploading an image
                                            pd.dismiss();
                                            Toast.makeText(Sign_Up_Page.this, "Image not selected. Proceeding without image.", Toast.LENGTH_SHORT).show();
                                            // Proceed with registering user without an image
                                            // You can add code here to register the user without an image if necessary
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle registration failure
                                        pd.dismiss();
                                        Toast.makeText(Sign_Up_Page.this, "Failed to register user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // Error occurred while checking email registration status
                            pd.dismiss();
                            Toast.makeText(Sign_Up_Page.this, "Failed to check email registration status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail() {

        FirebaseAuth.getInstance().getCurrentUser()
                .sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(Sign_Up_Page.this, "Email Verification link sent to you email. Please verify and then signIn", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(Sign_Up_Page.this, "Email Verification link don't sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fcmToken = task.getResult();
                        // Use the token as needed, such as storing it in Firebase Database
                    } else {
                        Log.e("FCM", "Fetching FCM registration token failed", task.getException());
                    }
                });

    }


    private void imagePickerDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i==0)
                {
                    openCamera();
                }
                if(i==1)
                {
                    openGallery();
                }
            }
        });
        builder.create().show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_IMAGE_CODE);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        values.put(MediaStore.Images.Media.TITLE,"Temp Desc");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,CAMERA_IMAGE_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                imageView.setImageURI(imageUri);
                //if(myAddImage != null){
                // myViewAddImg.setVisibility(View.INVISIBLE);
                //}
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Toast.makeText(getApplicationContext(),"Something wrong",Toast.LENGTH_LONG).show();
            }
        }
    }
}