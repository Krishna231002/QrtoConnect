package com.example.demo8;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class WelcomePage extends AppCompatActivity {

    Button btn_Login;
    EditText txtEmail, txtPass;
    TextView btn_SignUp,forgotPassword;
    String Email, Pass,fcmToken;
    FirebaseAuth auth;
    ProgressDialog pd;

    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        auth = FirebaseAuth.getInstance();


        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
            // If user is already signed in, start MainActivity and finish WelcomePage
            // retrieveTokenFromDatabaseAndStartMainActivity();
            return; // Return to prevent further execution
        }

        pd = new ProgressDialog(this);

        btn_Login = findViewById(R.id.btn_login);
        btn_SignUp = findViewById(R.id.txt_sign_up);
        txtEmail = findViewById(R.id.txt_email);
        txtPass = findViewById(R.id.txt_pass);
        forgotPassword=findViewById(R.id.txtforgotPassword);

        ImageView togglePassword = findViewById(R.id.toggle_password_login);

        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtPass.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Password is currently visible, hide it
                    txtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePassword.setImageResource(R.drawable.close_eye); // Set your closed eye icon
                } else {
                    // Password is currently hidden, show it
                    txtPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePassword.setImageResource(R.drawable.open_eye); // Set your open eye icon
                }
                // Move cursor to the end of the text
                txtPass.setSelection(txtPass.getText().length());
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in, so immediately start MainActivity and finish WelcomePage
                    startActivity(new Intent(WelcomePage.this, MainActivity.class));
                    finish();
                } else {
                    // User is signed out
                }
            }
        };



        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Email = txtEmail.getText().toString();
                Pass = txtPass.getText().toString();

                if (TextUtils.isEmpty(Email) || TextUtils.isEmpty(Pass)) {
                    Toast.makeText(WelcomePage.this, "Required", Toast.LENGTH_SHORT).show();
                } else {
                    if (isNetworkConnected()) {
                        LogmeIn(Email, Pass);
                    }else {
                        Toast.makeText(WelcomePage.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomePage.this, Sign_Up_Page.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomePage.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot,null);
                EditText email_box= dialogView.findViewById(R.id.edt_email_box);
                Button btnReset = dialogView.findViewById(R.id.btnResetPassword);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userEmail = email_box.getText().toString();
                        if (isNetworkConnected()) {
                            if (!isValidEmail(userEmail)) {
                                Toast.makeText(WelcomePage.this, "Enter your registered Email id", Toast.LENGTH_SHORT).show();
                            }

                            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(WelcomePage.this, "Check your email", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(WelcomePage.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(WelcomePage.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                if (dialog.getWindow() != null){
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog.show();
            }
        });

        // Start listening for auth state changes immediately in onCreate
        // FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
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

    private void LogmeIn(String email, String pass) {
        pd.setMessage("Sign in");
        pd.show();

        // Validate email
        if (!isValidEmail(email)) {
            pd.dismiss();
            Toast.makeText(WelcomePage.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password
        if (!isValidPassword(pass)) {
            pd.dismiss();
            Toast.makeText(WelcomePage.this, "Please enter a valid password (at least 8 characters)", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(task -> {
            pd.dismiss();

            // Retrieve the FCM token and start the main activity
            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
            {
                Toast.makeText(WelcomePage.this, "Log in", Toast.LENGTH_SHORT).show();
                retrieveTokenFromDatabaseAndStartMainActivity();
                startMainActivity();
            }else{
                Toast.makeText(WelcomePage.this, "Please verify your email.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(WelcomePage.this, "Please enter proper details", Toast.LENGTH_SHORT).show();
        });
    }


    private void retrieveTokenFromDatabaseAndStartMainActivity() {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("UsersDetails").child(FirebaseAuth.getInstance().getUid());
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    fcmToken = user.getToken();
                    // Start the main activity with the obtained FCM token
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void startMainActivity() {
        // Start the main activity
        Intent intent = new Intent(WelcomePage.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //getApplicationContext().startActivity(intent);
        // Finish the welcome page activity
        finish();
    }






    // Remove the AuthStateListener when the activity is stopped
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}
