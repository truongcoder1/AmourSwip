package vn.edu.tlu.cse.amourswip.model.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tlu.cse.amourswip.model.data.User;

public class ChatRepository {

    private final DatabaseReference userRef;

    public ChatRepository(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public void getUserInfo(OnResultListener listener) {
        listener.onLoading();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        listener.onSuccess(user);
                    } else {
                        listener.onError("Không tìm thấy thông tin người dùng");
                    }
                } else {
                    listener.onError("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(User user);
        void onError(String error);
        void onLoading();
    }
}