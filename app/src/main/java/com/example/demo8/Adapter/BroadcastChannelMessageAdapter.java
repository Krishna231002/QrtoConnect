package com.example.demo8.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
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
import com.example.demo8.BroadChannelMessageModel;
import com.example.demo8.FullImageView;
import com.example.demo8.databinding.BroadcastLeftItemLayoutBinding;
import com.example.demo8.databinding.BroadcastRightItemLayoutBinding;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class BroadcastChannelMessageAdapter extends RecyclerView.Adapter<BroadcastChannelMessageAdapter.ViewHolder> {

    private ArrayList<BroadChannelMessageModel> messageModels;
    private Context context;
    private String myId;
    private String broadcastChannelId; // Added groupId
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("Broadcast Message");
    private Map<String, Pair<Integer, Integer>> imageDimensionsMap = new HashMap<>(); // Store image dimensions
    RecyclerView broadcastMessageView;
    boolean isImageDownloaded=false,isDownloadButtonClicked = false;

    private String adminId;

    public BroadcastChannelMessageAdapter(ArrayList<BroadChannelMessageModel> messageModels, Context context, String myId, String broadcastChannelId,RecyclerView broadcastMessageView) {
        this.messageModels = messageModels;
        this.context = context;
        this.myId = myId;
        this.broadcastChannelId =broadcastChannelId; // Initialize broadcastId
        this.broadcastMessageView = broadcastMessageView;

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Broadcast Channel").child(broadcastChannelId);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    adminId = dataSnapshot.child("adminId").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding viewDataBinding = null;

        switch (viewType) {
            case 0:
                viewDataBinding = BroadcastRightItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
                break;
            case 1:
                viewDataBinding = BroadcastLeftItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
                break;
        }

        return new BroadcastChannelMessageAdapter.ViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastChannelMessageAdapter.ViewHolder holder, int position) {
        BroadChannelMessageModel broadChannelMessageModel = messageModels.get(position);

        holder.viewDataBinding.setVariable(BR.groupMessage, broadChannelMessageModel);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setLongClickListener(holder.itemView, broadChannelMessageModel.getMessageId(), currentUserId);

        // Get the formatted time from the message model
        long timestamp = Long.parseLong(broadChannelMessageModel.getDate());
        // Get the formatted time from the timestamp
        String formattedTime = getFormattedTime(timestamp);

        // Check if the layout is for the right or left item
        if (holder.viewDataBinding instanceof BroadcastRightItemLayoutBinding) {
            // Right item layout (sent by current user)
            BroadcastRightItemLayoutBinding rightBinding = (BroadcastRightItemLayoutBinding) holder.viewDataBinding;
            rightBinding.txtBroadcastsenderTime.setText(formattedTime); // Set the time to the appropriate TextView
            // setLongClickListener(rightBinding.getRoot(), groupMessageModel.getMessageId());
            if (broadChannelMessageModel.getType().equals("image")) {
                //    loadImageAndSetDimensions(rightBinding.groupSendImage, groupMessageModel);
                rightBinding.txtBroadcastSenderMessage.setVisibility(View.GONE);
                rightBinding.broadcastSendImage.setVisibility(View.VISIBLE);
                rightBinding.broadcastSendImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(context, FullImageView.class);
                        intent.putExtra("image",broadChannelMessageModel.getMessage());
                        context.startActivity(intent);
                    }
                });
                Glide.with(context)
                        .load(broadChannelMessageModel.getMessage())// Assuming the image URL is stored in the model
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .into(rightBinding.broadcastSendImage);
                rightBinding.txtBroadcastSenderMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (isCurrentUserAdmin(currentUserId)) {
                            showDeleteConfirmationDialog(broadChannelMessageModel.getMessageId());
                        }
                        return false;
                    }
                });

                rightBinding.broadcastSendImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (isCurrentUserAdmin(currentUserId)) {
                            showDeleteConfirmationDialog(broadChannelMessageModel.getMessageId());
                        }
                        notifyDataSetChanged();
                        return false;
                    }

                });



            }else {
                rightBinding.txtBroadcastSenderMessage.setVisibility(View.VISIBLE);
                rightBinding.txtBroadcastSenderMessage.setText(broadChannelMessageModel.getMessage());
                rightBinding.broadcastSendImage.setVisibility(View.GONE);
            }
        } else if (holder.viewDataBinding instanceof BroadcastLeftItemLayoutBinding) {
            // Left item layout (received from other user)
            BroadcastLeftItemLayoutBinding leftBinding = (BroadcastLeftItemLayoutBinding) holder.viewDataBinding;
            leftBinding.txtbroadcastreceiverTime.setText(formattedTime);
            leftBinding.txtBroadcastReceiverMessage.setText(broadChannelMessageModel.getName());

            if (broadChannelMessageModel.getType().equals("image")) {
                leftBinding.broadcastReceiveImage.setVisibility(View.VISIBLE);

                leftBinding.broadcastSenderName.setText(broadChannelMessageModel.getName());
                //      loadImageAndSetDimensions(leftBinding.groupReceiveImage, groupMessageModel);
                leftBinding.txtBroadcastReceiverMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(broadChannelMessageModel.getMessage())// Assuming the image URL is stored in the model
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3))) // Apply blur transformation// Assuming the image URL is stored in the model
                        .into(leftBinding.broadcastReceiveImage);

                leftBinding.brdownloadButton.setVisibility(View.VISIBLE);

                DatabaseReference imageDownload = FirebaseDatabase.getInstance().getReference("Broadcast Message").child(broadcastChannelId)
                        .child(broadChannelMessageModel.getMessageId())
                        .child("isImageDownloaded").child(FirebaseAuth.getInstance().getUid());

                imageDownload.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if the activity is still valid
                        if (!((Activity) context).isDestroyed()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                isImageDownloaded =snapshot.child("imageDownloaded").getValue(boolean.class);

                                if (isImageDownloaded) {
                                    // If image is downloaded, hide download button and blur effect
                                    leftBinding.brdownloadButton.setVisibility(View.GONE);
                                    leftBinding.broadcastReceiveImage.clearColorFilter(); // Clear any previously applied color filters
                                    // Load image using Glide
                                    Glide.with(context)
                                            .load(broadChannelMessageModel.getMessage())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(leftBinding.broadcastReceiveImage);

                                    leftBinding.broadcastReceiveImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent= new Intent(context, FullImageView.class);
                                            intent.putExtra("image",broadChannelMessageModel.getMessage());
                                            context.startActivity(intent);
                                        }
                                    });
                                } else {
                                    // If image is not downloaded, show download button and apply blur effect
                                    leftBinding.brdownloadButton.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });

                leftBinding.brdownloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isDownloadButtonClicked = true;
                        if (!broadChannelMessageModel.getImageDownloaded()) {

                            broadChannelMessageModel.setImageDownloaded(true);

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
                                            .load(broadChannelMessageModel.getMessage())
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    // Save the bitmap to the external storage
                                                    saveBitmapToExternalStorage(resource, directory, fileName);

                                                    // Update the UI to indicate that the image has been downloaded
                                                    leftBinding.brdownloadButton.setVisibility(View.GONE);
                                                    leftBinding.broadcastReceiveImage.clearColorFilter();
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

                            database.child(broadcastChannelId)
                                    .child(broadChannelMessageModel.getMessageId())
                                    .child("isImageDownloaded")
                                    .child(FirebaseAuth.getInstance().getUid()).setValue(broadChannelMessageModel);
                            // Show progress bar
                            leftBinding.brprogressBar.setVisibility(View.VISIBLE);
                            leftBinding.brdownloadButton.setVisibility(View.GONE);

                            // Simulate image downloading
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Hide progress bar once download is completed
                                    leftBinding.brprogressBar.setVisibility(View.GONE);
                                    leftBinding.brdownloadButton.setVisibility(View.GONE);

                                    // Load image into ImageView
                                }
                            }, 3000); // Simulating 3 seconds download time
                        }
                    }
                });

            }else {
                leftBinding.txtBroadcastReceiverMessage.setVisibility(View.VISIBLE);
                leftBinding.broadcastReceiveImage.setVisibility(View.GONE);
                leftBinding.brdownloadButton.setVisibility(View.GONE);
                leftBinding.broadcastSenderName.setText(broadChannelMessageModel.getName());
                leftBinding.txtBroadcastReceiverMessage.setText(broadChannelMessageModel.getMessage());
            }// Set the time to the appropriate TextView
            // Fetch sender's name from Firebase and set it
        }

    }

    private void setLongClickListener(View view, String messageId, String currentUserId) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isCurrentUserAdmin(currentUserId)) {
                    showDeleteConfirmationDialog(messageId);
                }
                return true; // Consume the long click event
            }
        });
    }

    private void showDeleteConfirmationDialog(String messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Message");
        builder.setMessage("Are you sure you want to delete this message?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the message from Firebase
                database.child(broadcastChannelId).child(messageId).removeValue();

                int position = findPositionByMessageId(messageId);
                if (position != -1) {
                    messageModels.remove(position);
                    notifyItemRemoved(position);
                }
            }
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }

    private int findPositionByMessageId(String messageId) {
        for (int i = 0; i < messageModels.size(); i++) {
            if (messageModels.get(i).getMessageId().equals(messageId)) {
                return i;
            }
        }
        return -1;
    }
    private boolean isCurrentUserAdmin(String currentUserId) {
        return adminId != null && adminId.equals(currentUserId);
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

    private String getFormattedTime(long timestamp) {
        // Convert timestamp to human-readable format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
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
            broadcastMessageView.post(new Runnable() {
                @Override
                public void run() {
                    // Scroll to the last item in the RecyclerView
                    broadcastMessageView.scrollToPosition(getItemCount() - 1);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        BroadChannelMessageModel messageModel = messageModels.get(position);
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
