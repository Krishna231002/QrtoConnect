package com.example.demo8.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.demo8.AddContact;
import com.example.demo8.MyModels.BlockContactModel;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedContactAdapter extends RecyclerView.Adapter<BlockedContactAdapter.ViewHolder> {

    List<BlockContactModel> blockList;
    Context context;
    FirebaseUser mUser;

    public BlockedContactAdapter(List<BlockContactModel> blockList, Context context) {
        this.blockList = blockList;
        this.context = context;
        // mUser = FirebaseAuth.getInstance().getCurrentUser();
        // blockRef = FirebaseDatabase.getInstance().getReference().child("BlockedContacts");
    }

    @NonNull
    @Override
    public BlockedContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blocked_contact_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedContactAdapter.ViewHolder holder, int position) {

        BlockContactModel blockContactModel = blockList.get(position);
        String contactName = blockContactModel.getBlockFirstName();
        String contactName2 = blockContactModel.getBlockLastName();
        String contactNumber = blockContactModel.getBlockMobile();

        String fullName = contactName + " " + contactName2;

        holder.blockNumber.setText(contactNumber);
        holder.blockName.setText(fullName);
        // Load image using Glide
        Glide.with(context).load(blockContactModel.getBlockImageURL()).into(holder.blockImage);

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser = FirebaseAuth.getInstance().getCurrentUser();

                // Generate a unique filepath for the new image
                String filepath = generateUniqueFilepath(holder.getAdapterPosition());
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepath);

                // Download the image from the original URL
                Glide.with(context)
                        .asBitmap()
                        .load(blockContactModel.getBlockImageURL())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Convert the Bitmap to a byte array
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                // Upload the image to the storage with the new filepath
                                storageReference.putBytes(data)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // Get the download URL of the newly uploaded image
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        // Create a HashMap with contact details including the new image URL
                                                        String imageURL = uri.toString();
                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        hashMap.put("imageURL", imageURL);
                                                        hashMap.put("contactFirstName", blockContactModel.getBlockFirstName());
                                                        hashMap.put("contactLastName", blockContactModel.getBlockLastName());
                                                        hashMap.put("contactEmail", blockContactModel.getBlockEmail());
                                                        hashMap.put("contactMobile", blockContactModel.getBlockMobile());
                                                        hashMap.put("contactCompanyName", blockContactModel.getBlockCompanyName());
                                                        hashMap.put("contactNotes", blockContactModel.getBlockNotes());

                                                        // Add the contact to the database
                                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mUser.getUid()).child(blockContactModel.getBlockKey());
                                                        reference.setValue(hashMap)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(context, "Contact Added", Toast.LENGTH_SHORT).show();
                                                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("BlockedContacts").child(FirebaseAuth.getInstance().getUid()).child(blockContactModel.getBlockKey());
                                                                        reference.removeValue();

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(context, "Failed to add contact. Please try again later.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed to upload image. Please try again later.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                notifyDataSetChanged();
            }





        });

    }

    private String generateUniqueFilepath(int position) {
        BlockContactModel blockContactModel = blockList.get(position);
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        String contactId = blockContactModel.getBlockLastName() + "_" + UUID.randomUUID().toString();

        String filepath = "ContactPhotos/" + "Photos/" + "Contact_" + firebaseUser1.getUid() + "/" + contactId;
        return filepath;
    }

    @Override
    public int getItemCount() {
        return blockList.size();
    }

    public void filterList(List<BlockContactModel> filteredList) {
        blockList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView blockImage;
        TextView blockName, blockNumber;
        Button btnRemove;

        public ViewHolder (@NonNull View itemView) {
            super(itemView);
            blockImage = itemView.findViewById(R.id.block_profile_image);
            blockName = itemView.findViewById(R.id.blockName);
            blockNumber = itemView.findViewById(R.id.blockNumber);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
