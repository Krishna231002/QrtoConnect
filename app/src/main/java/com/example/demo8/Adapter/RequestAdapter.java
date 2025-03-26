package com.example.demo8.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.MyModels.BroadcastChannelMemberModel;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.example.demo8.MyModels.ChannelRequestModel;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.Viewholder> {

    Context context;
    List<ChannelRequestModel> requestlist;
    String Token,reqUserId;


    public RequestAdapter(Context context, List<ChannelRequestModel> requestlist) {
        this.context = context;
        this.requestlist = requestlist;
    }

    @NonNull
    @Override
    public RequestAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_items,parent,false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.Viewholder holder, int position) {
        final ChannelRequestModel requestModel = requestlist.get(position);

        String Id = requestModel.getReqChannelId();
        String Image = requestModel.getReqImage();
        String Name = requestModel.getReqName();
       // Toast.makeText(context, "Name : " + Name, Toast.LENGTH_SHORT).show();
        String ChannelName = requestModel.getReqChannelName();
        Token = requestModel.getReqToken();
        reqUserId  = requestModel.getReqUserId();

        Glide.with(context).load(Image).into(holder.reqImage);
        holder.reqName.setText(Name);
        holder.reqChannelName.setText(ChannelName);

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel");
                    DatabaseReference reqChannelRef = FirebaseDatabase.getInstance().getReference().child("Requests");

                    reqChannelRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Get the key/ID of the request
                                String reqChannelId = ds.getKey();
                                // Check if the request ID matches the channel ID
                                if (reqChannelId.equals(requestModel.getReqChannelId())) {
                                    // Perform database operations if the IDs match
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.hasChildren()) {
                                                // Iterate through each child node under "Broadcast Channel"
                                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                    // Retrieve the channelId from each child
                                                    String channelId = childSnapshot.getKey();
                                                    // Use the channelId as needed
                                                    if (channelId != null && channelId.equals(requestModel.getReqChannelId())) {
                                                        // Perform database operations if the channel IDs match
                                                        // Add the user as a member to the channel
                                                        /*HashMap<String, Object> memberMap = new HashMap<>();
                                                        memberMap.put("id", reqUserId);
                                                        memberMap.put("role", "member");
                                                        memberMap.put("token", Token);*/

                                                        BroadcastChannelMemberModel memberModel = new BroadcastChannelMemberModel();
                                                        memberModel.token=Token;
                                                        memberModel.role="Member";
                                                        memberModel.id=reqUserId;

                                                        reference.child(channelId).child("Members").child(reqUserId).setValue(memberModel)
                                                                .addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Toast.makeText(context, "Failed to add member: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                        // Remove the request from the "Requests" node
                                                        reqChannelRef.child(requestModel.getReqChannelId()).removeValue();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

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

                // Remove the request from the list
                requestlist.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                notifyItemRangeChanged(holder.getAdapterPosition(), requestlist.size());
            }
        });


        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference("Requests");

                    requestReference.child(requestModel.getReqChannelId()).removeValue();

                    // Remove the request from the list
                    requestlist.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    notifyItemRangeChanged(holder.getAdapterPosition(), requestlist.size());
                }
            }
        });
        holder.reqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return requestlist.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        CircleImageView reqImage;
        TextView reqName,reqChannelName;
        ImageButton btnCancel,btnAccept;
        LinearLayout reqLayout;
        public Viewholder(@NonNull View itemView) {
            super(itemView);

            reqImage = itemView.findViewById(R.id.req_image);
            reqName = itemView.findViewById(R.id.req_name);
            reqChannelName = itemView.findViewById(R.id.req_Channel_name);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            reqLayout = itemView.findViewById(R.id.req_layout);
        }
    }
}
