package com.example.demo8.Adapter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.demo8.FullImageView;
import com.example.demo8.MyModels.MessageModel;
import com.example.demo8.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.os.Handler;
import android.widget.Toast;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MessageAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;
    String recId;
    private FirebaseAuth firebaseAuth;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    RecyclerView recyclerView;
    boolean isImageDownloaded=false,isDownloadButtonClicked = false;
    public MessageAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    public MessageAdapter(ArrayList<MessageModel> messageModels, Context context, String recId,RecyclerView recyclerView) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
        this.recyclerView =recyclerView;
        firebaseAuth = FirebaseAuth.getInstance();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position) {
        MessageModel messageModel = messageModels.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message ?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()).child(senderRoom)
                                        .child(messageModel.getMessageId())
                                        .removeValue();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                return false;
            }
        });



        if (holder.getClass() == SenderViewHolder.class){
            ((SenderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
            ((SenderViewHolder) holder).senderTime.setText(getFormattedTime(messageModel.getTimestamp()));
        } else {
            ((ReceiverViewHolder) holder).receiverMsg.setText(messageModel.getMessage());
            ((ReceiverViewHolder) holder).receiverTime.setText(getFormattedTime(messageModel.getTimestamp()));
        }

        if (messageModel.getType().equals("text")) {
            // If the message type is text, hide the ImageView and show the TextView
            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).senderImg.setVisibility(View.GONE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
            } else if (holder instanceof ReceiverViewHolder) {
                ((ReceiverViewHolder) holder).receiverImg.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.VISIBLE);

            }
        } else {
            // If the message type is not text, show the ImageView and hide the TextView
            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).senderImg.setVisibility(View.VISIBLE);

                ((SenderViewHolder) holder).senderImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(context, FullImageView.class);
                        intent.putExtra("image",messageModel.getMessage());
                        context.startActivity(intent);
                    }
                });

                ((SenderViewHolder) holder).senderImg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Are you sure you want to delete this message ?")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                        database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()).child(senderRoom)
                                                .child(messageModel.getMessageId())
                                                .removeValue();
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                        return false;
                    }
                });

                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messageModel.getMessage())// Assuming the image URL is stored in the model
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .into(((SenderViewHolder) holder).senderImg);

            } else if (holder instanceof ReceiverViewHolder) {
                ((ReceiverViewHolder) holder).receiverImg.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiverImg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Are you sure you want to delete this message ?")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                        database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()).child(senderRoom)
                                                .child(messageModel.getMessageId())
                                                .removeValue();
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                        return false;
                    }
                });
                Glide.with(context)
                        .load(messageModel.getMessage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation( 25,3))) // Apply blur transformation// Assuming the image URL is stored in the model
                        .into(((ReceiverViewHolder) holder).receiverImg);

                DatabaseReference imageDownload = FirebaseDatabase.getInstance().getReference("Chats")
                        .child(FirebaseAuth.getInstance().getUid()).child("isImageDownloaded").child(messageModel.getMessageId());

                imageDownload.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            isImageDownloaded = snapshot.child("imageDownloaded").getValue(boolean.class);

                            if (!((Activity) context).isDestroyed()) {
                                if (isImageDownloaded) {
                                    ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.GONE);
                                    ((ReceiverViewHolder) holder).receiverImg.clearColorFilter();

                                    Glide.with(context)
                                            .load(messageModel.getMessage())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)// Cache both original & resized image
                                            .into(((ReceiverViewHolder) holder).receiverImg);

                                    ((ReceiverViewHolder) holder).receiverImg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(context, FullImageView.class);
                                            intent.putExtra("image", messageModel.getMessage());
                                            context.startActivity(intent);
                                        }
                                    });

                                } else {
                                    ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.VISIBLE);

                                }

                            }
                        }
                        // Toast.makeText(context, "snapshot: " +snapshot.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                ((ReceiverViewHolder) holder).downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        isDownloadButtonClicked = true;
                        if (!messageModel.getImageDownloaded()) {

                            messageModel.setImageDownloaded(true);

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
                                            .load(messageModel.getMessage())
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    // Save the bitmap to the external storage
                                                    saveBitmapToExternalStorage(resource, directory, fileName);

                                                    // Update the UI to indicate that the image has been downloaded
                                                    ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.GONE);
                                                    ((ReceiverViewHolder) holder).receiverImg.clearColorFilter();
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

                            DatabaseReference imageDownload = FirebaseDatabase.getInstance().getReference("Chats")
                                    .child(FirebaseAuth.getInstance().getUid()).child("isImageDownloaded").child(messageModel.getMessageId());
                            imageDownload.setValue(messageModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });

                            // Show progress bar
                            ((ReceiverViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                            ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.GONE);

                            // Simulate image downloading
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Hide progress bar once download is completed
                                    ((ReceiverViewHolder) holder).progressBar.setVisibility(View.GONE);
                                    ((ReceiverViewHolder) holder).downloadButton.setVisibility(View.GONE);

                                }
                            }, 3000); // Simulating 3 seconds download time
                        }
                    }
                });


            }

        }




     /*  if (position == getItemCount() - 1) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.smoothScrollToPosition(position);
                }
            });
        }else {
           recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
               @Override
               public void onGlobalLayout() {
                   // Scroll to the last item in the RecyclerView
                   recyclerView.scrollToPosition(getItemCount() - 1);
                   // Remove the listener to avoid multiple calls
                   recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
           });
        }*/
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

    public void scrollToBottom() {
        if (getItemCount() > 0 && !isDownloadButtonClicked) {
            recyclerView.scrollToPosition(getItemCount() - 1);
        }
    }



    // Method to get formatted time
    private String getFormattedTime(Long timestamp) {
        // Convert timestamp to human-readable format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        TextView receiverMsg , receiverTime;
        ImageView receiverImg;
        ProgressBar progressBar;
        ImageButton downloadButton;
        boolean isImageDownloaded = false;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverImg=itemView.findViewById(R.id.receiverImage);
            progressBar = itemView.findViewById(R.id.progressBar);
            downloadButton = itemView.findViewById(R.id.downloadButton);


        }

        // Method to get formatted time
        private String getFormattedTime(long timestamp) {
            // Convert timestamp to human-readable format
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView senderMsg , senderTime;
        ImageView senderImg;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderImg=itemView.findViewById(R.id.senderImage);

        }
    }
}