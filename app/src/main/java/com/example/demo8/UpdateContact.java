package com.example.demo8;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.demo8.MyModels.Contact;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateContact extends AppCompatActivity {

    EditText upd_cFirstName,upd_cLastName,upd_cNumber,upd_cEmail,upd_cCompanyName,upd_cNotes;
    Button upd_cSaveBtn,upd_cCancelBtn;
    CircleImageView upd_cImage;
    FirebaseUser firebaseUser;
    ImageView upback;

    Uri imageUri = null;
    String contactFirstName, contactLastName, contactNumber,contactCompanyName,contactNotes,contactEmail,imageUrl,key,oldImageUrl;
    Uri uri;
    ProgressDialog pd;
    DatabaseReference reference;
    StorageReference storageReference;
    public static final int GALLERY_IMAGE_CODE = 100;
    public static final int CAMERA_IMAGE_CODE = 200;

    //back button on action bar..
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        upd_cImage = findViewById(R.id.my_update_image);
        upd_cFirstName = findViewById(R.id.my_txt_first_name);
        upd_cLastName = findViewById(R.id.my_txt_last_name);
        upd_cNumber = findViewById(R.id.my_txt_mobile_number);
        upd_cEmail = findViewById(R.id.my_txt_email);
        upd_cCompanyName = findViewById(R.id.my_txt_company_name);
        upd_cNotes = findViewById(R.id.txt_notes);
        upd_cSaveBtn = findViewById(R.id.save_btn);
        upd_cCancelBtn = findViewById(R.id.cancel_btn);
        upback=findViewById(R.id.upContactBack);

        Permissions();

        pd = new ProgressDialog(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        upd_cCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts. StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult (ActivityResult result) {
                        if (result.getResultCode () == Activity. RESULT_OK) {
                            Intent data = result.getData();
                            uri= data.getData();
                            upd_cImage.setImageURI(uri);
                        } else {
                            Toast.makeText(UpdateContact.this,"No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        Bundle bundle = getIntent().getExtras ();
        if (bundle != null) {
            Glide.with(UpdateContact.this).load(bundle.getString("Image")).into(upd_cImage);
            upd_cFirstName.setText(bundle.getString("FirstName"));
            upd_cLastName.setText(bundle.getString("LastName"));
            upd_cEmail.setText(bundle.getString( "Email"));
            upd_cNumber.setText(bundle.getString("Number"));
            upd_cCompanyName.setText(bundle.getString("CompanyName"));
            upd_cNotes.setText(bundle.getString("Notes"));
            key = bundle.getString("Key");
            oldImageUrl = bundle.getString("Image");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid()).child(key);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Contact contact= snapshot.getValue(Contact.class);

                upd_cFirstName.setText(contact.getContactFirstName());
                upd_cLastName.setText(contact.getContactLastName());
                upd_cNumber.setText(contact.getContactMobile());
                upd_cCompanyName.setText(contact.getContactCompanyName());
                upd_cEmail.setText(contact.getContactEmail());
                upd_cNotes.setText(contact.getContactNotes());

                Glide.with(getApplicationContext()).load(contact.getImageURL()).into(upd_cImage);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });

        upback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        upd_cImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();
            }
        });

        upd_cSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkConnected()) {
                    contactFirstName = upd_cFirstName.getText().toString();
                    contactLastName = upd_cLastName.getText().toString();
                    contactEmail = upd_cEmail.getText().toString();
                    contactNumber = upd_cNumber.getText().toString();
                    contactCompanyName = upd_cCompanyName.getText().toString();
                    contactNotes = upd_cNotes.getText().toString();

                    if(TextUtils.isEmpty(contactFirstName)){
                        upd_cFirstName.setError("Required");
                    }else if(TextUtils.isEmpty(contactNumber)){
                        upd_cNumber.setError("Required");
                    } else if (TextUtils.isEmpty(contactEmail)) {
                        updateContact(contactFirstName, contactLastName, contactEmail, contactNumber, contactCompanyName, contactNotes);
                    }
                    else if( TextUtils.isEmpty(contactLastName) ||  TextUtils.isEmpty(contactCompanyName) || !TextUtils.isEmpty(contactEmail) || TextUtils.isEmpty(contactNotes)){
                        if(!isValidEmail(contactEmail))
                        {
                            Toast.makeText(UpdateContact.this, "Please Enter Proper Email", Toast.LENGTH_SHORT).show();
                        }else {
                            updateContact(contactFirstName, contactLastName, contactEmail, contactNumber, contactCompanyName, contactNotes);
                        }

                        //Toast.makeText(AddContact.this, "Please fill all details including image.", Toast.LENGTH_SHORT).show();
                    }else {
                        updateContact(contactFirstName, contactLastName, contactEmail, contactNumber, contactCompanyName, contactNotes);
                    }

                }else {
                    Toast.makeText(UpdateContact.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }

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
    private void updateContact(String contactFirstName, String contactLastName, String contactEmail, String contactNumber, String contactCompanyName, String contactNotes) {
        pd.setMessage("Updating");
        pd.show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String filepath = "ContactPhotos/" + "Photos/" + "Contact_" + firebaseUser.getUid() + "/" ;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid()).child(key);

        // Check if imageUri is null
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepath).child(key);

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

                            hashMap.put("contactFirstName", contactFirstName);
                            hashMap.put("contactLastName", contactLastName);
                            hashMap.put("contactEmail", contactEmail);
                            hashMap.put("contactMobile" , contactNumber);
                            hashMap.put("contactCompanyName", contactCompanyName);
                            hashMap.put("contactNotes", contactNotes);
                            hashMap.put("imageURL",imageURL);

                            reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();

                                    Toast.makeText(UpdateContact.this, "Updated", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            });
                        }
                    });
                }
            });
        } else {
            // If imageUri is null, use oldImageUrl instead
            HashMap<String, Object> hashMap = new HashMap<>();

            hashMap.put("contactFirstName", contactFirstName);
            hashMap.put("contactLastName", contactLastName);
            hashMap.put("contactEmail", contactEmail);
            hashMap.put("contactMobile" , contactNumber);
            hashMap.put("contactCompanyName", contactCompanyName);
            hashMap.put("contactNotes", contactNotes);

            reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pd.dismiss();

                    Toast.makeText(UpdateContact.this, "Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            });
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY_IMAGE_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            upd_cImage.setImageURI(imageUri);
        }

        if (requestCode == CAMERA_IMAGE_CODE && resultCode == RESULT_OK) {
            upd_cImage.setImageURI(imageUri);
        }
        Bundle bundle = getIntent().getExtras ();
        // Check if imageUri is null and assign oldImageUrl if it is
        /*if (imageUri == null) {
            oldImageUrl = bundle.getString("Image");
            imageUri= Uri.parse(oldImageUrl);
            upd_cImage.setImageURI(imageUri);
        }*/
    }
}