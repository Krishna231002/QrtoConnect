package com.example.demo8.Adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.Interface.ContactItemInterface;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.Users;
import com.example.demo8.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupContactAdapter extends RecyclerView.Adapter<GroupContactAdapter.ViewHolder> {

    List<Users> arrayList;
    Context context;
    ContactItemInterface contactItemInterface;


    public GroupContactAdapter(Context context, ContactItemInterface contactItemInterface, List<Users> arrayList) {
        this.context = context;
        this.contactItemInterface = contactItemInterface;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public GroupContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_items,parent,false);

        return new GroupContactAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupContactAdapter.ViewHolder holder, int position) {

        Users users = arrayList.get(position);
        if(users!=null){

            String image = users.getImageURL();
            String firstname = users.getFirstName();
            String lastname = users.getLastName();
            String userId = users.getId();
            String name = firstname + " " + lastname;

            holder.GItemName.setText(name);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call the interface method with the intent strings
                    contactItemInterface.onContactClick(users, holder.getAdapterPosition(), false);
                }
            });
            if (!TextUtils.isEmpty(image)) {
                // Load image using Glide
                Glide.with(context)
                        .load(Uri.parse(image))
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .into(holder.GItemImage);
            } else {
                // If image URL is empty, set a default placeholder
                holder.GItemImage.setImageResource(R.drawable.baseline_account_circle_24);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (arrayList!=null){
            return arrayList.size();

        }else
            return 0;
    }

    public void filterList(List<Users> filteredList) {
        arrayList = filteredList;
        notifyDataSetChanged();
    }

    public void setArrayList(ArrayList<Users> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView GItemImage;
        TextView GItemName;
        LinearLayout groupLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            GItemImage = itemView.findViewById(R.id.profile_image);
            GItemName = itemView.findViewById(R.id.userNameList);
            groupLayout = itemView.findViewById(R.id.chatLayout);


        }
    }
}
