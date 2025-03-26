package com.example.demo8.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.AllBroadcastChannelModel;
import com.example.demo8.MyModels.ChannelRequestModel;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllBroadCastChannelAdapter extends RecyclerView.Adapter<AllBroadCastChannelAdapter.ViewHolder> {

    private ArrayList<AllBroadcastChannelModel> channelModelsList;
    private Context context;
    private FirebaseUser mUser;
    private DatabaseReference requestRef;

    String userName,userMobile,userID,userImage,userToken;

    public AllBroadCastChannelAdapter(ArrayList<AllBroadcastChannelModel> channelModelsList, Context context) {
        this.channelModelsList = channelModelsList;
        this.context = context;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_broadcast_channel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AllBroadcastChannelModel channelModel = channelModelsList.get(position);
        String broadCastChannelName = channelModel.getChannelName();
        String channelCreator = channelModel.getAdminName();
        String channelId = channelModel.getChannelId();
        String adminId = channelModel.getAdminId();


        holder.broadcastCName.setText(broadCastChannelName);
        holder.channelCreator.setText(channelCreator);

        // Load image using Glide
        Glide.with(context).load(channelModel.getChannelImage()).into(holder.grProfileImage);

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference();

        mUserRef.child("UsersDetails").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userID = dataSnapshot.child("id").getValue(String.class);
                    userName = dataSnapshot.child("fullname").getValue(String.class);
                    userImage = dataSnapshot.child("imageURL").getValue(String.class);
                    userMobile = dataSnapshot.child("mobile").getValue(String.class);
                    userToken = dataSnapshot.child("token").getValue(String.class);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context.getApplicationContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set visibility of the button based on whether the current user is the admin
        DatabaseReference broadcastChannelRef = FirebaseDatabase.getInstance().getReference().child("All Broadcast Channel");
        broadcastChannelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String AdminId = snapshot.child(channelId).child("adminId").getValue(String.class);
                     if (mUser != null && mUser.getUid().equals(AdminId)) {
                        // Hide the button if the current user is the admin
                        holder.btnSendRequest.setVisibility(View.GONE);
                    } else {
                        // Show the button if the current user is not the admin
                        holder.btnSendRequest.setVisibility(View.VISIBLE);

                        // Check request status and set button color and text accordingly
                        requestRef.child(channelId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String status = snapshot.child("status").getValue(String.class);
                                    if (status != null && status.equals("pending")) {
                                        holder.btnSendRequest.setText("Cancel");
                                        holder.btnSendRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.btn_red));
                                    } else {
                                        holder.btnSendRequest.setText("Request");
                                        holder.btnSendRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.btn_green));
                                    }
                                } else {
                                    holder.btnSendRequest.setText("Request");
                                    holder.btnSendRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.btn_green));
                                }
                                //setChannelModels(channelModelsList);
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Error retrieving request status", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Set click listener for the button
                        holder.btnSendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String buttonText = ((TextView) v).getText().toString();
                                if (buttonText.equals("Request")) {
                                    sendRequest(channelId,AdminId,broadCastChannelName);
                                } else if (buttonText.equals("Cancel")) {
                                    cancelRequest(channelId);
                                }
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendRequest(String channelId,String adminId,String channelName) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("status", "pending");
        requestMap.put("channelId", channelId);
        requestMap.put("userId", mUser.getUid());
        requestMap.put("userImage", userImage);
        requestMap.put("userName", userName);
        requestMap.put("userToken", userToken);
        requestMap.put("creatorId", adminId);
        requestMap.put("channelName", channelName);

        ChannelRequestModel channelRequestModel = new ChannelRequestModel(mUser.getUid(),channelId,userImage,userName,channelName,"pending",userToken,adminId);


        requestRef.child(channelId).setValue(channelRequestModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void filterList(List<AllBroadcastChannelModel> filteredList) {
        channelModelsList = (ArrayList<AllBroadcastChannelModel>) filteredList;
        notifyDataSetChanged();
    }
    private void cancelRequest(String channelId) {
        // Remove request from the database
        requestRef.child(channelId).removeValue();
    }

    @Override
    public int getItemCount() {
        return channelModelsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView grProfileImage;
        TextView broadcastCName, btnSendRequest, channelCreator;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            grProfileImage = itemView.findViewById(R.id.gr_profile_image);
            broadcastCName = itemView.findViewById(R.id.broadcastCName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            channelCreator = itemView.findViewById(R.id.broadcastCCreatorName);
        }

    }
}
