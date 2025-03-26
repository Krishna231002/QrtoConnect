package com.example.demo8.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.AddContact;
import com.example.demo8.ContactDetails;
import com.example.demo8.DbHelper;
import com.example.demo8.Fragment.ContactsFragment;
import com.example.demo8.ModelContact;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.example.demo8.UpdateContact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    Context context;
    List<Contact> favoriteList;
    FirebaseDatabase database;
    DatabaseReference favoriteContactsRef;

    public FavoriteAdapter(Context context, List<Contact> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
        database = FirebaseDatabase.getInstance();
        favoriteContactsRef = database.getReference("Favorite Contacts");

    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_items,viewGroup,false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        //get data
        final Contact contact = favoriteList.get(position);

        // final Contact contact = filteredContactList.get(position);
        String image = contact.getImageURL();
        String firstName = contact.getContactFirstName();
        String lastName = contact.getContactLastName();

        //set data in view
        Glide.with(context).load(contact.getImageURL()).into(holder.CItemImage);
        holder.CItemName.setText(firstName + " " + lastName);

        // Check if the image URL is not empty
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

        //call button..
        holder.CItemCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ContactDetails.class);
                intent.putExtra("Image",favoriteList.get(holder.getAdapterPosition()).getImageURL());
                intent.putExtra("FirstName",favoriteList.get(holder.getAdapterPosition()).getContactFirstName());
                intent.putExtra("LastName",favoriteList.get(holder.getAdapterPosition()).getContactLastName());
                intent.putExtra("Mobile",favoriteList.get(holder.getAdapterPosition()).getContactMobile());
                intent.putExtra("Email",favoriteList.get(holder.getAdapterPosition()).getContactEmail());
                intent.putExtra("CompanyName",favoriteList.get(holder.getAdapterPosition()).getContactCompanyName());
                intent.putExtra("Notes",favoriteList.get(holder.getAdapterPosition()).getContactNotes());
                intent.putExtra("Key",favoriteList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);

            }
        });


        //item click and show contact details
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNo = favoriteList.get(holder.getAdapterPosition()).getContactMobile();
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:" + phoneNo));
                context.startActivity(i);
            }
        });

        //delete button click
        holder.CItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the current user's UID
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                // Create a reference to the contact node under the current user's UID
                DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference()
                        .child("Contacts")
                        .child(firebaseUser.getUid())
                        .child(contact.getKey()); // Assuming `contact.getKey()` returns the key of the contact to be deleted

                DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference()
                        .child("Favorite Contacts")
                        .child(firebaseUser.getUid())
                        .child(contact.getKey()); // Assuming `contact.getKey()` returns the key of the contact to be deleted

                // Create a reference to the image storage location
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(contact.getImageURL()); // Assuming `contact.getImageURL()` returns the image URL of the contact

                // Delete the image from storage
                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Image deleted successfully, now delete the contact data from the database
                        contactRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                // Contact deleted successfully
                                favoriteRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //Toast.makeText(context, "Delete From Favorite", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // Optionally, you can update your RecyclerView here to reflect the changes
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to delete contact
                                Toast.makeText(context, "Failed to delete contact", Toast.LENGTH_SHORT).show();
                            }
                        });
                        notifyDataSetChanged();
                    }
                });
            }
        });

        //edit button click
        holder.CItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the details of the contact
                String firstName= contact.getContactFirstName();
                String lastName = contact.getContactLastName();
                String email = contact.getContactEmail();
                String number = contact.getContactMobile();
                String companyName = contact.getContactMobile();
                String notes = contact.getContactMobile();
                String imageUrl = contact.getImageURL();
                String key = contact.getKey();

                // Create an intent to start the UpdateContact activity and pass the contact details as extras
                Intent intent = new Intent(context, UpdateContact.class);
                intent.putExtra("FirstName", firstName);
                intent.putExtra("LastName", lastName);
                intent.putExtra("Email", email);
                intent.putExtra("Number", number);
                intent.putExtra("Image", imageUrl);
                intent.putExtra("CompanyName", companyName);
                intent.putExtra("Notes", notes);
                intent.putExtra("Key", key);
                context.startActivity(intent);
            }
        });
    }

    public void filterList(List<Contact> filteredList) {
        favoriteList = filteredList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        CircleImageView CItemImage;
        TextView CItemName,CItemEdit,CItemDelete;
        ImageView CItemCall;
        RelativeLayout relativeLayout;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);

            CItemImage = itemView.findViewById(R.id.contact_item_image);
            CItemName = itemView.findViewById(R.id.contact_item_name);
            CItemCall = itemView.findViewById(R.id.contact_item_call);
            CItemEdit = itemView.findViewById(R.id.contact_item_edit);
            CItemDelete = itemView.findViewById(R.id.contact_item_delete);
            relativeLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
