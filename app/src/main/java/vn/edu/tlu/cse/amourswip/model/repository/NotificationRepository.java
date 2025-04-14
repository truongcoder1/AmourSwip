package vn.edu.tlu.cse.amourswip.model.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.edu.tlu.cse.amourswip.model.data.Notification;
import vn.edu.tlu.cse.amourswip.model.data.User;

public class NotificationRepository {

    private final DatabaseReference database;
    private final String currentUserId;
    private ValueEventListener matchesListener;

    public NotificationRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void getNotifications(OnResultListener listener) {
        List<Notification> notificationList = new ArrayList<>();
        listener.onLoading();
        matchesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                if (!snapshot.exists()) {
                    listener.onEmpty();
                    return;
                }

                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    // Tạo chatId
                    String chatId = currentUserId.compareTo(matchedUserId) < 0 ? currentUserId + "_" + matchedUserId : matchedUserId + "_" + currentUserId;

                    // Lấy thông tin người dùng đã match
                    database.child("users").child(matchedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                // Lấy tin nhắn cuối cùng từ node chats/{chatId}/lastMessage
                                database.child("chats").child(chatId).child("lastMessage").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot lastMessageSnapshot) {
                                        String lastMessage = "đã match với bạn"; // Giá trị mặc định
                                        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                                        boolean isUnread = true;

                                        if (lastMessageSnapshot.exists()) {
                                            lastMessage = lastMessageSnapshot.child("message").getValue(String.class) != null
                                                    ? lastMessageSnapshot.child("message").getValue(String.class)
                                                    : "đã match với bạn";
                                            Long timestamp = lastMessageSnapshot.child("timestamp").getValue(Long.class);
                                            if (timestamp != null) {
                                                time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
                                            }
                                            isUnread = lastMessageSnapshot.child("isUnread").getValue(Boolean.class) != null
                                                    ? lastMessageSnapshot.child("isUnread").getValue(Boolean.class)
                                                    : true;
                                        }

                                        // Lấy URL hình ảnh từ user
                                        String userImage = user.getPhotos() != null && !user.getPhotos().isEmpty() ? user.getPhotos().get(0) : "";
                                        Notification notification = new Notification(
                                                matchedUserId,
                                                user.getName(),
                                                userImage,
                                                lastMessage,
                                                time,
                                                isUnread
                                        );
                                        notificationList.add(0, notification);
                                        listener.onSuccess(notificationList);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        listener.onError(error.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            listener.onError(error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        database.child("matches").child(currentUserId).addValueEventListener(matchesListener);
    }

    public void removeListeners() {
        if (matchesListener != null) {
            database.child("matches").child(currentUserId).removeEventListener(matchesListener);
            matchesListener = null;
        }
    }
    //
    public interface OnResultListener {
        void onSuccess(List<Notification> notifications);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }
}