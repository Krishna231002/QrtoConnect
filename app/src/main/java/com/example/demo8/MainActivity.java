package com.example.demo8;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.demo8.Fragment.AccountFragment;
import com.example.demo8.Fragment.BroadcastChannelFragment;
import com.example.demo8.Fragment.ChatFragment;
import com.example.demo8.Fragment.ContactsFragment;
import com.example.demo8.Fragment.DialerFragment;
import com.example.demo8.Utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {


    private MeowBottomNavigation bottomNavigation;

    private final int ID_CONTACT = 1;
    private static final int ID_CHAT = 2;
    private final int ID_ADD = 3;
    private final int ID_ACCOUNT = 5;
    private final int ID_BROADCAST_CHANNEL = 4;

    static int PERMISSION_CODE = 100;

    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFCMToken();


        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setBackground(null);
        bottomNavigation.show(ID_CONTACT, true);
        initBottomNavigation();



        // Show initial fragment
        loadFrag(new ContactsFragment(), true);

        // Array of permissions to check
        String[] permissions = {
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE,
                Manifest.permission.INTERNET,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.SYSTEM_ALERT_WINDOW

        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, permissionsToRequest.toArray(new String[0]), PERMISSION_CODE);
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // Retrieve the user ID from Firebase
        reference.child("UsersDetails").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the user data exists
                if (dataSnapshot.exists()) {
                    // Retrieve the user ID from the snapshot
                    //contactId = dataSnapshot.child("mobile").getValue(String.class);
                    String contactId,userName;

                    contactId = dataSnapshot.child("mobile").getValue(String.class);
                    userName = dataSnapshot.child("fullname").getValue(String.class);

                    // Pass the user ID to the startService() method
                    startService1(contactId,userName);
                } else {
                    // Handle the case where user data doesn't exist
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the retrieval process
                Toast.makeText(MainActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });

        //startService1(contactId,userName);

        requestSystemAlertWindowPermission();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            // Check if all permissions were granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions were granted
                // Continue with your app logic
            } else {
                // Some permissions were not granted
                // Handle this as needed
            }
        }
    }

    private void initBottomNavigation() {
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CONTACT, R.drawable.baseline_person_outline_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CHAT, R.drawable.baseline_chat_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ADD, R.drawable.dial_pad));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_BROADCAST_CHANNEL, R.drawable.broad_cast_image));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.baseline_account_circle_selected));

        bottomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()) {
                    case ID_CONTACT:
                        loadFrag(new ContactsFragment(), true);
                        break;
                    case ID_CHAT:
                        loadFrag(new ChatFragment(), false);
                        break;
                    case ID_ADD:
                      /*  Intent intent = new Intent(MainActivity.this, CreateGroupActivity.class);
                        startActivity(intent);*/
                        loadFrag(new DialerFragment(),false);
                        break;
                    case ID_BROADCAST_CHANNEL:
                        loadFrag(new BroadcastChannelFragment(), false);
                        break;
                    case ID_ACCOUNT:
                        loadFrag(new AccountFragment(), false);
                        break;
                }
                return null;
            }
        });
    }


    private void loadFrag(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (addToBackStack) {
            ft.add(R.id.framelayout, fragment);
            // ft.add(R.id.gchatcontainer, new GroupFragment());  // Replace 'AnotherFragment' with the fragment you want to add
        } else {
            ft.replace(R.id.framelayout, fragment);
            // ft.replace(R.id.gchatcontainer, new GroupFragment());  // Replace 'AnotherFragment' with the fragment you want to add
        }
        ft.commit();
    }
    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token  = task.getResult();
                HashMap map = new HashMap<>();
                map.put("token",token);
                Log.i("my Token",token);
                //Toast.makeText(this, "Token: "+map, Toast.LENGTH_SHORT).show();
                FirebaseUtil.currentUserDetails().updateChildren(map);
            }
        });
    }

    public void startService1(String userId,String fullName){
        Application application = getApplication(); // Android's application context
        long appID = 1104786941;   // yourAppID
        String appSign = "35f62338697cb2c2d5f6e5f894135dc33c0b89f4a43cffc0b63320585379875b";
        //String userName = fullName;   // yourUserName

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallInvitationService.init(application, appID, appSign, userId, fullName,callInvitationConfig);
    }

    private void requestSystemAlertWindowPermission() {
        PermissionX.init(MainActivity.this)
                .permissions(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
                .onExplainRequestReason(new ExplainReasonCallback() {
                    @Override
                    public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                        String message = "We need your consent for the following permissions in order to use the offline call function properly";
                        scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        // Handle permission results
                    }
                });
    }
}
