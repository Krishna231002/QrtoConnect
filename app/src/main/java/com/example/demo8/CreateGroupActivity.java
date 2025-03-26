package com.example.demo8;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class CreateGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getSupportFragmentManager().beginTransaction().add(R.id.gchatcontainer,
                //new GetGroupDetailFragment()).commit();

       /* getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().add(R.id.gchatcontainer,
                new GetGroupDetailFragment()).commit();*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}