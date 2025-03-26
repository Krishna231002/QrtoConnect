package com.example.demo8.Utils;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId()!=null){
            return true;
        }
        return false;
    }

    public static DatabaseReference currentUserDetails(){
        return FirebaseDatabase.getInstance().getReference( "UsersDetails").child(FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference groupDetailsReference(String groupId) {
        return FirebaseDatabase.getInstance().getReference("Group Details").child(groupId);
    }


    public static DatabaseReference allUserCollectionReference() {
        return FirebaseDatabase.getInstance().getReference("UsersDetails");
    }
}

