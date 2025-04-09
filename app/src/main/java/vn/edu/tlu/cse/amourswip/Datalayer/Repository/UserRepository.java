package vn.edu.tlu.cse.amourswip.Datalayer.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tlu.cse.amourswip.Datalayer.Model.User;

public class UserRepository {

    private DatabaseReference database;
    private FirebaseAuth auth;

    public UserRepository() {
        database = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();
    }

    // Lấy UID của người dùng hiện tại
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    // Lưu thông tin người dùng mới (dùng khi đăng ký)
    public void saveUser(User user, OnUserActionListener listener) {
        String userId = getCurrentUserId();
        if (userId != null) {
            database.child(userId).setValue(user)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        } else {
            listener.onFailure("Không tìm thấy người dùng hiện tại");
        }
    }

    // Cập nhật thông tin người dùng (cập nhật từng trường)
    public void updateUserField(String field, Object value, OnUserActionListener listener) {
        String userId = getCurrentUserId();
        if (userId != null) {
            database.child(userId).child(field).setValue(value)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        } else {
            listener.onFailure("Không tìm thấy người dùng hiện tại");
        }
    }

    // Lấy thông tin người dùng hiện tại
    public void getCurrentUser(ValueEventListener listener) {
        String userId = getCurrentUserId();
        if (userId != null) {
            database.child(userId).addListenerForSingleValueEvent(listener);
        }
    }

    // Interface để xử lý callback
    public interface OnUserActionListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}