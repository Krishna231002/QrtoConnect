package com.example.demo8;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.github.chrisbanes.photoview.PhotoView;

public class FullImageView extends AppCompatActivity {
    private PhotoView enlargeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);

        enlargeImage=findViewById(R.id.enlargeImage);

        String image = getIntent().getStringExtra("image");

        Glide.with(this)
                .load(image)
                .into(enlargeImage);
    }
}