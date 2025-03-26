package com.example.demo8.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.GroupMessageActivity;
import com.example.demo8.GroupModels;
import com.example.demo8.databinding.GroupItemBinding;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    ArrayList<GroupModels> groupModels;

    Context context;

    public GroupChatAdapter(ArrayList<GroupModels> groupModels, Context context) {
        this.groupModels = groupModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GroupItemBinding binding = GroupItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (groupModels != null) {

            GroupModels groupModel = groupModels.get(position);




            String groupName =groupModel.getName();

            holder.binding.setGroupModel(groupModel);
            holder.binding.groupNameList.setText(groupName);
            Glide.with(context).load(groupModel.getImage()).into(holder.binding.grProfileImage);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), GroupMessageActivity.class);
                intent.putExtra("groupIdToMessage",groupModel.getId());
                intent.putExtra("groupNameToMessage",groupModel.getName());
                intent.putExtra("groupImageToMessage",groupModel.getImage());

                intent.putExtra("groupModel",groupModel);
                context.startActivity(intent);
            });

        }

    }

    @Override
    public int getItemCount() {
        if (groupModels != null)
            return groupModels.size();
        else
            return 0;
    }

    public void setGroupModels(@NonNull ArrayList<GroupModels> groupModels) {
        this.groupModels = groupModels;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private GroupItemBinding binding;

        public ViewHolder(@NonNull GroupItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
