package com.example.demo8;

import static androidx.test.InstrumentationRegistry.getContext;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.demo8.Adapter.ContactAdapter;
import com.example.demo8.Adapter.FavoriteAdapter;
import com.example.demo8.MyModels.BlockContactModel;
import com.example.demo8.MyModels.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

public class ContactDetails extends AppCompatActivity {

    ImageView dImage;
    TextView dFirstName1,dFirstName2,dLastName,dMobileNumber,dEmail,dCompanyName1,dCompanyName2,dNotes;String key = "";
    String imageUrl = "";
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    //this for edit icon click..
    String eMobileNumber,eEmail,uid,eCompanyName,eNotes;

    //image button for favorite..
    ImageButton favBtn,backBtn,editBtn,btnMessage,btnEmail,btnEdit;
    Button btnblock;
    String targetId,mobile;
    static int PERMISSION_CODE = 100;
    ZegoSendCallInvitationButton btnCall,btnVideoCall;
    RelativeLayout layoutAbout;
    LinearLayout layoutCompanyName,layoutNotes,txtAbout;
    ContactAdapter contactAdapter = new ContactAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);


        dImage = findViewById(R.id.detail_image);
        dFirstName1 = findViewById(R.id.detail_first_name1);
        dFirstName2 = findViewById(R.id.detail_first_name2);
        dLastName = findViewById(R.id.detail_last_name);
        dMobileNumber = findViewById(R.id.detail_mobile_number);
        dEmail = findViewById(R.id.detail_email);
        dCompanyName1 = findViewById(R.id.detail_company_name1);
        dCompanyName2 = findViewById(R.id.detail_company_name2);
        dNotes = findViewById(R.id.detail_notes);
        favBtn = findViewById(R.id.detail_fav_btn);
        backBtn = findViewById(R.id.btn_back);
        editBtn = findViewById(R.id.btn_edit);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnVideoCall = findViewById(R.id.btn_video_call);
        btnEmail = findViewById(R.id.btn_email);
        btnEdit = findViewById(R.id.btn_edit);
        btnblock=findViewById(R.id.block_btn);
        layoutAbout = findViewById(R.id.layoutAbout);
        layoutNotes = findViewById(R.id.layoutNotes);
        layoutCompanyName = findViewById(R.id.layoutCompanyName);
        txtAbout = findViewById(R.id.txtAbout);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            dFirstName1.setText(bundle.getString("FirstName"));
            dFirstName2.setText(bundle.getString("FirstName"));
            dLastName.setText(bundle.getString("LastName"));
            dMobileNumber.setText(bundle.getString("Mobile"));
            dCompanyName1.setText(bundle.getString("CompanyName"));
            dCompanyName2.setText(bundle.getString("CompanyName"));
            dNotes.setText(bundle.getString("Notes"));
            dEmail.setText(bundle.getString("Email"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");

            eEmail = bundle.getString("Email");
            eMobileNumber = bundle.getString("Mobile");
            uid = bundle.getString("uid");

            eCompanyName = bundle.getString("CompanyName");
            eNotes = bundle.getString("Notes");

            layoutAbout.setVisibility(View.VISIBLE);

            if(eCompanyName.isEmpty() && eNotes.isEmpty()){
                txtAbout.setVisibility(View.GONE);
                layoutAbout.setVisibility(View.GONE);
            }
            else {
                if (eCompanyName.isEmpty()){
                    layoutCompanyName.setVisibility(View.GONE);
                }
                else if (eNotes.isEmpty())
                {
                    layoutNotes.setVisibility(View.GONE);
                }
            }

            //Toast.makeText(this, eCompanyName + eNotes, Toast.LENGTH_SHORT).show();

            // Load image with Glide
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.account_circle_icon) // Placeholder image
                    .error(R.drawable.account_circle_icon) // Error image
                    .into(dImage);
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("UsersDetails").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mobile = snapshot.child("mobile").getValue(String.class);
                //Toast.makeText(ContactDetails.this, mobile, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Retrieve the user ID from Firebase
        reference.child("Contacts").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the user data exists
                if (dataSnapshot.exists()) {
                    // Retrieve the user ID from the snapshot
                    targetId = dataSnapshot.child(key).child("contactMobile").getValue(String.class);

                    if (targetId.equals(mobile)){
                        btnblock.setVisibility(View.GONE);
                    }
                } else {
                    // Handle the case where user data doesn't exist
                    Toast.makeText(ContactDetails.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the retrieval process
                Toast.makeText(ContactDetails.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference contactsRef = database.getReference("Contacts");
        DatabaseReference favoriteContactsRef = database.getReference("Favorite Contacts");
        String userId = firebaseUser.getUid();
        String contactId = key;

        favoriteContactsRef.child(userId).child(contactId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Toast.makeText(ContactDetails.this, snapshot.toString(), Toast.LENGTH_SHORT).show();
                if (snapshot.exists()) {
                    // The contact is already in the user's favorite contacts list
                    favBtn.setImageDrawable(ContextCompat.getDrawable(ContactDetails.this, R.drawable.custom_btn_fav_fill));
                } else {
                    // The contact is not in the user's favorite contacts list
                    favBtn.setImageDrawable(ContextCompat.getDrawable(ContactDetails.this, R.drawable.custom_btn_fav_unfill));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if the contact is already in the user's favorite contacts list
                favoriteContactsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild(contactId)) {
                            // Remove the contact from the favorite contacts list
                            favoriteContactsRef.child(userId).child(contactId).removeValue();
                            favBtn.setImageDrawable(ContextCompat.getDrawable(ContactDetails.this, R.drawable.custom_btn_fav_unfill));

                            Toast.makeText(ContactDetails.this, "Contact Removed From Favorite", Toast.LENGTH_SHORT).show();
                        } else {
                            // Add the contact to the favorite contacts list
                            contactsRef.child(userId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Contact contact = snapshot.getValue(Contact.class);
                                    favoriteContactsRef.child(userId).child(contactId).setValue(contact);
                                    favBtn.setImageDrawable(ContextCompat.getDrawable(ContactDetails.this, R.drawable.custom_btn_fav_fill));
                                    Toast.makeText(ContactDetails.this, "Contact Added On Favorite", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ContactDetails.this, "Error getting contact information", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ContactDetails.this, "Error getting favorite contacts list", Toast.LENGTH_SHORT).show();
                    }
                });

                contactAdapter.notifyDataSetChanged();

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = (bundle.getString("FirstName"));
                String lastName = (bundle.getString("LastName"));
                String number = (bundle.getString("Mobile"));
                String companyName = (bundle.getString("CompanyName"));
                String notes = (bundle.getString("Notes"));
                String email =(bundle.getString("Email"));
                String key = bundle.getString("Key");
                String imageUrl = bundle.getString("Image");

                Intent intent = new Intent(ContactDetails.this, UpdateContact.class);
                intent.putExtra("FirstName", firstName);
                intent.putExtra("LastName", lastName);
                intent.putExtra("Email", email);
                intent.putExtra("Number", number);
                intent.putExtra("Image", imageUrl);
                intent.putExtra("CompanyName", companyName);
                intent.putExtra("Notes", notes);
                intent.putExtra("Key", key);
                startActivity(intent);

            }
        });

        if (ContextCompat.checkSelfPermission(ContactDetails.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ContactDetails.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);

        }

        DatabaseReference contactReference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid()).child(key);
        DatabaseReference favoriteReference = FirebaseDatabase.getInstance().getReference("Favorite Contacts").child(firebaseUser.getUid()).child(key);
        DatabaseReference blockContactRef = FirebaseDatabase.getInstance().getReference("BlockedContacts").child(firebaseUser.getUid()).child(key);

        btnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = (bundle.getString("FirstName"));
                String lastName = (bundle.getString("LastName"));
                String number = (bundle.getString("Mobile"));
                String companyName = (bundle.getString("CompanyName"));
                String notes = (bundle.getString("Notes"));
                String email = (bundle.getString("Email"));
                String key = bundle.getString("Key");
                String imageUrl = bundle.getString("Image");

                contactReference.removeValue(); // Remove contact from the database
                favoriteReference.removeValue();

                blockContactRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        BlockContactModel blockContact = new BlockContactModel(email, firstName, lastName, number, companyName, notes, imageUrl, key);

                        blockContactRef.setValue(blockContact).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ContactDetails.this, firstName + " " + lastName + " is Blocked", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        contactAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCall.setVisibility(View.INVISIBLE);

                setVoiceCall(targetId);
            }
        });

        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVideoCall.setVisibility(View.INVISIBLE);

                setVideoCall(targetId);
            }
        });

        if (ContextCompat.checkSelfPermission(ContactDetails.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ContactDetails.this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_CODE);

        }
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create an AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetails.this);
                builder.setTitle("Enter Your Message").setIcon(R.drawable.notification_102);

                // Set custom layout for the dialog
                View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
                builder.setView(dialogView);

                // Find EditText view in custom layout
                final EditText input = dialogView.findViewById(R.id.editTextMessage);

                // Set up the buttons
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the text entered by the user
                        String smsText = input.getText().toString();
                        // Get the phone number
                        String number = eMobileNumber;

                        // Check if the SMS text is empty
                        if (!TextUtils.isEmpty(smsText)) {
                            // Create the intent to send the SMS
                            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

                            // Send the SMS
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(number, null, smsText, pi, null);

                            // Show a toast message indicating that the message was sent successfully
                            Toast.makeText(ContactDetails.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // If the SMS text is empty, show an error toast
                            Toast.makeText(ContactDetails.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Create and show the AlertDialog
                builder.show();
            }
        });



        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email1 = eEmail;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("mailto:" + email1));
                startActivity(i);
            }
        });
    }



    void setVoiceCall (String targetId) {

        btnCall.setIsVideoCall(false);
        btnCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        btnCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetId)));

    }
    void setVideoCall(String targetId) {

        btnVideoCall.setIsVideoCall(true);
        btnVideoCall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        btnVideoCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetId)));

    }

    @Override
    protected void onResume() {
        super.onResume();

        btnCall.setVisibility(View.VISIBLE);
        btnCall.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_btn_call_ui));
        btnVideoCall.setVisibility(View.VISIBLE);
        btnVideoCall.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_btn_call_text_video_ui));

    }
}