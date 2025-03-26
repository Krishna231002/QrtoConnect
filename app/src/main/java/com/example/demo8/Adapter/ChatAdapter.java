package com.example.demo8.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.ChatDetailActivity;
import com.example.demo8.Users;
import com.example.demo8.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    Context context;
    List<Users> chatList;


    FirebaseAuth auth = FirebaseAuth.getInstance();

    FirebaseUser firebaseUser = auth.getCurrentUser();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Chats");


    public ChatAdapter(Context context, List<Users> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_items,parent,false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position) {

        final Users users = chatList.get(position);
        final String senderId = auth.getUid();


        String image = users.getImageURL();
        String firstname = users.getFirstName();
        String lastname = users.getLastName();
        String lastMessage = users.getLastMessage();
        String userId = users.getId();
        String senderRoom = senderId + userId;
        String receiverRoom = userId + senderId;
        String name = firstname + " " + lastname;

        // Append "(You)" if the user is the current authenticated user
        if (userId.equals(firebaseUser.getUid())) {
            name += " (You)";
        }
        //set data in view
      //  Glide.with(context).load(users.getImageURL()).into(holder.CItemImage);

        holder.CItemName.setText(name);



       /* DatabaseReference messagesRef = database.child(firebaseUser.getUid()).child(senderRoom);
        messagesRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot message : snapshot.getChildren()) {
                    String lastMessage = message.child("message").getValue(String.class);
                    users.setLastMessage(lastMessage);
                    holder.CItemLastMsg.setText(lastMessage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Not able to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference messagesRef1 = database.child(firebaseUser.getUid()).child(receiverRoom);
        messagesRef1.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot message : snapshot.getChildren()) {
                    String lastMessage = message.child("message").getValue(String.class);
                    users.setLastMessage(lastMessage);
                    holder.CItemLastMsg.setText(lastMessage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Not able to fetch data", Toast.LENGTH_SHORT).show();
            }
        });*/

        String finalName = name;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId",users.getId());
                intent.putExtra("profilePic",users.getImageURL());
                intent.putExtra("userName", finalName);
                intent.putExtra("userMobile", users.getMobile());

                context.startActivity(intent);
            }
        });

      //  holder.CItemLastMsg.setText(lastMessage);

        if (!TextUtils.isEmpty(image)) {
            // Load image using Glide
            Glide.with(context)
                    .load(Uri.parse(image))
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(holder.CItemImage);
        } else {
            // If image URL is empty, set a default placeholder
            holder.CItemImage.setImageResource(R.drawable.baseline_account_circle_24);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView CItemImage;
        TextView CItemName,CItemLastMsg;
        LinearLayout chatLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            CItemImage = itemView.findViewById(R.id.profile_image);
            CItemName = itemView.findViewById(R.id.userNameList);
            CItemLastMsg = itemView.findViewById(R.id.lastMessage);
            chatLayout = itemView.findViewById(R.id.chatLayout);
        }
    }

}
