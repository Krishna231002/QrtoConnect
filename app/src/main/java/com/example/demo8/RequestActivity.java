package com.example.demo8;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Adapter.RequestAdapter;
import com.example.demo8.MyModels.ChannelRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity {

    RequestAdapter requestAdapter;
    RecyclerView recyclerView;
    FirebaseUser mUser;
    DatabaseReference mDataRef;
    List<ChannelRequestModel> requestlist;
    ImageView reqBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mUser = FirebaseAuth.getInstance().getCurrentUser();



        reqBack=findViewById(R.id.reqBack);
        recyclerView = findViewById(R.id.req_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestlist = new ArrayList<>(); // Initialize the list
        requestAdapter = new RequestAdapter(this, requestlist);
        recyclerView.setAdapter(requestAdapter);

        loadRequest();

        reqBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void loadRequest() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        mDataRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //String channelID = snapshot.getChildren().ge;
                // String channelID = snapshot.getValue(ChannelRequestModel.class).getReqChannelName();
                //BroadcastChannelModel requestModel = snapshot.getValue(BroadcastChannelModel.class);
                //Toast.makeText(RequestActivity.this, requestModel.toString(), Toast.LENGTH_SHORT).show();
                //String id = requestModel.getAdminID();

                requestlist.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){

                        // Get the key/ID of the request
                        String requestId = ds.getKey();
                        ChannelRequestModel requestModel = ds.getValue(ChannelRequestModel.class);
                        if (requestModel != null) {

                            String creatorUserId = ds.child("creatorUserId").getValue(String.class);

                            if (creatorUserId != null && creatorUserId.equals(firebaseUser.getUid())) {
                                //Add the request to the list
                                requestModel.setReqChannelId(requestId);
                                requestlist.add(requestModel);
                            }
                        }
                    }
                    requestAdapter.notifyDataSetChanged();
                }
                Log.e("checkSnap","Sanpshot not exists");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadRequest();
    }
}