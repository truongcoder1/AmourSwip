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

    public NotificationRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void getNotifications(OnResultListener listener) {
        List<Notification> notificationList = new ArrayList<>();
        listener.onLoading();
        database.child("matches").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    database.child("users").child(matchedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                                Notification notification = new Notification(matchedUserId, user.getName(), "đã match với bạn", time, true, user.isOnline());
                                notificationList.add(0, notification);
                                listener.onSuccess(notificationList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            listener.onError(error.getMessage());
                        }
                    });
                }
                if (notificationList.isEmpty()) {
                    listener.onEmpty();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(List<Notification> notifications);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }
}