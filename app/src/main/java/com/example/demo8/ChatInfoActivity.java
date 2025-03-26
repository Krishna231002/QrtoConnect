package com.example.demo8;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo8.Adapter.ChatAdapter;
import com.example.demo8.MyModels.BlockContactModel;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.Permissions.Permissions;
import com.example.demo8.databinding.ActivityChatDetailBinding;
import com.example.demo8.databinding.ActivityChatInfoBinding;
import com.example.demo8.databinding.ActivityGroupInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class ChatInfoActivity extends AppCompatActivity {

    String chatImage,chatName;
    ActivityChatInfoBinding binding;

    String mobile;
    Permissions permissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.infoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        permissions=new Permissions();

        Intent intent = getIntent();
        chatImage = intent.getStringExtra("chatImage");
        chatName = intent.getStringExtra("chatName");
        mobile = getIntent().getStringExtra("mobile");

        if(chatImage==null)
        {
            chatName = intent.getStringExtra("groupToChatName");
            chatImage = intent.getStringExtra("groupToChatImage");
            mobile = getIntent().getStringExtra("groupToChatMobile");
        }

        binding.infoToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.collapsingToolbar.setTitle(chatName);
        Picasso.get().load(chatImage).placeholder(R.drawable.avatar).into(binding.expandedImage);

        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser1.getUid());

// Query to find the contact with the matching mobile number
        reference1.orderByChild("contactMobile").equalTo(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Retrieve the key of the contact with the matching mobile number
                    String contactKey = dataSnapshot.getKey();


                    DatabaseReference blockContactRef = FirebaseDatabase.getInstance().getReference("BlockedContacts").child(firebaseUser1.getUid()).child(contactKey);


                    DatabaseReference contactReference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser1.getUid()).child(contactKey);
                    contactReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String contactFirstName = snapshot.child("contactFirstName").getValue(String.class);
                                String contactLastName = snapshot.child("contactLastName").getValue(String.class);
                                String contactEmail = snapshot.child("contactEmail").getValue(String.class);
                                String contactCompanyName = snapshot.child("contactCompanyName").getValue(String.class);
                                String contactNotes = snapshot.child("contactNotes").getValue(String.class);
                                String contactMobile = snapshot.child("contactMobile").getValue(String.class);
                                String imageURL = snapshot.child("imageURL").getValue(String.class);

                                // Now you have the contact details, you can use them as needed
                                binding.btnEditName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //Toast.makeText(ChatInfoActivity.this, "Working", Toast.LENGTH_SHORT).show();

                                        Intent intent1 = new Intent(ChatInfoActivity.this,UpdateContact.class);
                                        intent1.putExtra("Image",imageURL);
                                        intent1.putExtra("FirstName",contactFirstName);
                                        intent1.putExtra("LastName",contactLastName);
                                        intent1.putExtra("Mobile",contactMobile);
                                        intent1.putExtra("Email",contactEmail);
                                        intent1.putExtra("CompanyName",contactCompanyName);
                                        intent1.putExtra("Notes",contactNotes);
                                        intent1.putExtra("Key",contactKey);

                                        startActivity(intent1);

                                    }
                                });

                                binding.cardBlockContact.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        contactReference.removeValue(); // Remove contact from the database

                                        blockContactRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                BlockContactModel blockContact = new BlockContactModel(contactEmail,contactFirstName,contactLastName,contactMobile,contactCompanyName,contactNotes,imageURL,contactKey);
/*
                                                blockContact.blockFirstName = contactFirstName;
                                                blockContact.blockLastName = contactLastName;
                                                blockContact.blockEmail = contactEmail;
                                                blockContact.blockMobile = contactMobile;
                                                blockContact.blockKey = contactKey;
                                                blockContact.blockNotes = contactNotes;
                                                blockContact.blockImageURL = imageURL;
                                                blockContact.blockCompanyName = contactCompanyName;
*/
                                                blockContactRef.setValue(blockContact).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(ChatInfoActivity.this, contactFirstName+" "+contactLastName +" is Blocked", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                            }

                                        });


                                      /*  contactReference.removeValue();

                                        blockContactRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                BlockContactModel blockContact = new BlockContactModel(contactEmail,contactFirstName,contactLastName,contactMobile,contactCompanyName,contactNotes,imageURL,contactKey);
/*
                                                blockContact.blockFirstName = contactFirstName;
                                                blockContact.blockLastName = contactLastName;
                                                blockContact.blockEmail = contactEmail;
                                                blockContact.blockMobile = contactMobile;
                                                blockContact.blockKey = contactKey;
                                                blockContact.blockNotes = contactNotes;
                                                blockContact.blockImageURL = imageURL;
                                                blockContact.blockCompanyName = contactCompanyName;
                                                blockContactRef.setValue(blockContact).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(ChatInfoActivity.this, contactFirstName+" "+contactLastName +" is Blocked", Toast.LENGTH_SHORT).show();
                                                        Intent intent1 = new Intent(ChatInfoActivity.this, MainActivity.class);
                                                        startActivity(intent1);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });*/
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled event
                        }
                    });


                    break; // Break the loop after finding the contact
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}