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
import android.util.Log;

public class LikeRepository {

    private static final String TAG = "LikeRepository";
    private final DatabaseReference database;
    private final String currentUserId;
    private long processedUsers;
    private long totalUsers;

    public LikeRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

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

    public void getUsersWhoLikedMe(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<User> usersWhoLikedMe = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

        Query query = database.child("likedBy").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersWhoLikedMe: No users found in likedBy");
                    listener.onEmpty();
                    return;
                }

                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String userId = likeSnapshot.getKey();
                    if (userId != null && !userIds.contains(userId)) {
                        database.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (!userSnapshot.exists()) {
                                    Log.e(TAG, "getUsersWhoLikedMe: User data not found for uid: " + userId);
                                    userIds.add(userId);
                                    if (userIds.size() == snapshot.getChildrenCount()) {
                                        if (usersWhoLikedMe.isEmpty()) {
                                            Log.d(TAG, "getUsersWhoLikedMe: No users found after processing");
                                            listener.onEmpty();
                                        } else {
                                            Log.d(TAG, "getUsersWhoLikedMe: Found " + usersWhoLikedMe.size() + " users");
                                            listener.onSuccess(usersWhoLikedMe);
                                        }
                                    }
                                    return;
                                }

                                // Ánh xạ thủ công để xử lý dữ liệu không đầy đủ
                                User user = new User();
                                user.setUid(userId);
                                user.setName(userSnapshot.child("name").getValue(String.class));
                                user.setEmail(userSnapshot.child("email").getValue(String.class));
                                user.setGender(userSnapshot.child("gender").getValue(String.class));
                                user.setPreferredGender(userSnapshot.child("preferredGender").getValue(String.class));
                                user.setDateOfBirth(userSnapshot.child("dateOfBirth").getValue(String.class));
                                user.setReligion(userSnapshot.child("religion").getValue(String.class));
                                user.setResidence(userSnapshot.child("residence").getValue(String.class));
                                user.setEducationLevel(userSnapshot.child("educationLevel").getValue(String.class));
                                user.setOccupation(userSnapshot.child("occupation").getValue(String.class));
                                user.setDescription(userSnapshot.child("description").getValue(String.class));

                                // Ánh xạ danh sách photos
                                List<String> photos = new ArrayList<>();
                                DataSnapshot photosSnapshot = userSnapshot.child("photos");
                                if (photosSnapshot.exists()) {
                                    for (DataSnapshot photoSnapshot : photosSnapshot.getChildren()) {
                                        String photo = photoSnapshot.getValue(String.class);
                                        if (photo != null) {
                                            photos.add(photo);
                                        }
                                    }
                                }
                                user.setPhotos(photos);

                                // Ánh xạ locationEnabled, latitude, longitude
                                user.setLocationEnabled(userSnapshot.child("locationEnabled").getValue(Boolean.class) != null ?
                                        userSnapshot.child("locationEnabled").getValue(Boolean.class) : false);
                                user.setLatitude(userSnapshot.child("latitude").getValue(Double.class) != null ?
                                        userSnapshot.child("latitude").getValue(Double.class) : 0.0);
                                user.setLongitude(userSnapshot.child("longitude").getValue(Double.class) != null ?
                                        userSnapshot.child("longitude").getValue(Double.class) : 0.0);

                                usersWhoLikedMe.add(user);
                                userIds.add(userId);
                                Log.d(TAG, "getUsersWhoLikedMe: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + userId + ")");

                                if (userIds.size() == snapshot.getChildrenCount()) {
                                    if (usersWhoLikedMe.isEmpty()) {
                                        Log.d(TAG, "getUsersWhoLikedMe: No users found after processing");
                                        listener.onEmpty();
                                    } else {
                                        Log.d(TAG, "getUsersWhoLikedMe: Found " + usersWhoLikedMe.size() + " users");
                                        listener.onSuccess(usersWhoLikedMe);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "getUsersWhoLikedMe: Error fetching user data: " + error.getMessage());
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getUsersWhoLikedMe: Error: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }

    public void getUsersILiked(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<User> usersILiked = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

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
                                    userIds.add(likedUserId);
                                }
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