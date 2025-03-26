package com.example.demo8;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Adapter.GroupContactAdapter;
import com.example.demo8.Interface.AllConstants;
import com.example.demo8.Interface.ContactItemInterface;
import com.example.demo8.Adapter.SelectedContactAdapter;
import com.example.demo8.MyModels.GroupMemberModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class SelectGroupMembers extends AppCompatActivity implements ContactItemInterface {

    RecyclerView selectedMembers, recyclerViewContact;
    SelectedContactAdapter selectedContactAdapter;
    GroupContactAdapter groupContactAdapter;
    List<Users> selectedContacts, chatList;
    FloatingActionButton btnCreateGroup;
    String groupName;
    Uri groupImage;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    ProgressDialog pd;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);

        selectedMembers = findViewById(R.id.selectedMembersRecyclerView);
        recyclerViewContact = findViewById(R.id.recyclerViewSelectedContact);
        btnCreateGroup = findViewById(R.id.btnSelectDone);
        searchView = findViewById(R.id.group_select_search_view);

        pd = new ProgressDialog(this);

        selectedContacts = new ArrayList<>();
        chatList = new ArrayList<>();
        selectedContactAdapter = new SelectedContactAdapter(this, this);
        groupContactAdapter = new GroupContactAdapter(this, this, chatList);

        selectedMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(this));
        selectedMembers.setAdapter(selectedContactAdapter);
        recyclerViewContact.setAdapter(groupContactAdapter);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            groupName = bundle.getString("GroupName");
            String imageUri = bundle.getString("GroupImage");
            groupImage = Uri.parse(imageUri);
        }

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createGroup();
            }
        });

        ViewTreeObserver viewTreeObserver = searchView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove focus from the SearchView
                searchView.clearFocus();

                // Remove the listener to avoid multiple invocations
                searchView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                filterContacts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterContacts(newText);
                return true;
            }
        });
    }

    private void filterContacts(String query) {
        List<Users> filteredList = new ArrayList<>();
        for (Users users : chatList) {
            // Add the contact to the filtered list if it matches the query
            if (users.getFullname().toLowerCase().contains(query.toLowerCase()) ||
                    users.getMobile().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(users);
            }
        }
        // Update the adapter with the filtered list
        groupContactAdapter.filterList(filteredList);
    }

    private void retrieveContacts() {
        database.child("Contacts").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                List<String> contactMobiles = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String contactMobile = ds.child("contactMobile").getValue(String.class);
                    contactMobiles.add(contactMobile);
                }

                DatabaseReference usersDetailsRef = database.child("UsersDetails");
                usersDetailsRef.orderByChild("mobile").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                        boolean userFound = false;
                        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                            String mobile = userSnapshot.child("mobile").getValue(String.class);

                            if (contactMobiles.contains(mobile)) {

                                Users users = userSnapshot.getValue(Users.class);
                                users.setId(userSnapshot.getKey());
                                if (!users.getId().equals(FirebaseAuth.getInstance().getUid())) {
                                    chatList.add(users);
                                }
                                userFound = true;
                            }
                        }
                        groupContactAdapter.notifyDataSetChanged();

                        if (!userFound) {
                            //Toast.makeText(SelectGroupMembers.this, "No user found for any contact mobile number", Toast.LENGTH_SHORT).show();
                            Log.d("Message: ","No user found for any contact mobile number");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SelectGroupMembers.this, "Not able to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectGroupMembers.this, "Can't", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onContactClick(Users users, int position, boolean isSelect) {
        if (isSelect) {
            for (int i = 0; i < selectedContacts.size(); i++) {
                if (selectedContacts.get(i).getId().equals(users.getId())) {
                    selectedContacts.remove(i);
                    break;
                }
            }
            selectedContactAdapter.setArrayList((ArrayList<Users>) selectedContacts);
            chatList.add(users);
            groupContactAdapter.notifyDataSetChanged();
        } else {
            selectedContacts.add(users);
            selectedContactAdapter.setArrayList((ArrayList<Users>) selectedContacts);
            chatList.remove(users);
            groupContactAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deselectContact(Users users) {
        selectedContacts.remove(users);
        selectedContactAdapter.notifyDataSetChanged();
        chatList.add(users);
        groupContactAdapter.notifyDataSetChanged();
    }

    private void createGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersDetails").child(firebaseAuth.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);

                    String firstName = user.getFirstName();
                    String lastName = user.getLastName();
                    String name = firstName + " " + lastName;
                    pd.setMessage("Creating...");
                    pd.show();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group Details");
                    String groupId = databaseReference.push().getKey();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("Group Image");
                    storageReference.child(groupId + AllConstants.GROUP_IMAGE).putFile(groupImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();

                            Toast.makeText(SelectGroupMembers.this, "Group Created", Toast.LENGTH_SHORT).show();
                            Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                            image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String url = task.getResult().toString();
                                        GroupModels groupModel = new GroupModels();
                                        groupModel.adminId = firebaseAuth.getUid();
                                        groupModel.adminName = name;
                                        groupModel.name = groupName;
                                        groupModel.createAt = String.valueOf(System.currentTimeMillis());
                                        groupModel.image = url;
                                        groupModel.id = groupId;

                                        databaseReference.child(groupId).setValue(groupModel);

                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Details").child(groupId).child("Members");
                                        GroupMemberModel groupMemberModel = new GroupMemberModel();
                                        groupMemberModel.id = firebaseAuth.getUid();
                                        groupMemberModel.role = "Admin";
                                        groupMemberModel.token=user.getToken();
                                        groupMemberModel.mobile = user.getMobile();

                                        reference.child(firebaseAuth.getUid()).setValue(groupMemberModel);

                                        for (Users users : selectedContacts) {
                                            GroupMemberModel memberModel = new GroupMemberModel();
                                            memberModel.id = users.getId();
                                            memberModel.role = "Member";
                                            memberModel.token=users.getToken();
                                            memberModel.mobile = users.getMobile();
                                            reference.child(users.getId()).setValue(memberModel);


                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(SelectGroupMembers.this, "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveContacts();//refresh data
    }
}
