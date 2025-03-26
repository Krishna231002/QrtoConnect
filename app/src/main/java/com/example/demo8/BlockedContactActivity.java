package com.example.demo8;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo8.Adapter.AllBroadCastChannelAdapter;
import com.example.demo8.Adapter.BlockedContactAdapter;
import com.example.demo8.MyModels.BlockContactModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BlockedContactActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BlockedContactAdapter contactAdapter;
    androidx.appcompat.widget.SearchView searchView;
    List<BlockContactModel> blockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contact);

        recyclerView = findViewById(R.id.block_contact_view);
        searchView = findViewById(R.id.search_view_block);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blockList = new ArrayList<>();
        contactAdapter = new BlockedContactAdapter(blockList, this);
        recyclerView.setAdapter(contactAdapter);

    }

    private void filterContacts(String query) {

        List<BlockContactModel> filteredList = new ArrayList<>();
        for (BlockContactModel blockContact : blockList) {
            // Add the contact to the filtered list if it matches the query
            if (blockContact.getBlockFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    blockContact.getBlockLastName().toLowerCase().contains(query.toLowerCase()) ||
                    blockContact.getBlockMobile().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(blockContact);
            }
        }
        // Update the adapter with the filtered list
        contactAdapter.filterList(filteredList);
    }

    private void getBlockedContactList() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("BlockedContacts").child(FirebaseAuth.getInstance().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blockList.clear(); // Clear the list before adding new data
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BlockContactModel blockContactModel = ds.getValue(BlockContactModel.class);
                    if (blockContactModel!=null) {
                        blockContactModel.setBlockKey(ds.getKey());
                        blockList.add(blockContactModel);
                        Log.d("Blocked details: ", ds.toString());

                    }
                }
                contactAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getBlockedContactList();
    }
}