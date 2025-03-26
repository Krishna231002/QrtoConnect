package com.example.demo8.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.Interface.ContactItemInterface;
import com.example.demo8.Users;
import com.example.demo8.databinding.SelectedContactItemLayoutBinding;

import java.util.ArrayList;

public class SelectedContactAdapter extends RecyclerView.Adapter<SelectedContactAdapter.ViewHolder> {

    ArrayList<Users> arrayList;
    ContactItemInterface contactItemInterface;
    Context context;

    public SelectedContactAdapter(ContactItemInterface contactItemInterface, Context context) {
        this.contactItemInterface = contactItemInterface;
        this.context = context;
    }

    public SelectedContactAdapter(ArrayList<Users> arrayList, ContactItemInterface contactItemInterface) {
        this.arrayList = arrayList;
        this.contactItemInterface = contactItemInterface;
    }

    public SelectedContactAdapter(ArrayList<Users> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectedContactItemLayoutBinding binding = SelectedContactItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (arrayList!=null){
            Users users = arrayList.get(position);

            Glide.with(context).load(users.getImageURL()).into(holder.binding.imgSelectedContact);
            holder.itemView.setOnClickListener(view ->{

                contactItemInterface.onContactClick(users,position,false);
            });
            holder.binding.btnDeselect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactItemInterface.deselectContact(users);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if(arrayList!= null){
            return  arrayList.size();
        }else{
            return 0;
        }
    }

    public void setArrayList(ArrayList<Users> arrayList){
          this.arrayList = arrayList;
          notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        SelectedContactItemLayoutBinding binding;
        public ViewHolder(@NonNull SelectedContactItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


}
