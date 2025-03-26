package com.example.demo8;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Contacts;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Adapter.ContactAdapter;
import com.example.demo8.MyModels.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DialerActivity extends AppCompatActivity {

    EditText txtNumbers;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    ContactAdapter contactAdapter;
    List<Contact> contactsList;
    FirebaseUser firebaseUser;
    static int PERMISSION_CODE = 100;

    static int DISMISS_CODE = 100;

    TextView txtAddContact,txtSendSms;
    private String currentNumber = "";

    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        txtNumbers = findViewById(R.id.dialer_number);
        relativeLayout = findViewById(R.id.contactAddLayout);
        txtAddContact = findViewById(R.id.txtAddContact);
        txtSendSms = findViewById(R.id.txtSendSms);


        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contactsList = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, contactsList);
        recyclerView.setAdapter(contactAdapter);

        pd = new ProgressDialog(this);

        pd.dismiss();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        View.OnTouchListener numberButtonTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Handle button click
                    LinearLayout linearLayout = (LinearLayout) view;
                    TextView textView = (TextView) linearLayout.getChildAt(0); // Assuming the TextView is the first child

                    String number = textView.getText().toString(); // Get the text from the TextView

                    String currentNumber = txtNumbers.getText().toString();
                    int cursorPosition = txtNumbers.getSelectionStart();
                    String beforeCursor = currentNumber.substring(0, cursorPosition);
                    String afterCursor = currentNumber.substring(cursorPosition);

                    String updatedNumber = beforeCursor + number + afterCursor;
                    txtNumbers.setText(updatedNumber);
                    txtNumbers.setSelection(cursorPosition + number.length());
                    txtNumbers.setCursorVisible(false);

                    // Filter contacts
                    filterContacts(updatedNumber);

                    // Add vibration
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(50); // Vibrate for 50 milliseconds
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // Handle button release if needed
                }
                return false;
            }
        };

        findViewById(R.id.button_1).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_2).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_3).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_4).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_5).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_6).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_7).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_8).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_9).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_0).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_star).setOnTouchListener(numberButtonTouchListener);
        findViewById(R.id.button_hash).setOnTouchListener(numberButtonTouchListener);

        // Add OnClickListener for back press button
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClick(view);
            }
        });

        if (ContextCompat.checkSelfPermission(DialerActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DialerActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);

        }
        loadContacts();

        txtAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Progressing");
                pd.show();
                String number = txtNumbers.getText().toString();
                Intent intent = new Intent(DialerActivity.this, AddContact.class);
                intent.putExtra("contactNumber",number);
                startActivity(intent);
                pd.dismiss();
            }
        });

        txtSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(DialerActivity.this);
                builder.setTitle("Enter Your Message").setIcon(R.drawable.notification);

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
                        String number = txtNumbers.getText().toString();

                        // Check if the SMS text is empty
                        if (!TextUtils.isEmpty(smsText)) {
                            // Create the intent to send the SMS
                            //Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
                            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

                            // Send the SMS
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(number, null, smsText, pi, null);

                            // Show a toast message indicating that the message was sent successfully
                            Toast.makeText(DialerActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // If the SMS text is empty, show an error toast
                            Toast.makeText(DialerActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
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
    }

    private void loadContacts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Contact contact = ds.getValue(Contact.class);
                    if (contact != null) {
                        contact.setKey(ds.getKey());
                        contactsList.add(contact);
                    }
                }
                contactAdapter.notifyDataSetChanged();
                txtNumbers.setShowSoftInputOnFocus(false);
                txtNumbers.setCursorVisible(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
              /* if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // Log the error or handle it in an appropriate way
                    Log.e("TAG", "Error: " + error.getMessage());
                }*/

            }
        });

        txtNumbers.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                txtNumbers.setCursorVisible(true);
                return false;
            }
        });

        txtNumbers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this example
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter contacts
                filterContacts(s.toString());
                String enteredNumber = s.toString();

                // Filter contacts
                ArrayList<Contact> filteredContacts = new ArrayList<>();
                for (Contact contact : contactsList) {
                    if (contact.getContactMobile().contains(enteredNumber)) {
                        filteredContacts.add(contact);
                    }
                }

                if (filteredContacts.isEmpty()) {
                    relativeLayout.setVisibility(View.VISIBLE);
                }
                else {
                    relativeLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void filterContacts(String text) {
        ArrayList<Contact> filteredList = new ArrayList<>();
        for (Contact contact : contactsList) {
            if (contact.getContactMobile().contains(text)) {
                filteredList.add(contact);
            }
        }
        contactAdapter.filterList(filteredList);
    }

    public void onCallClick(View view) {
        String number = txtNumbers.getText().toString();
        if (!number.isEmpty()) {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + number));
            startActivity(i);
        }
    }

    public void onBackClick(View view) {
        String currentNumber = txtNumbers.getText().toString();
        int cursorPosition = txtNumbers.getSelectionStart();
        if (cursorPosition > 0) {
            String beforeCursor = currentNumber.substring(0, cursorPosition - 1);
            String afterCursor = currentNumber.substring(cursorPosition);
            txtNumbers.setText(beforeCursor + afterCursor);
            txtNumbers.setSelection(cursorPosition - 1);

            // Update currentNumber after updating txtNumbers
            this.currentNumber = beforeCursor + afterCursor;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISMISS_CODE) { // Replace YOUR_REQUEST_CODE with a unique integer value
            if (resultCode == RESULT_OK) {
                // Dismiss the ProgressDialog here
                pd.dismiss();
            }
        }
    }
}