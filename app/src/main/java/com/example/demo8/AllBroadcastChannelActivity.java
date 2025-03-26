package com.example.demo8;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.demo8.Adapter.AllBroadCastChannelAdapter;
import com.example.demo8.MyModels.BroadcastChannelLastMsg;
import com.example.demo8.MyModels.BroadcastChannelMemberModel;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.databinding.ActivityAllBroadcastChannelBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

public class AllBroadcastChannelActivity extends AppCompatActivity {

    ActivityAllBroadcastChannelBinding binding;
    AllBroadCastChannelAdapter allBroadCastChannelAdapter;
    ArrayList<AllBroadcastChannelModel> channelModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAllBroadcastChannelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.chatAllBroadcastCView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatAllBroadcastCView.setHasFixedSize(false);

        channelModels = new ArrayList<>();
        allBroadCastChannelAdapter = new AllBroadCastChannelAdapter(channelModels, this);
        binding.chatAllBroadcastCView.setAdapter(allBroadCastChannelAdapter);


        getBroadCastList();

        ViewTreeObserver viewTreeObserver = binding.allChannelSearchView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove focus from the SearchView
                binding.allChannelSearchView.clearFocus();

                // Remove the listener to avoid multiple invocations
                binding.allChannelSearchView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        //binding.allChannelSearchView.setIconified(false);

        binding.allChannelSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        List<AllBroadcastChannelModel> filteredList = new ArrayList<>();
        for (AllBroadcastChannelModel channelModel : channelModels) {
            // Add the contact to the filtered list if it matches the query
            if (channelModel.getChannelName().toLowerCase().contains(query.toLowerCase()) ||
                    channelModel.getAdminName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(channelModel);
            }
        }
        // Update the adapter with the filtered list
        allBroadCastChannelAdapter.filterList(filteredList);
    }

    public void getBroadCastList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("All Broadcast Channel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    channelModels.clear(); // Clear the list before adding new data
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String adminId = ds.child("adminId").getValue(String.class);
                        AllBroadcastChannelModel allBroadcastChannelModel = ds.getValue(AllBroadcastChannelModel.class);

                        if (allBroadcastChannelModel != null && adminId != null && !adminId.equals(FirebaseAuth.getInstance().getUid())) {
                            // Check if the user is a member of the broadcast channel
                            isUserMemberOfBroadcastChannel(allBroadcastChannelModel.getChannelId(), allBroadcastChannelModel);
                        }
                    }
                    // Sort the contact list
                    Collections.sort(channelModels, new Comparator<AllBroadcastChannelModel>() {
                        @Override
                        public int compare(AllBroadcastChannelModel o1, AllBroadcastChannelModel o2) {
                            return o1.getChannelName().compareToIgnoreCase(o2.getChannelName());
                        }
                    });

                    // Update the adapter with the sorted contact list
                    allBroadCastChannelAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void isUserMemberOfBroadcastChannel(String channelId, AllBroadcastChannelModel allBroadcastChannelModel) {
        DatabaseReference broadcastChannelRef = FirebaseDatabase.getInstance().getReference().child("Broadcast Channel").child(channelId).child("Members");
        broadcastChannelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(FirebaseAuth.getInstance().getUid())) {
                    // User is not a member of the broadcast channel
                    channelModels.add(allBroadcastChannelModel);

                    Collections.sort(channelModels, new Comparator<AllBroadcastChannelModel>() {
                        @Override
                        public int compare(AllBroadcastChannelModel o1, AllBroadcastChannelModel o2) {
                            return o1.getChannelName().compareToIgnoreCase(o2.getChannelName());
                        }
                    });

                    allBroadCastChannelAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Add code for onResume if needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Add code for onPause if needed
    }
}
