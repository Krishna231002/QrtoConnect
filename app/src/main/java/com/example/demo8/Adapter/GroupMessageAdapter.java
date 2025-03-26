package com.example.demo8.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.demo8.BR;
import com.example.demo8.FullImageView;
import com.example.demo8.GroupMessageModel;
import com.example.demo8.databinding.GroupLeftItemLayoutBinding;
import com.example.demo8.databinding.GroupRightItemLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    private ArrayList<GroupMessageModel> messageModels;
    private Context context;
    private String myId;
    private String groupId; // Added groupId
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("Group Message");
    //private Map<String, Pair<Integer, Integer>> imageDimensionsMap = new HashMap<>(); // Store image dimensions
    RecyclerView groupMessageView;
    private boolean isImageDownloaded=false,isDownloadButtonClicked = false;

    public GroupMessageAdapter(ArrayList<GroupMessageModel> messageModels, Context context, String myId, String groupId,RecyclerView groupMessageView) {
        this.messageModels = messageModels;
        this.context = context;
        this.myId = myId;
        this.groupId = groupId; // Initialize groupId
        this.groupMessageView=groupMessageView;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding viewDataBinding = null;

        switch (viewType) {
            case 0:
                viewDataBinding = GroupRightItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
                break;
            case 1:
                viewDataBinding = GroupLeftItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
                break;
        }

        return new ViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMessageModel groupMessageModel = messageModels.get(position);

        holder.viewDataBinding.setVariable(BR.groupMessage, groupMessageModel);

        // Get the formatted time from the message model
        long timestamp = Long.parseLong(groupMessageModel.getDate());
        // Get the formatted time from the timestamp
        String formattedTime = getFormattedTime(timestamp);

        // Check if the layout is for the right or left item
        if (holder.viewDataBinding instanceof GroupRightItemLayoutBinding) {
            // Right item layout (sent by current user)
            GroupRightItemLayoutBinding rightBinding = (GroupRightItemLayoutBinding) holder.viewDataBinding;
            rightBinding.txtgroupsenderTime.setText(formattedTime); // Set the time to the appropriate TextView
            // setLongClickListener(rightBinding.getRoot(), groupMessageModel.getMessageId());
            if (groupMessageModel.getType().equals("image")) {
                //    loadImageAndSetDimensions(rightBinding.groupSendImage, groupMessageModel);
                rightBinding.txtSenderMessage.setVisibility(View.GONE);
                rightBinding.groupSendImage.setVisibility(View.VISIBLE);
                rightBinding.groupSendImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(context, FullImageView.class);
                        intent.putExtra("image",groupMessageModel.getMessage());
                        context.startActivity(intent);
                    }
                });
                Glide.with(context)
                        .load(groupMessageModel.getMessage())// Assuming the image URL is stored in the model
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .into(rightBinding.groupSendImage);
            }else {
                rightBinding.txtSenderMessage.setVisibility(View.VISIBLE);
                rightBinding.groupSendImage.setVisibility(View.GONE);
            }
        } else if (holder.viewDataBinding instanceof GroupLeftItemLayoutBinding) {
            // Left item layout (received from other user)
            GroupLeftItemLayoutBinding leftBinding = (GroupLeftItemLayoutBinding) holder.viewDataBinding;
            leftBinding.txtgroupreceiverTime.setText(formattedTime);
            leftBinding.senderName.setText(groupMessageModel.getSenderName());
            if (groupMessageModel.getType().equals("image")) {

                leftBinding.groupReceiveImage.setVisibility(View.VISIBLE);
                //      loadImageAndSetDimensions(leftBinding.groupReceiveImage, groupMessageModel);
                leftBinding.txtReceiverMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(groupMessageModel.getMessage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3))) // Apply blur transformation// Assuming the image URL is stored in the model
                        .into(leftBinding.groupReceiveImage);
                leftBinding.grdownloadButton.setVisibility(View.VISIBLE);

                DatabaseReference imageDownload = FirebaseDatabase.getInstance().getReference("Group Message").child(groupId).child(groupMessageModel.getMessageId())
                        .child("isImageDownloaded").child(FirebaseAuth.getInstance().getUid());

                imageDownload.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            isImageDownloaded =snapshot.child("imageDownloaded").getValue(boolean.class);

                            if (!((Activity) context).isDestroyed()) {
                                if (isImageDownloaded) {
                                    // If image is downloaded, hide download button and blur effect
                                    leftBinding.grdownloadButton.setVisibility(View.GONE);
                                    leftBinding.groupReceiveImage.clearColorFilter(); // Clear any previously applied color filters
                                    // Load image using Glide
                                    Glide.with(context)
                                            .load(groupMessageModel.getMessage())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(leftBinding.groupReceiveImage);

                                    leftBinding.groupReceiveImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(context, FullImageView.class);
                                            intent.putExtra("image", groupMessageModel.getMessage());
                                            context.startActivity(intent);
                                        }
                                    });
                                } else {
                                    // If image is not downloaded, show download button and apply blur effect
                                    leftBinding.grdownloadButton.setVisibility(View.VISIBLE);

                                }
                            }
                        }
                        // Toast.makeText(context, "snapshot: " +snapshot.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                leftBinding.grdownloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isDownloadButtonClicked = true;
                        if (!groupMessageModel.getImageDownloaded()) {

                            groupMessageModel.setImageDownloaded(true);

                            // Check if external storage is available and writable
                            if (isExternalStorageWritable()) {
                                // Directory name to store images
                                File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                                File directory = new File(downloadsDirectory, "QrToConnect Images");
                                // Create the directory if it doesn't exist
                                if (!directory.exists()) {
                                    directory.mkdirs();
                                }

                                // Generate a unique filename for the image
                                String fileName = "image_" + System.currentTimeMillis() + ".jpg";

                                // Check if the directory exists
                                if (directory.exists() && directory.isDirectory()) {
                                    // Download and store the image
                                    Glide.with(context)
                                            .asBitmap()
                                            .load(groupMessageModel.getMessage())
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    // Save the bitmap to the external storage
                                                    saveBitmapToExternalStorage(resource, directory, fileName);

                                                    // Update the UI to indicate that the image has been downloaded
                                                    leftBinding.grdownloadButton.setVisibility(View.GONE);
                                                    leftBinding.groupReceiveImage.clearColorFilter();
                                                }
                                            });
                                } else {
                                    // Directory doesn't exist or is not a directory
                                    Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // External storage is not available or not writable
                                Toast.makeText(context, "External storage is not available", Toast.LENGTH_SHORT).show();
                            }
                            database.child(groupId).child(groupMessageModel.getMessageId()).child("isImageDownloaded").child(FirebaseAuth.getInstance().getUid()).setValue(groupMessageModel);
                            // Show progress bar
                            leftBinding.grprogressBar.setVisibility(View.VISIBLE);
                            leftBinding.grdownloadButton.setVisibility(View.GONE);

                            // Simulate image downloading
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Hide progress bar once download is completed
                                    leftBinding.grprogressBar.setVisibility(View.GONE);
                                    leftBinding.grdownloadButton.setVisibility(View.GONE);

                                }
                            }, 3000); // Simulating 3 seconds download time
                        }
                    }
                });

            }else {
                leftBinding.txtReceiverMessage.setVisibility(View.VISIBLE);
                leftBinding.groupReceiveImage.setVisibility(View.GONE);
                leftBinding.grdownloadButton.setVisibility(View.GONE);
            }// Set the time to the appropriate TextView
            // Fetch sender's name from Firebase and set it
        }

    }
    /*    private void loadImageAndSetDimensions(ImageView imageView, GroupMessageModel groupMessageModel) {

                // Load the image from Firebase Storage or other sources using Glide
                Glide.with(context)
                        .asBitmap()
                        .load(groupMessageModel.getMessage())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Resize the bitmap to match desired dimensions
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                // Store the dimensions in the map
                                imageDimensionsMap.put(groupMessageModel.getMessage(), new Pair<>(width, height));
                                // Set the bitmap to the ImageView
                                imageView.setImageBitmap(resource);
                            }
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Implement if needed
                            }
                        });

        }
*/

    private String getFormattedTime(long timestamp) {
        // Convert timestamp to human-readable format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void saveBitmapToExternalStorage(Bitmap bitmap, File directory, String fileName) {
        File file = new File(directory, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Notify the system to add the image to the media database
            scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

            // Image saved successfully, update messageModel or UI accordingly
        } catch (IOException e) {
            e.printStackTrace();
            // Error occurred while saving the image
        }
    }

    private void scanFile(Context context, String[] path, String[] mimeType, MediaScannerConnection.OnScanCompletedListener callback) {
        MediaScannerConnection.scanFile(context, path, mimeType, callback);
    }



        /*private void setLongClickListener(View view, String messageId) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                   // showDeleteConfirmationDialog(messageId);
                    return true; // Consume the long click event
                }
            });
        }*/

      /*  private void showDeleteConfirmationDialog(String messageId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Message");
            builder.setMessage("Are you sure you want to delete this message?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete the message from Firebase
                    database.child(groupId).child(messageId).removeValue();
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }*/

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public void scrollToBottom() {

        if (getItemCount() > 0 && !isDownloadButtonClicked) {
            groupMessageView.post(new Runnable() {
                @Override
                public void run() {
                    // Scroll to the last item in the RecyclerView
                    groupMessageView.scrollToPosition(getItemCount() - 1);
                }
            });
        }
    }

    /*public void scrollToBottom() {
        if (getItemCount() > 0 && !isDownloadButtonClicked) {
            groupMessageView.scrollToPosition(getItemCount() - 1);
        }
    }

     */


    @Override
    public int getItemViewType(int position) {
        GroupMessageModel messageModel = messageModels.get(position);
        return myId.equals(messageModel.getSenderId()) ? 0 : 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding viewDataBinding;

        public ViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;
        }
    }
}
