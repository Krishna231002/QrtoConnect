package com.example.demo8.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.demo8.Adapter.GroupChatAdapter;
import com.example.demo8.CreateNewGroup;
import com.example.demo8.GroupModels;
import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.GroupMemberModel;
import com.example.demo8.Utils.Util;
import com.example.demo8.databinding.FragmentGroupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GroupFragment extends Fragment {

    private FragmentGroupBinding binding;
    ImageButton btnCreateGroup;
    GroupChatAdapter groupChatAdapter;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    com.example.demo8.Utils.Util util;

    ArrayList<GroupModels> groupModels;

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();


    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        util = new Util();
        firebaseAuth = FirebaseAuth.getInstance();
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Groups");

        groupModels = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(groupModels,getContext());

        binding.chatGroupView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatGroupView.setHasFixedSize(false);

        binding.chatGroupView.setAdapter(groupChatAdapter);
        groupChatAdapter.setGroupModels(groupModels);

        getGroupList();

        binding.btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateNewGroup.class);
                startActivity(intent);
            }
        });
        return binding.getRoot();

    }

    public void getGroupList(){
        Query query = FirebaseDatabase.getInstance().getReference("Group Details");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    groupModels.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        DataSnapshot ms = ds.child("Members").child(firebaseUser.getUid());
                        if (ms.exists()) {

                            GroupModels groupModel = ds.getValue(GroupModels.class);
                            DataSnapshot dataSnapshot = ds.child("Members");
                            List<GroupMemberModel> memberModelList = new ArrayList<>();
                            GroupLastMessageModel lastMessageModel = ds.child("lastMessageModel").getValue(GroupLastMessageModel.class);
                            if (lastMessageModel != null) {
                                lastMessageModel.date = Util.getTimeAgo(Long.parseLong(lastMessageModel.date));
                                groupModel.lastMessageModel = lastMessageModel;
                            }

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                GroupMemberModel memberModel = data.getValue(GroupMemberModel.class);
                                memberModelList.add(memberModel);
                            }

                            DataSnapshot roleSnapshot = ds.child("Members").child(FirebaseAuth.getInstance().getUid()).child("role");
                            if (roleSnapshot.exists() && roleSnapshot.getValue() != null) {
                                groupModel.isAdmin = roleSnapshot.getValue().toString().equals("Admin");
                            }

                            groupModel.members = memberModelList;

                            // Check if the member is present in the group
                            boolean isMemberPresent = false;
                            for (GroupMemberModel member : memberModelList) {
                                if (member.id.equals(firebaseUser.getUid())) {
                                    isMemberPresent = true;
                                    break;
                                }
                            }

                            // If the member is present, add the group details to the list
                            if (isMemberPresent) {
                                groupModels.add(groupModel);
                            }
                        }
                    }
                }
                groupChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    @Override
    public void onResume() {
        super.onResume();
        util.updateOnlineStatus("online");
        getGroupList();

    }

    @Override
    public void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }
}