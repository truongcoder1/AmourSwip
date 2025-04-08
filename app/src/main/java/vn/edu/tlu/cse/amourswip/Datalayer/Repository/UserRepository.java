package vn.edu.tlu.cse.amourswip.Datalayer.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.edu.tlu.cse.amourswip.Datalayer.Model.User;

public class UserRepository {

    private DatabaseReference database;
    private FirebaseAuth auth;

    public UserRepository() {
        database = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();
    }

    public void saveUser(User user) {
        String userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            database.child(userId).setValue(user);
        }
    }
}
