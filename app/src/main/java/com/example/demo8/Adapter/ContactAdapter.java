package com.example.demo8.Adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo8.ContactDetails;
import com.example.demo8.DbHelper;
import com.example.demo8.Fragment.ContactsFragment;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.example.demo8.UpdateContact;
import com.example.demo8.Users;
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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{

    Context context;
    List<Contact> contactList;
    ContactsFragment contactsFragment;
    FirebaseUser firebaseUser;
    private List<Contact> filteredContactList;

    public ContactAdapter() {
    }

    public ContactAdapter(Context context, List<Contact> contactList){
        this.context = context;
        this.contactList = contactList;
        this.filteredContactList = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_items,parent,false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {

        //get data
        final Contact contact = contactList.get(position);

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
                intent.putExtra("Image",contactList.get(holder.getAdapterPosition()).getImageURL());
                intent.putExtra("FirstName",contactList.get(holder.getAdapterPosition()).getContactFirstName());
                intent.putExtra("LastName",contactList.get(holder.getAdapterPosition()).getContactLastName());
                intent.putExtra("Mobile",contactList.get(holder.getAdapterPosition()).getContactMobile());
                intent.putExtra("Email",contactList.get(holder.getAdapterPosition()).getContactEmail());
                intent.putExtra("CompanyName",contactList.get(holder.getAdapterPosition()).getContactCompanyName());
                intent.putExtra("Notes",contactList.get(holder.getAdapterPosition()).getContactNotes());
                intent.putExtra("Key",contactList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);

            }
        });


        //item click and show contact details
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNo = contactList.get(holder.getAdapterPosition()).getContactMobile();
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
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void filterList(List<Contact> filteredList) {
        contactList = filteredList;
        notifyDataSetChanged();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder{

        CircleImageView CItemImage;
        TextView CItemName,CItemEdit,CItemDelete;
        ImageView CItemCall;
        RelativeLayout relativeLayout;

        public ContactViewHolder(@NonNull View itemView) {
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
