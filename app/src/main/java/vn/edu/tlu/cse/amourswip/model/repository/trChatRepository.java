package vn.edu.tlu.cse.amourswip.model.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tlu.cse.amourswip.model.data.xUser;

public class trChatRepository {

    private final DatabaseReference userRef;

    public trChatRepository(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public void getUserInfo(OnResultListener listener) {
        listener.onLoading();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    xUser user = snapshot.getValue(xUser.class);
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
        void onSuccess(xUser user);
        void onError(String error);
        void onLoading();
    }
}