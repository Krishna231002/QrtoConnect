package com.example.demo8;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Adapter.GroupContactAdapter;
import com.example.demo8.Adapter.SelectedContactAdapter;
import com.example.demo8.Interface.ContactItemInterface;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.MyModels.GroupMemberModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddMembers extends AppCompatActivity implements ContactItemInterface {

    RecyclerView selectedMembers, recyclerViewContact;
    SelectedContactAdapter selectedContactAdapter;
    GroupContactAdapter groupContactAdapter;
    List<Users> selectedContacts, chatList;
    FloatingActionButton btnAddMembers;
    String userId;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    ProgressDialog pd;
    GroupModels groupModels;
    SearchView searchView;
    ImageView addMemberBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        groupModels = getIntent().getParcelableExtra("groupModel");

        if (!firebaseAuth.getCurrentUser().getUid().equals(groupModels.getAdminId())) {
            Toast.makeText(this, "Only the group admin can add members", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedMembers = findViewById(R.id.selectedMembersRecyclerViewAdd);
        recyclerViewContact = findViewById(R.id.recyclerViewSelectedContactAdd);
        btnAddMembers = findViewById(R.id.btnSelectDoneAdd);
        searchView = findViewById(R.id.search_view_member);
        addMemberBack=findViewById(R.id.addMemberBack);

        pd = new ProgressDialog(this);

        selectedContacts = new ArrayList<>();
        chatList = new ArrayList<>();
        selectedContactAdapter = new SelectedContactAdapter(this, this);
        groupContactAdapter = new GroupContactAdapter(this, this, chatList);

        selectedMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(this));
        selectedMembers.setAdapter(selectedContactAdapter);
        recyclerViewContact.setAdapter(groupContactAdapter);


        addMemberBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addMembers();

              onBackPressed();
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
                    //users.getLastName().toLowerCase().contains(query.toLowerCase()) ||
                    users.getMobile().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(users);
            }
        }
        // Update the adapter with the filtered list
        groupContactAdapter.filterList(filteredList);
    }

    private void addMembers() {
        pd.setMessage("Adding members...");
        pd.show();

        // Get a reference to the "Members" child node of the existing group
        DatabaseReference reference = database.child("Group Details").child(groupModels.id).child("Members");

        // Add the new members to the "Members" child node
        for (Users user : selectedContacts) {
            GroupMemberModel groupMemberModel = new GroupMemberModel();
            groupMemberModel.id = user.getId();
            groupMemberModel.role = "Member";
            groupMemberModel.token=user.getToken();
            groupMemberModel.mobile = user.getMobile();
            reference.child(user.getId()).setValue(groupMemberModel);
        }

        // Update the groupModels object in the activity
        for (Users user : selectedContacts) {
            GroupMemberModel groupMemberModel = new GroupMemberModel();
            groupMemberModel.id = user.getId();
            groupMemberModel.role = "Member";
            groupMemberModel.token=user.getToken();
            groupMemberModel.mobile = user.getMobile();
            groupModels.members.add(groupMemberModel);
        }

        pd.dismiss();
        Toast.makeText(AddMembers.this, "Members added successfully", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }


    private void retrieveContacts() {
        database.child("Contacts").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                List<String> contactMobiles = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String contactMobile = ds.child("contactMobile").getValue(String.class);
                    userId = ds.child("key").getKey();
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
                                    boolean isNewUser = true;
                                    for (Users selectedUser : selectedContacts) {
                                        if (selectedUser.getId().equals(users.getId())) {
                                            isNewUser = false;
                                            break;
                                        }
                                    }
                                    if (isNewUser) {
                                        boolean isGroupMember = false;
                                        for (GroupMemberModel memberModel : groupModels.members) {
                                            if (memberModel.id.equals(users.getId())) {
                                                isGroupMember = true;
                                                break;
                                            }
                                        }
                                        if (!isGroupMember) {
                                            chatList.add(users);
                                            userFound = true;
                                        }
                                    }
                                }
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
                        Toast.makeText(AddMembers.this, "Not able to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddMembers.this, "Can't", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*private void retrieveContacts() {
        database.child("Contacts").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                List<String> contactMobiles = new ArrayList<>();


                for (DataSnapshot ds : snapshot.getChildren()) {
                    String contactMobile = ds.child("contactMobile").getValue(String.class);
                    userId = ds.child("key").getKey();
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
                                    boolean isNewUser = true;
                                    for (Users selectedUser : selectedContacts) {
                                        if (selectedUser.getId().equals(users.getId())) {
                                            isNewUser = false;
                                            break;
                                        }
                                    }
                                    if (isNewUser) {
                                        chatList.add(users);
                                        userFound = true;
                                    }
                                }
                            }
                        }
                        groupContactAdapter.notifyDataSetChanged();

                        if (!userFound) {
                            Toast.makeText(AddMembers.this, "No user found for any contact mobile number", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddMembers.this, "Not able to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddMembers.this, "Can't", Toast.LENGTH_SHORT).show();
            }
        });
    }*/


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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("groupModel",groupModels);
        setResult(RESULT_OK,intent);
        finish();

        super.onBackPressed();
    }

    public void onResume() {
        super.onResume();
        retrieveContacts();//refresh data
    }
}