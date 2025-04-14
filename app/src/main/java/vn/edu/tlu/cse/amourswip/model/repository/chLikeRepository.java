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
import android.util.Log;
import vn.edu.tlu.cse.amourswip.model.data.xUser;

public class chLikeRepository {

    private static final String TAG = "chLikeRepository";
    private final DatabaseReference database;
    private final String currentUserId;
    private long processedUsers;
    private long totalUsers;

    public chLikeRepository() {
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
                        Log.d(TAG, "getCurrentUserLocation: Found location - latitude: " + latitude + ", longitude: " + longitude);
                        locationListener.onSuccess(latitude, longitude);
                    } else {
                        Log.w(TAG, "getCurrentUserLocation: Latitude or longitude is null");
                        locationListener.onError("Không tìm thấy tọa độ của bạn");
                    }
                } else {
                    Log.w(TAG, "getCurrentUserLocation: User data not found");
                    locationListener.onError("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getCurrentUserLocation: Error: " + error.getMessage());
                locationListener.onError(error.getMessage());
            }
        });
    }

    public void getUsersWhoLikedMe(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<xUser> usersWhoLikedMe = new ArrayList<>();
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

                Log.d(TAG, "getUsersWhoLikedMe: Found " + snapshot.getChildrenCount() + " users in likedBy");
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
                                xUser user = new xUser();
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
        List<xUser> usersILiked = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

        Query query = database.child("likes").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersILiked: No users found in likes");
                    listener.onEmpty();
                    return;
                }

                Log.d(TAG, "getUsersILiked: Found " + snapshot.getChildrenCount() + " users in likes");
                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserId = likeSnapshot.getKey();
                    if (likedUserId != null && !userIds.contains(likedUserId)) {
                        database.child("users").child(likedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (!userSnapshot.exists()) {
                                    Log.e(TAG, "getUsersILiked: User data not found for uid: " + likedUserId);
                                    userIds.add(likedUserId);
                                    if (userIds.size() == snapshot.getChildrenCount()) {
                                        if (usersILiked.isEmpty()) {
                                            Log.d(TAG, "getUsersILiked: No users found after processing");
                                            listener.onEmpty();
                                        } else {
                                            Log.d(TAG, "getUsersILiked: Found " + usersILiked.size() + " users");
                                            listener.onSuccess(usersILiked);
                                        }
                                    }
                                    return;
                                }

                                xUser user = userSnapshot.getValue(xUser.class);
                                if (user != null) {
                                    user.setUid(likedUserId);
                                    usersILiked.add(user);
                                    userIds.add(likedUserId);
                                    Log.d(TAG, "getUsersILiked: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + likedUserId + ")");
                                } else {
                                    Log.e(TAG, "getUsersILiked: Failed to parse user data for uid: " + likedUserId);
                                }

                                if (userIds.size() == snapshot.getChildrenCount()) {
                                    if (usersILiked.isEmpty()) {
                                        Log.d(TAG, "getUsersILiked: No users found after processing");
                                        listener.onEmpty();
                                    } else {
                                        Log.d(TAG, "getUsersILiked: Found " + usersILiked.size() + " users");
                                        listener.onSuccess(usersILiked);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "getUsersILiked: Error fetching user data: " + error.getMessage());
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getUsersILiked: Error: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(List<xUser> users);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }

    public interface OnLocationListener {
        void onSuccess(double latitude, double longitude);
        void onError(String error);
    }
}