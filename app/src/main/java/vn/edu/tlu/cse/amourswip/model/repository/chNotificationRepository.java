package vn.edu.tlu.cse.amourswip.model.repository;

import android.util.Log;

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
import vn.edu.tlu.cse.amourswip.model.data.chNotification;
import vn.edu.tlu.cse.amourswip.model.data.xUser;

public class chNotificationRepository {

    private final DatabaseReference database;
    private final String currentUserId;
    private ValueEventListener matchesListener;

    public chNotificationRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void getNotifications(OnResultListener listener) {
        List<chNotification> notificationList = new ArrayList<>();
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
                            xUser user = userSnapshot.getValue(xUser.class);
                            if (user != null) {
                                // Lấy tin nhắn cuối cùng và timestamp từ node chats/{chatId}/lastMessage
                                database.child("chats").child(chatId).child("lastMessage").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot lastMessageSnapshot) {
                                        String lastMessage = "đã match với bạn"; // Giá trị mặc định
                                        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                                        boolean isUnread = true;
                                        long timestamp = System.currentTimeMillis(); // Giá trị mặc định

                                        if (lastMessageSnapshot.exists()) {
                                            lastMessage = lastMessageSnapshot.child("message").getValue(String.class) != null
                                                    ? lastMessageSnapshot.child("message").getValue(String.class)
                                                    : "đã match với bạn";
                                            Long messageTimestamp = lastMessageSnapshot.child("timestamp").getValue(Long.class);
                                            if (messageTimestamp != null) {
                                                time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(messageTimestamp));
                                                timestamp = messageTimestamp;
                                            }
                                            isUnread = lastMessageSnapshot.child("isUnread").getValue(Boolean.class) != null
                                                    ? lastMessageSnapshot.child("isUnread").getValue(Boolean.class)
                                                    : true;
                                        } else {
                                            // Nếu không có lastMessage, lấy timestamp từ match_notifications
                                            database.child("match_notifications").child(currentUserId).orderByChild("otherUserId").equalTo(matchedUserId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot notificationSnapshot) {
                                                            Long matchTimestamp = null;
                                                            for (DataSnapshot snap : notificationSnapshot.getChildren()) {
                                                                matchTimestamp = snap.child("timestamp").getValue(Long.class);
                                                                if (matchTimestamp != null) {
                                                                    break;
                                                                }
                                                            }

                                                            // Tạo chNotification
                                                            String userImage = user.getPhotos() != null && !user.getPhotos().isEmpty() ? user.getPhotos().get(0) : "";
                                                            chNotification notification = new chNotification(
                                                                    matchedUserId,
                                                                    user.getName(),
                                                                    userImage,
                                                                    "đã match với bạn", // Giá trị mặc định cho lastMessage
                                                                    matchTimestamp != null ? new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(matchTimestamp)) : new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()),
                                                                    true, // Giá trị mặc định cho isUnread
                                                                    matchTimestamp != null ? matchTimestamp : System.currentTimeMillis()
                                                            );
                                                            notificationList.add(notification);
                                                            Log.d("NotificationRepository", "Created notification: " + user.getName() + ", timestamp: " + (matchTimestamp != null ? matchTimestamp : System.currentTimeMillis()));
                                                            listener.onSuccess(notificationList);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            listener.onError(error.getMessage());
                                                        }
                                                    });
                                            return;
                                        }

                                        // Tạo chNotification
                                        String userImage = user.getPhotos() != null && !user.getPhotos().isEmpty() ? user.getPhotos().get(0) : "";
                                        chNotification notification = new chNotification(
                                                matchedUserId,
                                                user.getName(),
                                                userImage,
                                                lastMessage,
                                                time,
                                                isUnread,
                                                timestamp
                                        );
                                        notificationList.add(notification);
                                        Log.d("NotificationRepository", "Created notification: " + user.getName() + ", timestamp: " + timestamp);
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

    public interface OnResultListener {
        void onSuccess(List<chNotification> notifications);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }
}