package com.example.demo8.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Interface.MemberItemInterface;
import com.example.demo8.Users;
import com.example.demo8.databinding.MemberItemLayoutBinding;

import java.util.ArrayList;

public class GroupMemberAdapter extends RecyclerView.Adapter <GroupMemberAdapter.ViewHolder>{

    ArrayList<Users> arrayList;


    public GroupMemberAdapter(@NonNull MemberItemInterface memberItemInterface) {
        this.memberItemInterface = memberItemInterface;
    }

    MemberItemInterface memberItemInterface;
    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        MemberItemLayoutBinding binding = MemberItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ViewHolder holder, int position) {

        if (arrayList!=null){
            Users users=arrayList.get(position);
            holder.binding.setUsers(users);

            holder.itemView.setOnClickListener(view ->{
                memberItemInterface.onMemberClick(users,position);
            });
            String role = users.getTyping();
            if (role.equals("Admin")) {
                holder.binding.memberDetail.setText("Admin");
            } else {
                holder.binding.memberDetail.setText("");
            }

        }

    }

    @Override
    public int getItemCount() {
        if(arrayList!=null){
            return  arrayList.size();
        }
        else{
            return 0;
        }
    }

    public void setArrayList(@NonNull ArrayList<Users> arrayList){
        this.arrayList=arrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MemberItemLayoutBinding binding;
        public ViewHolder(@NonNull MemberItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
