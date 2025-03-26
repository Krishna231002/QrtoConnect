    package com.example.demo8.Adapter;


    import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

    import android.app.AlertDialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.graphics.drawable.ColorDrawable;
    import android.graphics.drawable.Drawable;
    import android.net.Uri;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Filter;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.bumptech.glide.load.DataSource;
    import com.bumptech.glide.load.engine.GlideException;
    import com.bumptech.glide.request.RequestListener;
    import com.bumptech.glide.request.target.Target;
    import com.example.demo8.ChatDetailActivity;
    import com.example.demo8.FullImageView;
    import com.example.demo8.GroupMessageActivity;
    import com.example.demo8.GroupModels;
    import com.example.demo8.MyModels.ChatUnreadMessageModel;
    import com.example.demo8.R;
    import com.example.demo8.Users;
    import com.example.demo8.databinding.GroupItemBinding;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.List;

    import de.hdodenhof.circleimageview.CircleImageView;

    public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_GROUP_CHAT = 0;
        private static final int VIEW_TYPE_INDIVIDUAL_CHAT = 1;

        Context context;
        List<Object> combinedList;

        List<Object> groupModels;

        public CombinedAdapter(Context context, List<Object> combinedList) {
            this.context = context;
            this.combinedList = combinedList;

            this.groupModels = new ArrayList<Object>();
        }

        @Override
        public int getItemViewType(int position) {
            if (combinedList.get(position) instanceof GroupModels) {
                return VIEW_TYPE_GROUP_CHAT;
            } else if (combinedList.get(position) instanceof Users) {
                return VIEW_TYPE_INDIVIDUAL_CHAT;
            }
            return -1;
        }



        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);

            if (viewType == VIEW_TYPE_GROUP_CHAT) {
                // Inflate layout for group chat item
                View groupItemChatView = inflater.inflate(R.layout.group_item, parent, false);
                return new GroupChatViewHolder(groupItemChatView);
            } else if (viewType == VIEW_TYPE_INDIVIDUAL_CHAT) {
                // Inflate layout for individual chat item
                View individualChatView = inflater.inflate(R.layout.chat_items, parent, false);

                return new IndividualChatViewHolder(individualChatView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            if (viewType == VIEW_TYPE_GROUP_CHAT) {
                // Bind group chat data
                GroupModels groupChat = (GroupModels) combinedList.get(position);

                ((GroupChatViewHolder) holder).bind(groupChat);
            } else if (viewType == VIEW_TYPE_INDIVIDUAL_CHAT) {
                // Bind individual chat data
                Users individualChat = (Users) combinedList.get(position);

                //  holder.CItemLastMsg.setText(lastMessage);

                ((IndividualChatViewHolder) holder).bind(individualChat);
            }
        }

        @Override
        public int getItemCount() {
            return combinedList.size();
        }

        public void setGroupModels(@NonNull List<Object> groupModels) {
            if (this.groupModels == null) {
                this.groupModels = new ArrayList<Object>(); // Initialize the list if null
            } else {
                this.groupModels.clear(); // Clear the list if not null
            }
            this.groupModels.addAll(groupModels);
            notifyDataSetChanged();
        }

        public void filterList(List<Object> filteredList) {
            combinedList = filteredList;
            notifyDataSetChanged();
        }






        // ViewHolder for group chat item
        // ViewHolder for group chat item
        private static class GroupChatViewHolder extends RecyclerView.ViewHolder {
            Context context;

            private TextView groupNameTextView,groupUnreadBadge;
            CircleImageView groupProfilePic;
            GroupItemBinding binding;
            private LayoutInflater inflater;

            public GroupChatViewHolder(@NonNull View itemView) {
                super(itemView);
                context = itemView.getContext(); // Initialize context here
                groupNameTextView = itemView.findViewById(R.id.groupNameList);
                groupUnreadBadge = itemView.findViewById(R.id.grunreadBadge);
                groupProfilePic = itemView.findViewById(R.id.gr_profile_image);
                binding = GroupItemBinding.bind(itemView);

                inflater = LayoutInflater.from(context);

            }

            public void bind(GroupModels groupChat) {
                groupNameTextView.setText(groupChat.getName());

                groupProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View dialogView = inflater.inflate(R.layout.dialog_image_view,null);

                        ImageView back = dialogView.findViewById(R.id.backImage);
                        ImageView imageView = dialogView.findViewById(R.id.profileImage);
                        TextView userName = dialogView.findViewById(R.id.txtUserName);
                        Glide.with(context).load(groupChat.getImage()).into(imageView);
                        userName.setText(groupChat.getName());

                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent= new Intent(context, FullImageView.class);
                                intent.putExtra("image",groupChat.getImage());
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

            /*    int unreadCount = groupChat.getUnreadMessage();
                if (unreadCount > 0) {
                    // Display unread message count in your UI (e.g., set a TextView)
                    // textViewUnreadCount.setText(String.valueOf(unreadCount));
                    groupUnreadBadge.setText(String.valueOf(unreadCount));
                    // Make sure to set visibility accordingly
                    groupUnreadBadge.setVisibility(View.VISIBLE);
                } else {
                    groupUnreadBadge.setVisibility(View.GONE);
                    // textViewUnreadCount.setVisibility(View.GONE);
                }*/
                //Glide.with(context).load(groupChat.getImage()).into(groupProfilePic);

                Glide.with(context)
                        .load(groupChat.getImage())
                        .placeholder(R.drawable.account_circle_icon)
                        .error(R.drawable.account_11_selected)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("GlideError", "Image loading failed", e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(groupProfilePic);

                binding.setGroupModel(groupChat);

                itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(context, GroupMessageActivity.class);
                    intent.putExtra("groupIdToMessage", groupChat.getId());
                    intent.putExtra("groupNameToMessage", groupChat.getName());
                    intent.putExtra("groupImageToMessage", groupChat.getImage());
                    intent.putExtra("groupModel", groupChat);
                    context.startActivity(intent);
                });
            }

        }




        // ViewHolder for individual chat item
        // ViewHolder for individual chat item
        private static class IndividualChatViewHolder extends RecyclerView.ViewHolder {

            private TextView userNameTextView, unreadBage,userLastMessage;
            CircleImageView circleImageView;
            Context context;
            private Users user;
            private LayoutInflater inflater;

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = auth.getCurrentUser();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Chats");

            public IndividualChatViewHolder(@NonNull View itemView) {
                super(itemView);
                context = itemView.getContext(); // Initialize context here
                userNameTextView = itemView.findViewById(R.id.userNameList);
                unreadBage = itemView.findViewById(R.id.unreadBadge);
                circleImageView = itemView.findViewById(R.id.profile_image);
                userLastMessage=itemView.findViewById(R.id.lastMessage);
                inflater = LayoutInflater.from(context);
            }

            public void bind(Users individualChat) {
                // Store the Users object
                user = individualChat;
                String name = individualChat.getFullname();

                if (user.getId().equals(firebaseUser.getUid())) {
                    name += " (You)";
                }

                // Bind user data to the layout
                userNameTextView.setText(name);
                String image = individualChat.getImageURL();


                int unreadCount = individualChat.getUnreadMessage();
                if (unreadCount > 0) {
                    unreadBage.setText(String.valueOf(unreadCount));
                    unreadBage.setVisibility(View.VISIBLE);
                } else {
                    unreadBage.setVisibility(View.GONE);
                }

                String finalName = name;
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View dialogView = inflater.inflate(R.layout.dialog_image_view,null);

                        ImageView back = dialogView.findViewById(R.id.backImage);
                        ImageView imageView = dialogView.findViewById(R.id.profileImage);
                        TextView userName = dialogView.findViewById(R.id.txtUserName);
                        Glide.with(context).load(image).into(imageView);
                        userName.setText(finalName);

                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent= new Intent(context, FullImageView.class);
                                intent.putExtra("image",image);
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

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid());

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("You Want To Pin This Chat.?").setIcon(R.drawable.baseline_pin_24);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                reference.orderByChild("contactMobile").equalTo(individualChat.getMobile()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){

                                            String contactKey = ds.getKey();

                                            String chatPin = "pin";

                                            Toast.makeText(context, contactKey, Toast.LENGTH_SHORT).show();
                                            DatabaseReference contactReference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid()).child(contactKey);

                                            contactReference.child("Pin").setValue(chatPin).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    Toast.makeText(context, "Chat Successfully Pined", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        // Create and show the AlertDialog
                        builder.show();

                        return true;

                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ChatDetailActivity.class);
                        intent.putExtra("userId", individualChat.getId());
                        intent.putExtra("profilePic", individualChat.getImageURL());
                        intent.putExtra("userName", individualChat.getFullname());
                        intent.putExtra("userMobile", individualChat.getMobile());
                        intent.putExtra("fcmToken", individualChat.getToken());
                        String receiverId = individualChat.getId();

                        intent.putExtra("userMobile", individualChat.getMobile());
                       // Toast.makeText(context, individualChat.getMobile(), Toast.LENGTH_SHORT).show();

                        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference("Chats")
                                .child(FirebaseAuth.getInstance().getUid()).child("UnreadMessages");
                        unreadRef.child("unreadMessageCount").setValue(0)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Unread count reset to zero for receiver: " + receiverId);
                                        } else {
                                            Log.e(TAG, "Error resetting unread count: " + task.getException().getMessage());
                                        }
                                    }
                                });
                       /*unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    ChatUnreadMessageModel unreadMessageModel = snapshot.getValue(ChatUnreadMessageModel.class);
                                    if (unreadMessageModel != null) {
                                        // Reset unread count to 0
                                        unreadMessageModel.senderId = receiverId;
                                        unreadMessageModel.setUnreadMessageCount(0);

                                        unreadRef.setValue(unreadMessageModel)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Unread count updated successfully");
                                                        } else {
                                                            Log.e(TAG, "Error updating unread count: " + task.getException().getMessage());
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle onCancelled
                            }
                        });*/

                        context.startActivity(intent);
                    }
                });


                // Bind user's last message
                bindLastMessage();

                // Load user image using Glide
                loadImage(image);
            }

            private void bindLastMessage() {
                final String senderId = auth.getUid();
                final String senderRoom = senderId + user.getId();
                final String receiverRoom = user.getId() + senderId;

                DatabaseReference messagesRef = database.child(firebaseUser.getUid()).child(senderRoom);
                messagesRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot message : snapshot.getChildren()) {
                            String messageType = message.child("type").getValue(String.class);
                            String lastMessage;
                            if ("image".equals(messageType)) {
                                lastMessage = "Photo";
                            } else {
                                lastMessage = message.child("message").getValue(String.class);
                            }
                            user.setLastMessage(lastMessage);
                            userLastMessage.setText(lastMessage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Not able to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            // Method to load user image using Glide
            private void loadImage(String image) {
                if (!TextUtils.isEmpty(image)) {
                    // Load image using Glide
                    /*Glide.with(context)
                            .load(Uri.parse(image))
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(circleImageView);*/

                    Glide.with(context)
                            .load(Uri.parse(image))
                            .placeholder(R.drawable.avatar3)
                            .error(R.drawable.avatar1)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Image loading failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(circleImageView);

                } else {
                    // If image URL is empty, set a default placeholder
                    circleImageView.setImageResource(R.drawable.baseline_account_circle_24);
                }
            }
        }

    }