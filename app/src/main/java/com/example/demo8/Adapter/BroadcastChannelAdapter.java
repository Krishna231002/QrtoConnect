package com.example.demo8.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.BroadcastChannelMessageActivity;
import com.example.demo8.FullImageView;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.example.demo8.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BroadcastChannelAdapter extends RecyclerView.Adapter<BroadcastChannelAdapter.ViewHolder> {


    List<BroadcastChannelModel> channelModels;
    Context context;
    FirebaseUser mUser;
    DatabaseReference mUserRef,requestRef,broadcastChannelRef;

    public BroadcastChannelAdapter(List<BroadcastChannelModel> channelModels, Context context) {
        this.channelModels = channelModels;
        this.context = context;
    }

    @NonNull
    @Override
    public BroadcastChannelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_channel_item, parent, false);
        return new BroadcastChannelAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastChannelAdapter.ViewHolder holder, int position) {

        BroadcastChannelModel channelModel = channelModels.get(position);
        String channelName = channelModel.getChannelName();
        String channelCreator = channelModel.getAdminName();


        holder.channelName.setText(channelName);
        holder.creatorName.setText(channelCreator);
        Glide.with(context).load(channelModel.getChannelImage()).into(holder.channelImage);

        holder.channelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View dialogView = holder.inflater.inflate(R.layout.dialog_image_view,null);

                ImageView back = dialogView.findViewById(R.id.backImage);
                ImageView imageView = dialogView.findViewById(R.id.profileImage);
                TextView userName = dialogView.findViewById(R.id.txtUserName);
                Glide.with(context).load(channelModel.getChannelImage()).into(imageView);
                userName.setText(channelName);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(context, FullImageView.class);
                        intent.putExtra("image",channelModel.getChannelImage());
                        context.startActivity(intent);
                    }
                });

                back.setOnClickListener(new View.OnClickListener() {
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

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BroadcastChannelMessageActivity.class);
                intent.putExtra("channelId", channelModel.getChannelId());
                intent.putExtra("channelName", channelModel.getChannelName());
                intent.putExtra("channelImage", channelModel.getChannelImage());
                intent.putExtra("creatorName",channelModel.getAdminName());
                intent.putExtra("creatorId",channelModel.getAdminId());
                intent.putExtra("role",channelModel.getRole());
                intent.putExtra("channelModel",channelModel);
                context.startActivity(intent);
            }
        });

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("UsersDetails").child(mUser.getUid());
        broadcastChannelRef = FirebaseDatabase.getInstance().getReference().child("Broadcast Channel");


    }

    public void setChannelModels(List<BroadcastChannelModel> channelModels) {
        this.channelModels = channelModels;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return channelModels.size();
    }

    public void filterList(List<BroadcastChannelModel> filteredList) {
        channelModels = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        TextView channelName,creatorName;
        RelativeLayout relativeLayout;

        private LayoutInflater inflater;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channelImage);
            channelName = itemView.findViewById(R.id.broadcastCName);
            creatorName = itemView.findViewById(R.id.broadcastCCreatorName);
            relativeLayout = itemView.findViewById(R.id.mainLayout);
            inflater = LayoutInflater.from(context);
        }
    }
}
