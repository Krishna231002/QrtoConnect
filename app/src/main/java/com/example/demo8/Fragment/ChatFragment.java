package com.example.demo8.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.demo8.Adapter.CombinedAdapter;
import com.example.demo8.CreateNewGroup;
import com.example.demo8.GroupModels;
import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.GroupMemberModel;

import com.example.demo8.R;
import com.example.demo8.Users;
import com.example.demo8.Utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;

    private Toolbar toolbar;
    private CombinedAdapter combinedAdapter;
    private List<Object> combinedList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private MeowBottomNavigation bottomNavigation;
    private Util util;
    List<String> contactMobiles;
    int ID_ACCOUNT = 5;



    public ChatFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // This line is crucial to indicate that the fragment has its own options menu.
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        util = new Util();

        chatRecyclerView = view.findViewById(R.id.chatContactView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        combinedList = new ArrayList<>();
        combinedAdapter = new CombinedAdapter(getContext(), combinedList);
        chatRecyclerView.setAdapter(combinedAdapter);

        bottomNavigation = getActivity().findViewById(R.id.bottomNavigation);


        toolbar=view.findViewById(R.id.tb_1);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("QrToConnect");


        // Retrieve both group chats and individual chats
        combinedAdapter.setGroupModels(combinedList);


        return view;
    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (menu.findItem(R.id.actionsearchbar) == null) {
            inflater.inflate(R.menu.chat_action_bar, menu);


            MenuItem menuItem = menu.findItem(R.id.actionsearchbar);
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
            searchView.setQueryHint("Search here");

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

            sortChatList();

            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Search view is in focus, hide bottom navigation
                    bottomNavigation.setVisibility(View.GONE);
                } else {
                    // Search view lost focus, show bottom navigation
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.groupChat:
                Intent intent = new Intent(getContext(), CreateNewGroup.class);
                startActivity(intent);
                break;
            case R.id.Profile:
                bottomNavigation.show(ID_ACCOUNT,true);
                break;

        }

        return super.onOptionsItemSelected(item);
    }




    private void sortChatList() {
        Collections.sort(combinedList, new ChatItemComparator());
        combinedAdapter.notifyDataSetChanged();
    }

    private void filterContacts(String query) {
        List<Object> filteredList = new ArrayList<>();
        for (Object object : combinedList) {
            if (object instanceof GroupModels) {
                GroupModels groupModel = (GroupModels) object;
                if (groupModel.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(groupModel);
                }
            } else if (object instanceof Users) {
                Users users = (Users) object;
                if (users.getFullname().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(users);
                }
            }
        }
        sortChatList();
        combinedAdapter.filterList(filteredList);
    }


    private void fetchCombinedData() {
        combinedList.clear();
        retrieveContacts();
        getGroupList();

    }



    private void retrieveContacts() {
        DatabaseReference contactsRef = database.child("Contacts").child(firebaseUser.getUid());
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactMobiles = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    combinedList.clear();
                    String contactMobile = ds.child("contactMobile").getValue(String.class);
                    contactMobiles.add(contactMobile);
                }
                fetchUsersAndUnreadMessageCounts(contactMobiles);
                //fetchUnreadMessageCounts(contactMobiles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Can't fetch contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Fetch unread message counts from Firebase

  /*  private void fetchUnreadMessageCounts(List<String> contactMobiles) {
        Query query = FirebaseDatabase.getInstance().getReference("Chats")
                .child(FirebaseAuth.getInstance().getUid()).child("UnreadMessages");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (firebaseUser != null && snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String contactId = ds.getKey();
                        Integer unreadData = ds.child("unreadMessageCount").getValue(Integer.class);
                        String senderId = ds.child("senderId").getValue(String.class); // Fetch senderId from the unread message model
                        // Check if the contact is in the list of contact mobiles
                        if (contactMobiles.contains(contactId)) {
                            // Update the unread message count for the corresponding contact
                            updateUnreadMessageCount(contactId, senderId, unreadData);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }

    private void updateUnreadMessageCount(String contactId, String senderId, Integer unreadCount) {
        // Find the corresponding contact and update its unread message count
        for (int i = 0; i < combinedList.size(); i++) {
            Users contact = (Users) combinedList.get(i);
            if (contact.getId().equals(senderId)) { // Comparing senderId with contactId
                contact.setUnreadMessage(unreadCount);
                // Notify the adapter or update the UI to reflect the changes
                combinedAdapter.notifyItemChanged(i);
                //break; // No need to continue looping once the contact is found
            }
            String toastMessage = "SenderId: " + senderId + "\nUnread messages: " + unreadCount;
            showToast(getContext(), toastMessage);

        }
    }




    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }*/

  /* String userId = firebaseUser.getUid();
            DatabaseReference userUnreadRef = unreadMessageRef.child(FirebaseAuth.getInstance().getUid()).child("UnreadMessages");
            userUnreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Update unread message counts for individual chats
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String contactId = ds.getKey();
                        int unreadCount = ds.getValue(Integer.class);
                        updateUnreadMessageCount(contactId, unreadCount);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch unread message counts", Toast.LENGTH_SHORT).show();
                }
            });
*/

    // Similarly, fetch unread message counts for group chats if needed
  /*  private void updateUnreadMessageCount(String contactId, int unreadCount) {
        for (Object obj : combinedList) {
            if (obj instanceof Users) {
                Users user = (Users) obj;
                if (user.getId().equals(contactId)) {
                    user.setUnreadMessage(unreadCount);
                    combinedAdapter.notifyDataSetChanged();
                    break;
                }
            } else if (obj instanceof GroupModels) {
                GroupModels groupModels = (GroupModels) obj;
                if (groupModels.getId().equals(contactId)) {
                    groupModels.setUnreadMessage(unreadCount);
                    combinedAdapter.notifyDataSetChanged();
                    break;
                }
                // Update unread message count for group chats if needed
            }
        }
    }*/


    private void fetchUsersAndUnreadMessageCounts(List<String> contactMobiles) {
        DatabaseReference usersRef = database.child("UsersDetails");
        usersRef.orderByChild("mobile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                List<Object> newContactList = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String mobile = ds.child("mobile").getValue(String.class);
                    if (contactMobiles.contains(mobile)) {
                        Users users = ds.getValue(Users.class);

                        users.setId(ds.getKey());
                        newContactList.add(users);
                        userFound = true;
                    }
                }
                if (!userFound) {
                   // Toast.makeText(getContext(), "No user found for any contact mobile number", Toast.LENGTH_SHORT).show();
                    Log.d("Message: ","No user found for any contact mobile number");
                }
                else {
                    // Fetch unread message counts for the fetched users
                    combinedList.addAll(newContactList);
                    // Sort the combined list before updating the adapter
                    Collections.sort(combinedList, new ChatItemComparator());

                    fetchUnreadMessageCounts(combinedList);
                }

                combinedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Can't fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUnreadMessageCounts(List<Object> contactList) {
        Query query = FirebaseDatabase.getInstance().getReference("Chats")
                .child(FirebaseAuth.getInstance().getUid()).child("UnreadMessages");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() != null && firebaseUser != null && snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String contactId = snapshot.child("senderId").getValue(String.class);
                        Integer unreadCount = snapshot.child("unreadMessageCount").getValue(Integer.class);
                        updateUnreadMessageCount(contactId, unreadCount, contactList);

                        //     Toast.makeText(getContext(), "SenderId: " + contactId, Toast.LENGTH_SHORT).show();
                        //   Toast.makeText(getContext(), "UnreadCount: " + unreadCount, Toast.LENGTH_SHORT).show();

                        Log.d("UnreadMessageCount", "SenderId: " + contactId + ", UnreadCount: " + unreadCount);
                    }

                } else {
                    // Handle the case where getContext() is null
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }

    private void updateUnreadMessageCount(String contactId, Integer unreadCount, List<Object> contactList) {
        Log.d("updateUnread", "Updating unread count for contactId: " + contactId + ", unreadCount: " + unreadCount);
        for (Object contact : contactList) {
            if (contact instanceof Users && ((Users) contact).getId().equals(contactId)) {
                // Update the unread message count for the corresponding contact
                ((Users) contact).setUnreadMessage(unreadCount);
                // Show toast for debugging
                String toastMessage = "Contact: " + ((Users) contact).getFullname() + ", Unread Count: " + unreadCount;
                //  Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                Log.d("updateUnread", "Toast displayed: " + toastMessage);
                // Notify the adapter to update the UI
                combinedAdapter.notifyDataSetChanged();
                break; // No need to continue looping once the contact is found
            }
        }
    }





    public void getGroupList(){
        Query query = FirebaseDatabase.getInstance().getReference("Group Details");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                combinedList.clear();

                if (snapshot.exists()) {
                    combinedList.clear();
                    List<Object> newGroupList = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        DataSnapshot ms = ds.child("Members").child(firebaseUser.getUid());
                        if (ms.exists()) {

                            GroupModels groupModel = ds.getValue(GroupModels.class);
                            DataSnapshot dataSnapshot = ds.child("Members");
                            List<GroupMemberModel> memberModelList = new ArrayList<>();
                            GroupLastMessageModel lastMessageModel = ds.child("lastMessageModel").getValue(GroupLastMessageModel.class);
                            if (lastMessageModel != null) {
                                // Check the type of message
                                if (lastMessageModel.getType().equals("image")) {
                                    // If it's an image message, set the message to "Photo"
                                    lastMessageModel.setMessage("Photo");
                                }else {
                                    String messageText = ds.child("lastMessageModel").child("message").getValue(String.class);
                                    lastMessageModel.setMessage(messageText);
                                }
                                lastMessageModel.setDate(Util.getTimeAgo(Long.parseLong(lastMessageModel.getDate())));
                                groupModel.setLastMessageModel(lastMessageModel);
                            }

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                GroupMemberModel memberModel = data.getValue(GroupMemberModel.class);
                                memberModelList.add(memberModel);
                            }

                            DataSnapshot roleSnapshot = ds.child("Members").child(FirebaseAuth.getInstance().getUid()).child("role");
                            if (roleSnapshot.exists() && roleSnapshot.getValue() != null) {
                                groupModel.setAdmin(roleSnapshot.getValue().toString().equals("admin"));
                            }

                            groupModel.setMembers(memberModelList);

                            // Check if the member is present in the group
                            boolean isMemberPresent = false;
                            for (GroupMemberModel member : memberModelList) {
                                if (member.getId().equals(firebaseUser.getUid())) {
                                    isMemberPresent = true;
                                    break;
                                }
                            }

                            // If the member is present, add the group details to the list
                            if (isMemberPresent) {
                                newGroupList.add(groupModel);
                            }
                        }

                    }
                    combinedList.addAll(newGroupList);
                    combinedAdapter.notifyDataSetChanged();
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
        fetchCombinedData();
        util.updateOnlineStatus("online");
    }

    @Override
    public void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }
}

class ChatItemComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof GroupModels && o2 instanceof GroupModels) {
            // Sort GroupModels by name
            GroupModels group1 = (GroupModels) o1;
            GroupModels group2 = (GroupModels) o2;
            return group1.getName().compareToIgnoreCase(group2.getName());
        } else if (o1 instanceof Users && o2 instanceof Users) {
            // Sort Users by fullname
            Users user1 = (Users) o1;
            Users user2 = (Users) o2;
            return user1.getFullname().compareToIgnoreCase(user2.getFullname());
        }else if (o1 instanceof GroupModels && o2 instanceof Users) {
            return -1;
        } else if (o1 instanceof Users && o2 instanceof GroupModels) {
            return 1;
        } else {
            // Handle cases where objects are not GroupModels or Users (optional)
            return 0;
        }
    }
}