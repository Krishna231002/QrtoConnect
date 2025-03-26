package com.example.demo8.Fragment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.demo8.Adapter.FavoriteAdapter;
import com.example.demo8.AddContact;
import com.example.demo8.CreateNewBroadcastChannel;
import com.example.demo8.CreateNewGroup;
import com.example.demo8.RequestActivity;
import com.example.demo8.Adapter.ContactAdapter;
import com.example.demo8.DialerActivity;
import com.example.demo8.MyModels.Contact;
import com.example.demo8.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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


public class ContactsFragment extends Fragment {

    public ContactsFragment() {
        // Required empty public constructor
    }

    private RecyclerView contactRecyclerView,favoriteContactRecyclerView;
    private ContactAdapter contactAdapter;
    FavoriteAdapter favoriteAdapter;
    private List<Contact> contactList;
    private List<Contact> favoriteList;
    TextView txtFav;

    LinearLayout linearLayout;
    private FirebaseUser firebaseUser;
    SearchView searchView;
    private MeowBottomNavigation bottomNavigation;

    private Toolbar toolbar;
    int ID_ACCOUNT = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // This line is crucial to indicate that the fragment has its own options menu.
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        FloatingActionButton fabtn = rootView.findViewById(R.id.fa_btn);
        contactRecyclerView = rootView.findViewById(R.id.contact_view);
        favoriteContactRecyclerView = rootView.findViewById(R.id.contact_favorite_view);
        linearLayout = rootView.findViewById(R.id.linearLayout);
        txtFav = rootView.findViewById(R.id.txtFavorite);
        searchView = rootView.findViewById(R.id.search_view);

        toolbar = rootView.findViewById(R.id.tb_1);

        bottomNavigation = getActivity().findViewById(R.id.bottomNavigation);
        // Setup RecyclerView
        contactList = new ArrayList<>();
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactAdapter(getContext(), contactList);
        contactRecyclerView.setAdapter(contactAdapter);


        favoriteContactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        favoriteList = new ArrayList<>();


        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("QrToConnect");

        // Initialize FirebaseUser
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load contacts
        loadContacts();
        loadFavoriteCon();

        // Handle click on FAB
        fabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), AddContact.class);
                startActivity(intent);

            }
        });


        return rootView;
    }

    private void loadFavoriteCon() {
        DatabaseReference favoriteReference = FirebaseDatabase.getInstance().getReference("Favorite Contacts").child(firebaseUser.getUid());
        favoriteReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Contact contact = ds.getValue(Contact.class);
                    if (contact != null) {
                        linearLayout.setVisibility(View.VISIBLE);
                        contact.setKey(ds.getKey());
                        favoriteList.add(contact);
                    }

                }
                favoriteAdapter = new FavoriteAdapter(getContext(), favoriteList);
                favoriteContactRecyclerView.setAdapter(favoriteAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        if (menu.findItem(R.id.contactsearchbar) == null) {
            menu.clear(); // Clear the existing menu items
            inflater.inflate(R.menu.contact_action_bar, menu);
            MenuItem menuItem = menu.findItem(R.id.contactsearchbar);
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
            searchView.setQueryHint("Search here");

            MenuItem notificationItem = menu.findItem(R.id.contactNotification);
            ImageButton notificationButton = notificationItem.getActionView().findViewById(R.id.notification_button);

            notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RequestActivity.class);
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

            MenuItem menuItem = menu.findItem(R.id.contactsearchbar);
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
            searchView.setQueryHint("Search here");

            MenuItem notificationItem = menu.findItem(R.id.contactNotification);
            ImageButton notificationButton = notificationItem.getActionView().findViewById(R.id.notification_button);

            notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RequestActivity.class);
                    startActivity(intent);
                }
            });


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
        if(item != null) {
            switch (item.getItemId()) {
                case R.id.groupChatfromContact:
                    Intent groupIntent = new Intent(getContext(), CreateNewGroup.class);
                    groupIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // Add this line
                    startActivity(groupIntent);
                    return true;
                case R.id.ProfilefromContact:
                    bottomNavigation.show(ID_ACCOUNT, true);
                    return true;
                case R.id.broadcastfromContact:
                    Intent broadcastIntent = new Intent(getContext(), CreateNewBroadcastChannel.class);
                    broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // Add this line
                    startActivity(broadcastIntent);
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void filterContacts(String query) {
        List<Contact> filteredContactList = new ArrayList<>();
        for (Contact contact : contactList) {
            // Add the contact to the filtered list if it matches the query
            if (contact.getContactFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getContactLastName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getContactMobile().toLowerCase().contains(query.toLowerCase())) {
                filteredContactList.add(contact);
            }
        }
        // Update the adapter with the filtered list
        contactAdapter.filterList(filteredContactList);

        List<Contact> filteredFavoriteList = new ArrayList<>();

        for (Contact contact : favoriteList) {
            // Add the contact to the filtered list if it matches the query
            if (contact.getContactFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getContactLastName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getContactMobile().toLowerCase().contains(query.toLowerCase())) {
                filteredFavoriteList.add(contact);
            }
        }
        // Update the adapter with the filtered list
        favoriteAdapter.filterList(filteredFavoriteList);

    }

    private void loadContacts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Contact contact = ds.getValue(Contact.class);
                    if (contact != null) {
                        contact.setKey(ds.getKey());
                        contactList.add(contact);

                        Collections.sort(contactList, new Comparator<Contact>() {
                            @Override
                            public int compare(Contact c1, Contact c2) {
                                // Sort contacts by first name
                                return c1.getContactFirstName().compareToIgnoreCase(c2.getContactFirstName());
                            }
                        });
                        //startService(contactId,userName);
                    }
                }
                contactAdapter.notifyDataSetChanged();

                // Update the favorite contacts list
                DatabaseReference favoriteReference = FirebaseDatabase.getInstance().getReference("Favorite Contacts").child(firebaseUser.getUid());
                favoriteReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Contact> favoriteContacts = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Contact contact = ds.getValue(Contact.class);
                            if (contact != null) {
                                contact.setKey(ds.getKey());
                                favoriteContacts.add(contact);
                                Collections.sort(favoriteList, new Comparator<Contact>() {
                                    @Override
                                    public int compare(Contact c1, Contact c2) {
                                        // Sort favorite contacts by first name
                                        return c1.getContactFirstName().compareToIgnoreCase(c2.getContactFirstName());
                                    }
                                });
                            }
                        }
                        favoriteAdapter.filterList(favoriteContacts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}