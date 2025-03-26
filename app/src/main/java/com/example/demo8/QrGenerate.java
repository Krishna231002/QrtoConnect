package com.example.demo8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrGenerate extends AppCompatActivity {

    String image,firstName,lastName,mobile,email,companyName;
    ImageView setImage,qrBack;


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);


        //back button in action bar

        setImage = findViewById(R.id.set_image);
        qrBack=findViewById(R.id.qrCodeBack);


        Intent intent = getIntent();
        firstName = intent.getStringExtra("first_name");
        lastName = intent.getStringExtra("last_name");
        mobile = intent.getStringExtra("mobile");
        email = intent.getStringExtra("email");
        companyName = intent.getStringExtra("company_name");

        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        //reference.child("UsersDetails").child(firebaseUser.getUid()).

        if (companyName.isEmpty()){
            String company = "Company Name";
            String ans = firstName + "," +lastName + "," +mobile + "," +email + "," +company ;

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            try{
                BitMatrix bitMatrix = multiFormatWriter.encode(ans, BarcodeFormat.QR_CODE,300,300);

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                setImage.setImageBitmap(bitmap);

            }catch (WriterException e){
                throw new RuntimeException(e);
            }
        }else

        {
            String ans = firstName + "," +lastName + "," +mobile + "," +email + "," +companyName ;

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            try{
                BitMatrix bitMatrix = multiFormatWriter.encode(ans, BarcodeFormat.QR_CODE,300,300);

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                setImage.setImageBitmap(bitmap);

            }catch (WriterException e){
                throw new RuntimeException(e);
            }
        }

        qrBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }
}