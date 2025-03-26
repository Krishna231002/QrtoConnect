package com.example.demo8.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.demo8.AddContact;
import com.example.demo8.BlockedContactActivity;
import com.example.demo8.CaptureActivityOrientation;
import com.example.demo8.EditMyDetails;
import com.example.demo8.Users;
import com.example.demo8.QrGenerate;
import com.example.demo8.R;
import com.example.demo8.WelcomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private FirebaseAuth Auth;
    private CircleImageView myIcon;
    private FirebaseUser firebaseUser;
    private ImageButton myEditBtn, qrGenerateBtn, qrScanBtn;
    private TextView myName, myMobile, txtSetName, txtSetMobile, txtSetEmail, txtSetCompanyName;
    private Button signOutBtn,btnBlock;
    String image, firstName, lastName, mobile, email, companyName,userId,updatedImageUri;

    private ProgressDialog pd;
    private static final int EDIT_DETAILS_REQUEST_CODE = 1001;



    public AccountFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        myIcon = rootView.findViewById(R.id.my_icon);
        myEditBtn = rootView.findViewById(R.id.my_edit_btn);
        myName = rootView.findViewById(R.id.my_name);
        myMobile = rootView.findViewById(R.id.my_mobile_number);
        qrGenerateBtn = rootView.findViewById(R.id.qr_generate_btn);
        qrScanBtn = rootView.findViewById(R.id.qr_scan_btn);
        txtSetName = rootView.findViewById(R.id.txt_set_name);
        txtSetMobile = rootView.findViewById(R.id.txt_set_mobile);
        txtSetEmail = rootView.findViewById(R.id.txt_set_email);
        txtSetCompanyName = rootView.findViewById(R.id.txt_set_company_name);
        signOutBtn = rootView.findViewById(R.id.sign_out_btn);
        btnBlock = rootView.findViewById(R.id.block_btn);

        Bundle bundle =getArguments();
        if (bundle != null) {
            String userID = bundle.getString("userId",userId);
            // Use the userID as needed
        }


        Auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
       // pd = new ProgressDialog(getContext());
       // pd.setMessage("Loading Data");

        myEditBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditMyDetails.class);

            intent.putExtra("first_name", firstName);
            intent.putExtra("last_name", lastName);
            intent.putExtra("email", email);
            intent.putExtra("mobile", mobile);

            intent.putExtra("image",image);
            if(companyName != null) {
                intent.putExtra("company_name", companyName);
            }else {
                intent.putExtra("company_name", "");
            }
            intent.putExtra("isEdit", false);
            startActivityForResult(intent, EDIT_DETAILS_REQUEST_CODE);
        });

        qrGenerateBtn.setOnClickListener(v -> {
            if (!txtSetName.getText().toString().equals("Edit Name")) {
                Intent intent = new Intent(getContext(), QrGenerate.class);

                intent.putExtra("first_name", firstName);
                intent.putExtra("last_name", lastName);
                intent.putExtra("email", email);
                intent.putExtra("mobile", mobile);
                intent.putExtra("company_name", companyName);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please Fill The Details", Toast.LENGTH_SHORT).show();
            }
        });

        qrScanBtn.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(AccountFragment.this);
            intentIntegrator.setPrompt("scan Qr Code");
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.setCaptureActivity(CaptureActivityOrientation.class);
            intentIntegrator.initiateScan();
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.signOut();
                Toast.makeText(getContext(), "Signed Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), WelcomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        }) ;

        btnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BlockedContactActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void loadUserData() {
        //pd.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        // Retrieve user data
                        firstName = user.getFirstName();
                        lastName = user.getLastName();
                        mobile = user.getMobile();
                        email = user.getEmail();
                        companyName = user.getCompanyName();
                        image = user.getImageURL();

                        // Set retrieved data to UI elements
                        String name = firstName + " " + lastName;
                        myName.setText(name);
                        txtSetName.setText(name);
                        myMobile.setText(mobile);
                        txtSetMobile.setText(mobile);
                        txtSetEmail.setText(email);
                        txtSetCompanyName.setText(companyName);

                        if (getContext() != null && image != null)
                            Glide.with(getContext()).load(image).into(myIcon);

                      //  pd.dismiss(); // Dismiss progress dialog
                    } else {
                       // pd.dismiss(); // Dismiss progress dialog
                        Toast.makeText(getContext(), "Please Sign Up First", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), WelcomePage.class));
                    }
                } else {
                 //   pd.dismiss(); // Dismiss progress dialog
                    Toast.makeText(getContext(), "Please Sign Up First", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), WelcomePage.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // pd.dismiss(); // Dismiss progress dialog
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == EDIT_DETAILS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
                // Check if the result is from EditMyDetails activity

                updatedImageUri = data.getStringExtra("updatedImageUri");
                if (updatedImageUri != null) {
                    // Update UI with the new image
                    if (getContext() != null)
                        Glide.with(getContext()).load(updatedImageUri).into(myIcon);

                }
                loadUserData();

            }



            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

                if (intentResult != null) {
                    String content = intentResult.getContents();
                    if (content != null) {
                        String[] dataParts = content.split(",");
                        String QrFirstName = dataParts[0];
                        String QrLastName = dataParts[1];
                        String QrMobile = dataParts[2];
                        String QrEmail = dataParts[3];
                         String QrCompanyName = dataParts[4];

                        Intent intent1 = new Intent(getActivity(), AddContact.class);
                        intent1.putExtra("isQrData", true);
                        intent1.putExtra("firstName", QrFirstName);
                        intent1.putExtra("lastName", QrLastName);
                        intent1.putExtra("mobile", QrMobile);
                        intent1.putExtra("email", QrEmail);
                        intent1.putExtra("companyName", QrCompanyName);
                        startActivityForResult(intent1, 1);
                    }
                }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}
