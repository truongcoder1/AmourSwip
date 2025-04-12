package vn.edu.tlu.cse.amourswip.model.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.edu.tlu.cse.amourswip.model.data.User;

public class LikeRepository {

    private final DatabaseReference database;
    private final String currentUserId;

    public LikeRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Lấy tọa độ của người dùng hiện tại
    public void getCurrentUserLocation(OnLocationListener locationListener) {
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);
                    if (latitude != null && longitude != null) {
                        locationListener.onSuccess(latitude, longitude);
                    } else {
                        locationListener.onError("Không tìm thấy tọa độ của bạn");
                    }
                } else {
                    locationListener.onError("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                locationListener.onError(error.getMessage());
            }
        });
    }

    // Lấy danh sách người đã thích bạn (hỗ trợ pagination)
    public void getUsersWhoLikedMe(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<User> usersWhoLikedMe = new ArrayList<>();
        Set<String> userIds = new HashSet<>(); // Để tránh trùng lặp

        Query query = database.child("likes").orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && userSnapshot.hasChild(currentUserId) && !userIds.contains(userId)) {
                        // Người dùng này đã thích bạn
                        database.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                                User user = userDataSnapshot.getValue(User.class);
                                if (user != null) {
                                    user.setUid(userId);
                                    usersWhoLikedMe.add(user);
                                    userIds.add(userId); // Thêm userId vào Set để tránh trùng lặp
                                }
                                // Kiểm tra nếu đã lấy hết dữ liệu trong trang
                                if (usersWhoLikedMe.size() == snapshot.getChildrenCount()) {
                                    if (usersWhoLikedMe.isEmpty()) {
                                        listener.onEmpty();
                                    } else {
                                        listener.onSuccess(usersWhoLikedMe);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
                if (usersWhoLikedMe.isEmpty()) {
                    listener.onEmpty();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Lấy danh sách người bạn đã thích (hỗ trợ pagination)
    public void getUsersILiked(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<User> usersILiked = new ArrayList<>();
        Set<String> userIds = new HashSet<>(); // Để tránh trùng lặp

        Query query = database.child("likes").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onEmpty();
                    return;
                }

                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserId = likeSnapshot.getKey();
                    if (likedUserId != null && !userIds.contains(likedUserId)) {
                        database.child("users").child(likedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    user.setUid(likedUserId);
                                    usersILiked.add(user);
                                    userIds.add(likedUserId); // Thêm userId vào Set để tránh trùng lặp
                                }
                                // Kiểm tra nếu đã lấy hết dữ liệu trong trang
                                if (usersILiked.size() == snapshot.getChildrenCount()) {
                                    if (usersILiked.isEmpty()) {
                                        listener.onEmpty();
                                    } else {
                                        listener.onSuccess(usersILiked);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(List<User> users);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }

    public interface OnLocationListener {
        void onSuccess(double latitude, double longitude);
        void onError(String error);
    }
}