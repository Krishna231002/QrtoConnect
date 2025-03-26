package com.example.demo8.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.demo8.Adapter.BroadcastChannelAdapter;
import com.example.demo8.AllBroadcastChannelActivity;
import com.example.demo8.CreateNewBroadcastChannel;
import com.example.demo8.CreateNewGroup;
import com.example.demo8.MyModels.BroadcastChannelMemberModel;
import com.example.demo8.MyModels.BroadcastChannelModel;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.example.demo8.RequestActivity;
import com.example.demo8.Utils.Util;
import com.example.demo8.databinding.FragmentBroadcastChannelBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BroadcastChannelFragment extends Fragment {

    private FragmentBroadcastChannelBinding binding;
    BroadcastChannelAdapter channelAdapter;
    com.example.demo8.Utils.Util util;
    List<BroadcastChannelModel> channelModels;
    MeowBottomNavigation bottomNavigation;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    private Toolbar toolbar;
    int ID_ACCOUNT = 5;

    public BroadcastChannelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // This line is crucial to indicate that the fragment has its own options menu.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentBroadcastChannelBinding.inflate(inflater, container, false);
        util = new Util();

        firebaseAuth = FirebaseAuth.getInstance();
        channelModels = new ArrayList<>();
        channelAdapter = new BroadcastChannelAdapter(channelModels,getContext());

        binding.chatBroadcastCView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatBroadcastCView.setHasFixedSize(false);

        binding.chatBroadcastCView.setAdapter(channelAdapter);
        channelAdapter.setChannelModels(channelModels);

        bottomNavigation = getActivity().findViewById(R.id.bottomNavigation);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(binding.tb1.getRoot());
        activity.getSupportActionBar().setTitle("QrToConnect");

        getBroadCastList();

        return binding.getRoot();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        if (menu.findItem(R.id.broadcastsearchbar) == null) {
            menu.clear(); // Clear the existing menu items
            inflater.inflate(R.menu.broadcast_action_bar, menu);
            MenuItem menuItem = menu.findItem(R.id.broadcastsearchbar);
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
            searchView.setQueryHint("Search here");

            MenuItem notificationItem = menu.findItem(R.id.community_button);
            ImageButton notificationButton = notificationItem.getActionView().findViewById(R.id.communityButton);

            notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), AllBroadcastChannelActivity.class);
                    startActivity(intent);
                }
            });

            // Set up query text listener
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

            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Search view is in focus, hide bottom navigation
                    bottomNavigation.setVisibility(View.GONE);
                } else {
                    // Search view lost focus, show bottom navigation
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            });
        }else {
            menu.clear(); // Clear the existing menu items
            inflater.inflate(R.menu.broadcast_action_bar, menu);
            MenuItem menuItem = menu.findItem(R.id.broadcastsearchbar);
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
            searchView.setQueryHint("Search here");

            MenuItem notificationItem = menu.findItem(R.id.community_button);
            ImageButton notificationButton = notificationItem.getActionView().findViewById(R.id.communityButton);

            notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), AllBroadcastChannelActivity.class);
                    startActivity(intent);
                }
            });

            // Set up query text listener
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
        if(item  !=null) {

            switch (item.getItemId()) {
                case R.id.RequestActivity:
                    Intent intent = new Intent(getContext(), RequestActivity.class);
                    startActivity(intent);
                    break;
                case R.id.ProfilefromBroadcast:
                    bottomNavigation.show(ID_ACCOUNT,true);
                    break;
                case R.id.createBroadCast:
                    Intent intent2 = new Intent(getContext(), CreateNewBroadcastChannel.class);
                    startActivity(intent2);
                    break;

            }
        }

        return super.onOptionsItemSelected(item);
    }


    /*public void getBroadCastList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                channelModels.clear();

                if (snapshot.exists()) {
                    List<String> reqChannelIds = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String channelId = ds.getKey();

                        // Retrieve the list of requests for this channel
                        DatabaseReference reqReference = reference.child(channelId).child("Requests");
                        reqReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    for (DataSnapshot ds1 : dataSnapshot.getChildren()){
                                        reqChannelIds.add(ds1.getKey());
                                    }
                                    // Filter the channels based on the current user's role
                                    filterBroadCastChannels(ds, reqChannelIds);
                                } else {
                                    // If there are no requests for this channel, add it to the list if the user is the admin
                                    if (ds.child("adminid").getValue(String.class).equals(firebaseUser.getUid())) {
                                        BroadcastChannelModel channelModel = ds.getValue(BroadcastChannelModel.class);
                                        channelModel.setChannelId(ds.getKey());
                                        channelModels.add(channelModel);
                                    }
                                    channelAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void filterBroadCastChannels(DataSnapshot ds, List<String> reqChannelIds) {
        // Retrieve the list of members for this channel
        DataSnapshot membersSnapshot = ds.child("Members");
        if (membersSnapshot.exists()) {
            for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                if (memberSnapshot.getKey().equals(firebaseUser.getUid())) {
                    // Check if the current user is the admin or a member of this channel
                    if (ds.child("adminid").getValue(String.class).equals(firebaseUser.getUid()) || memberSnapshot.child("role").getValue(String.class).equals("Member")) {
                        // Check if the user's request to join this channel has been accepted
                        if (reqChannelIds.contains(ds.getKey())) {
                            BroadcastChannelModel channelModel = ds.getValue(BroadcastChannelModel.class);
                            channelModel.setChannelId(ds.getKey());
                            channelModels.add(channelModel);
                        }
                        break;
                    }
                }
            }
        }
        channelAdapter.notifyDataSetChanged();
    }*/
    /*public void getBroadCastList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                channelModels.clear();


                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String channelId = ds.getKey();

                        DatabaseReference reqReference = FirebaseDatabase.getInstance().getReference("Requests");
                        reqReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    for (DataSnapshot ds1 : dataSnapshot.getChildren()){
                                        reqChannelId = ds1.getKey();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        String adminId = ds.child("adminid").getValue(String.class);


                        DataSnapshot membersSnapshot = ds.child("Members");
                        if (membersSnapshot.exists()) {
                            for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                                if (memberSnapshot.getKey().equals(firebaseUser.getUid())) {
                                    if (adminId != null && adminId.equals(firebaseUser.getUid())) {
                                        // This is your channel, and you are the admin
                                        if (reqChannelId != null && reqChannelId.equals(channelId)) {
                                            BroadcastChannelModel channelModel = ds.getValue(BroadcastChannelModel.class);
                                            channelModel.setChannelId(channelId);
                                            channelModels.add(channelModel);
                                        }
                                    } else {
                                        // This is a channel where you are a member, and your request has been accepted
                                        if (reqChannelId != null && reqChannelId.equals(channelId)) {
                                            BroadcastChannelModel channelModel = ds.getValue(BroadcastChannelModel.class);
                                            channelModel.setChannelId(channelId);
                                            channelModels.add(channelModel);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    channelAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }*/

    public void getBroadCastList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Broadcast Channel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                channelModels.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        BroadcastChannelModel broadcastChannelModel = ds.getValue(BroadcastChannelModel.class);
                        String channelId = ds.getKey();

                        String adminId = ds.child("adminid").getValue(String.class);

                        DataSnapshot membersSnapshot = ds.child("Members");
                        List<BroadcastChannelMemberModel> memberModelList = new ArrayList<>();

                        for (DataSnapshot data : membersSnapshot.getChildren()) {
                            BroadcastChannelMemberModel memberModel = data.getValue(BroadcastChannelMemberModel.class);
                            memberModelList.add(memberModel);
                        }

                        broadcastChannelModel.members = memberModelList;

                        if (membersSnapshot.exists()) {
                            for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                                if (memberSnapshot.getKey().equals(firebaseUser.getUid())) {
                                    if (adminId != null && adminId.equals(firebaseUser.getUid())) {
                                        // This is your channel, and you are the admin
                                        broadcastChannelModel.setChannelId(channelId);
                                        channelModels.add(broadcastChannelModel);
                                    } else {
                                        // This is a channel where you are a member, and your request has been accepted
                                        broadcastChannelModel.setChannelId(channelId);
                                        channelModels.add(broadcastChannelModel);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // Sort the channelModels list
                    Collections.sort(channelModels, new Comparator<BroadcastChannelModel>() {
                        @Override
                        public int compare(BroadcastChannelModel b1, BroadcastChannelModel b2) {
                            // Sort channels by channel name
                            return b1.getChannelName().compareToIgnoreCase(b2.getChannelName());
                        }
                    });

                    // Update the adapter with the sorted channelModels list
                    channelAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void filterContacts(String query) {
        List<BroadcastChannelModel> filteredList = new ArrayList<>();
        for (BroadcastChannelModel channelModel : channelModels) {
            // Add the contact to the filtered list if it matches the query
            if (channelModel.getChannelName().toLowerCase().contains(query.toLowerCase()))  {
                filteredList.add(channelModel);
            }
        }
        // Update the adapter with the filtered list
        channelAdapter.filterList(filteredList);
    }

    @Override
    public void onResume() {
        super.onResume();
        util.updateOnlineStatus("online");
        getBroadCastList();

    }


    @Override
    public void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }
}