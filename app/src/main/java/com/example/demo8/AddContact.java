package com.example.demo8;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddContact extends AppCompatActivity {

    CircleImageView addImage;
    TextInputEditText txtFirstName,txtLastName,txtMobile,txtEmail,txtCompanyName,txtNotes;
    Button btnSave,btnCancel;
    View viewAddImg;
    FirebaseUser firebaseUser;

    ProgressDialog pd;
    ImageView back;

    //create variable for store user's input..
    String id,firstName,lastName,mobile,email,companyName,notes,image;
    Boolean isEditMode;
    //create action bar


    //permission constant

    public static final int GALLERY_IMAGE_CODE =100;
    public static final int CAMERA_IMAGE_CODE =200;

    //Image Uri var
    Uri imageUri;
    //data from qr code..
    String QrFirstName,QrLastName,QrMobile,QrEmail,QrCompanyName;
    Boolean isQrData;
    String DialerContact;

    //back button on action bar..
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        addImage = findViewById(R.id.my_add_image);
        txtFirstName = findViewById(R.id.my_txt_first_name);
        txtLastName = findViewById(R.id.my_txt_last_name);
        txtMobile = findViewById(R.id.my_txt_mobile_number);
        txtEmail = findViewById(R.id.my_txt_email);
        txtCompanyName = findViewById(R.id.my_txt_company_name);
        txtNotes = findViewById(R.id.txt_notes);
        btnSave = findViewById(R.id.save_btn);
        btnCancel = findViewById(R.id.cancel_btn);
        viewAddImg = findViewById(R.id.view_add_img);
        back=findViewById(R.id.addContactBack);

        Permissions();
        pd = new ProgressDialog(this);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()){
                firstName = txtFirstName.getText().toString();
                lastName = txtLastName.getText().toString();
                mobile = txtMobile.getText().toString();
                companyName = txtCompanyName.getText().toString();
                email = txtEmail.getText().toString();
                notes = txtNotes.getText().toString();

                if (imageUri == null) {
                        // If no image is selected, use a default image
                        int defaultImageResource = R.drawable.avatar_4; // Replace default_image with your default image resource
                        imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + defaultImageResource);
                }


                    if(TextUtils.isEmpty(firstName)){
                        txtFirstName.setError("Required");
                    }else if(TextUtils.isEmpty(mobile)){
                        txtMobile.setError("Required");
                    }else if (TextUtils.isEmpty(email)) {
                        addContacts(firstName,lastName,mobile,email,companyName,notes);
                    }
                    else if(imageUri == null|| TextUtils.isEmpty(lastName) ||  TextUtils.isEmpty(companyName) || !TextUtils.isEmpty(email) || TextUtils.isEmpty(notes)){
                        if(!isValidEmail(email))
                        {
                            Toast.makeText(AddContact.this, "Please Enter Proper Email", Toast.LENGTH_SHORT).show();
                        }else {
                            addContacts(firstName,lastName,mobile,email,companyName,notes);
                        }

                        //Toast.makeText(AddContact.this, "Please fill all details including image.", Toast.LENGTH_SHORT).show();
                    }else {
                        addContacts(firstName,lastName,mobile,email,companyName,notes);
                    }
                }else {
                    Toast.makeText(AddContact.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //dbHelper = new DbHelper(this);

        //get intent data..
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode",false);

        Intent intent2 = getIntent();
        DialerContact = intent2.getStringExtra("contactNumber");
        txtMobile.setText(DialerContact);

        Intent intent1 = getIntent();
        isQrData = intent1.getBooleanExtra("isQrData",false);
        QrFirstName = intent1.getStringExtra("firstName");
        QrLastName = intent1.getStringExtra("lastName");
        QrMobile = intent1.getStringExtra("mobile");
        QrEmail = intent1.getStringExtra("email");
        QrCompanyName = intent1.getStringExtra("companyName");

        if(isQrData)
        {
            txtFirstName.setText(QrFirstName);
            txtLastName.setText(QrLastName);
            txtMobile.setText(QrMobile);
            txtEmail.setText(QrEmail);
            if (QrCompanyName.equals("Company Name")){
                txtCompanyName.setText("");
            }else {
                txtCompanyName.setText(QrCompanyName);
            }
        }
        else if(isEditMode){

            //get other values form intent..
            id = intent.getStringExtra("ID");
            image = intent.getStringExtra("IMAGE");
            firstName = intent.getStringExtra("FIRSTNAME");
            lastName = intent.getStringExtra("LASTNAME");
            mobile = intent.getStringExtra("MOBILE");
            email = intent.getStringExtra("EMAIL");
            companyName = intent.getStringExtra("COMPANYNAME");
            notes = intent.getStringExtra("NOTES");

            //set values to edit text
            txtFirstName.setText(firstName);
            txtLastName.setText(lastName);
            txtMobile.setText(mobile);
            txtEmail.setText(email);
            txtCompanyName.setText(companyName);
            txtNotes.setText(notes);

            imageUri = Uri.parse(image);

            addImage.setImageURI(imageUri);

        }



        //logic for action bar..

        //back button in action bar
       // actionBar = getSupportActionBar();
      //  actionBar.setTitle("action bar");
      //  actionBar.setDisplayHomeAsUpEnabled(true);


        //set action bar in update and add contact label..


    }

    private String generateUniqueFilepath() {
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        String contactId = txtLastName.getText().toString() + "_" + UUID.randomUUID().toString();

        String filepath = "ContactPhotos/" + "Photos/" + "Contact_" + firebaseUser1.getUid() + "/" + contactId;
        return filepath;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    private void addContacts(String firstName, String lastName, String mobile, String email, String companyName, String notes) {
        pd.setMessage("Adding Contact");
        pd.show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String filepath = generateUniqueFilepath();

        // Check if imageUri is null
        if (imageUri == null) {
            // If no image is selected, use a default image
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            int defaultImageResource = R.drawable.avatar3; // Replace default_image with your default image resource
            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + defaultImageResource);
        }

        // Proceed with uploading the image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepath);

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();

                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();

                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        pd.dismiss();

                        String imageURL = uri.toString();

                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("imageURL", imageURL);
                        hashMap.put("contactFirstName", firstName);
                        hashMap.put("contactLastName", lastName);
                        hashMap.put("contactEmail", email);
                        hashMap.put("contactMobile", mobile);
                        hashMap.put("contactCompanyName", companyName);
                        hashMap.put("contactNotes", notes);
                        hashMap.put("contactFavorite",false);

                        String key = FirebaseDatabase.getInstance().getReference().child("Contacts").child(firebaseUser.getUid()).push().getKey();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(firebaseUser.getUid()).child(key);
                        reference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(AddContact.this, "Contact Added", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddContact.this, "Failed to add contact. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddContact.this, "Failed to upload image. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
                addImage.setImageURI(imageUri);
                //if(myAddImage != null){
                // myViewAddImg.setVisibility(View.INVISIBLE);
                //}
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Toast.makeText(getApplicationContext(),"Something wrong",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();

    }
}