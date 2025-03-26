package com.example.demo8.Fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.demo8.MainActivity;
import com.example.demo8.R;
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

public class AddContactFragment extends Fragment {

    CircleImageView addImage;
    TextInputEditText txtFirstName,txtLastName,txtMobile,txtEmail,txtCompanyName,txtNotes;
    Button btnSave,btnCancel;
    View viewAddImg;
    FirebaseUser firebaseUser;

    ProgressDialog pd;

    //create variable for store user's input..
    String id,firstName,lastName,mobile,email,companyName,notes,image;
    Boolean isEditMode;
    //create action bar
    ActionBar actionBar;

    private MeowBottomNavigation bottomNavigation;
    //permission constant

    public static final int GALLERY_IMAGE_CODE =100;
    public static final int CAMERA_IMAGE_CODE =200;

    //Image Uri var
    Uri imageUri;
    //data from qr code..
    String QrFirstName,QrLastName,QrMobile,QrEmail,QrCompanyName;
    Boolean isQrData;
    String DialerContact;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        addImage = view.findViewById(R.id.my_add_image);
        txtFirstName = view.findViewById(R.id.my_txt_first_name);
        txtLastName = view.findViewById(R.id.my_txt_last_name);
        txtMobile = view.findViewById(R.id.my_txt_mobile_number);
        txtEmail = view.findViewById(R.id.my_txt_email);
        txtCompanyName = view.findViewById(R.id.my_txt_company_name);
        txtNotes = view.findViewById(R.id.txt_notes);
        btnSave = view.findViewById(R.id.save_btn);
        btnCancel = view.findViewById(R.id.cancel_btn);
        viewAddImg = view.findViewById(R.id.view_add_img);
        bottomNavigation = getActivity().findViewById(R.id.bottomNavigation);

        Permissions();
        pd = new ProgressDialog(getActivity());
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = txtFirstName.getText().toString();
                lastName = txtLastName.getText().toString();
                mobile = txtMobile.getText().toString();
                companyName = txtCompanyName.getText().toString();
                email = txtEmail.getText().toString();
                notes = txtNotes.getText().toString();

                if(imageUri == null||TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(companyName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(notes)){
                    Toast.makeText(getActivity(), "Please fill all details including image.", Toast.LENGTH_SHORT).show();
                }else {
                    addContacts(firstName,lastName,mobile,email,companyName,notes);
                }
            }
        });

        //get intent data..
        Intent intent = getActivity().getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode",false);

        Intent intent2 = getActivity().getIntent();
        DialerContact = intent2.getStringExtra("contactNumber");
        txtMobile.setText(DialerContact);

        Intent intent1 = getActivity().getIntent();
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
            txtCompanyName.setText(QrCompanyName);
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


        return view;
    }

    private String generateUniqueFilepath() {
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        String contactId = txtLastName.getText().toString() + "_" + UUID.randomUUID().toString();

        String filepath = "ContactPhotos/" + "Photos/" + "Contact_" + firebaseUser1.getUid() + "/" + contactId;
        return filepath;
    }

    private void addContacts(String firstName, String lastName, String mobile, String email, String companyName, String notes) {
        pd.setMessage("Adding Contact");
        pd.show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String filepath = generateUniqueFilepath();

        if (isNetworkConnected()) {
            // Upload data to FirebaseRealtimeDatabase
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

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


                            String key = reference.child("Contacts").child(firebaseUser.getUid()).push().getKey();
                            reference.child("Contacts").child(firebaseUser.getUid()).child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Contact Added", Toast.LENGTH_SHORT).show();
                                    //finish(); // You don't need finish() in a fragment
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Failed to add contact. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Failed to upload image. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Queue data locally if device is offline
            pd.dismiss();
            //queueDataLocally(firstName, lastName, mobile, email, companyName, notes,imageUri);
            startActivity(new Intent(getActivity(), MainActivity.class));
            Toast.makeText(getActivity(), "Check Your Internet Connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void imagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_IMAGE_CODE);

    }

    private void Permissions() {
        Dexter.withContext(getActivity())
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == GALLERY_IMAGE_CODE){
                //pick image from gallery
                //crop image..
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(), this);
            } else if (requestCode == CAMERA_IMAGE_CODE) {
                //pick image from camera
                //crop image..
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(), this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                //cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                addImage.setImageURI(imageUri);
                //if(myAddImage != null){
                // myViewAddImg.setVisibility(View.INVISIBLE);
                //}
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Toast.makeText(getActivity().getApplicationContext(),"Something wrong",Toast.LENGTH_LONG).show();
            }
        }
    }

}
