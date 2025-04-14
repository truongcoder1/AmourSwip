package vn.edu.tlu.cse.amourswip.controller;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.xUser;
import vn.edu.tlu.cse.amourswip.model.repository.chLikeRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.chLikeFragment;

public class chLikeController {

    private static final String TAG = "LikeController";
    private static final int PAGE_SIZE = 10;
    private final chLikeFragment fragment;
    private final chLikeRepository repository;
    private List<xUser> usersWhoLikedMe;
    private List<xUser> usersILiked;
    private String lastUserIdWhoLikedMe;
    private String lastUserIdILiked;
    private boolean isLikesTabSelected;
    private double maxDistance = Double.MAX_VALUE;
    private int minAge = 0;
    private int maxAge = Integer.MAX_VALUE;
    private String residenceFilter;

    public chLikeController(chLikeFragment fragment) {
        this.fragment = fragment;
        this.repository = new chLikeRepository();
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.lastUserIdWhoLikedMe = null;
        this.lastUserIdILiked = null;
        this.isLikesTabSelected = true;

        // Load current user location
        repository.getCurrentUserLocation(new chLikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                fragment.setCurrentLocation(latitude, longitude);
                onLikesTabClicked();
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }
        });
    }

    public void onLikesTabClicked() {
        isLikesTabSelected = true;
        fragment.updateTabSelection(true);
        lastUserIdWhoLikedMe = null;
        usersWhoLikedMe.clear();
        loadUsersWhoLikedMe(); // Tab "Lượt thích" hiển thị danh sách người đã thích bạn
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        fragment.updateTabSelection(false);
        lastUserIdILiked = null;
        usersILiked.clear();
        loadUsersILiked(); // Tab "Đã thích" hiển thị danh sách người bạn đã thích
    }

    public void loadMoreUsers() {
        if (isLikesTabSelected) {
            loadUsersWhoLikedMe();
        } else {
            loadUsersILiked();
        }
    }

    private void loadUsersWhoLikedMe() {
        repository.getUsersWhoLikedMe(new chLikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<xUser> users) {
                usersWhoLikedMe.addAll(users);
                applyFilterToUsers(usersWhoLikedMe);
                fragment.updateUserList(usersWhoLikedMe);
                lastUserIdWhoLikedMe = usersWhoLikedMe.get(usersWhoLikedMe.size() - 1).getUid();
            }

            @Override
            public void onEmpty() {
                fragment.updateUserList(usersWhoLikedMe);
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdWhoLikedMe, PAGE_SIZE);
    }

    private void loadUsersILiked() {
        repository.getUsersILiked(new chLikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<xUser> users) {
                usersILiked.addAll(users);
                applyFilterToUsers(usersILiked);
                fragment.updateUserList(usersILiked);
                lastUserIdILiked = usersILiked.get(usersILiked.size() - 1).getUid();
            }

            @Override
            public void onEmpty() {
                fragment.updateUserList(usersILiked);
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdILiked, PAGE_SIZE);
    }

    public void onLikeUser(xUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        // Lưu hành động "thích" vào node likes của người dùng hiện tại
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onLikeUser: Successfully liked user: " + otherUser.getName());
                        // Thêm người dùng vào danh sách đã thích
                        usersILiked.add(otherUser);
                        usersWhoLikedMe.remove(otherUser);
                        fragment.updateUserList(usersWhoLikedMe);

                        // Lưu vào node likedBy của người được thích
                        database.child("likedBy").child(otherUser.getUid()).child(currentUserId).setValue(true)
                                .addOnCompleteListener(likedByTask -> {
                                    if (likedByTask.isSuccessful()) {
                                        Log.d(TAG, "onLikeUser: Successfully updated likedBy for user: " + otherUser.getUid());
                                    } else {
                                        Log.e(TAG, "onLikeUser: Error updating likedBy: " + likedByTask.getException().getMessage());
                                        fragment.showError("Lỗi khi cập nhật likedBy: " + likedByTask.getException().getMessage());
                                    }
                                });

                        // Kiểm tra ghép đôi
                        checkForMatch(otherUser);
                    } else {
                        Log.e(TAG, "onLikeUser: Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    public void onDislikeUser(xUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        database.child("likedBy").child(currentUserId).child(otherUser.getUid()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onDislikeUser: Successfully removed user from likedBy: " + otherUser.getName());
                        usersWhoLikedMe.remove(otherUser);
                        fragment.updateUserList(usersWhoLikedMe);
                    } else {
                        Log.e(TAG, "onDislikeUser: Error removing user from likedBy: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi bỏ thích: " + task.getException().getMessage());
                    }
                });
    }

    private void checkForMatch(xUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "checkForMatch: Mutual like detected, match successful!");
                            String chatId = currentUserId.compareTo(otherUser.getUid()) < 0
                                    ? currentUserId + "_" + otherUser.getUid()
                                    : otherUser.getUid() + "_" + currentUserId;

                            database.child("matches").child(currentUserId).child(otherUser.getUid()).setValue(true);
                            database.child("matches").child(otherUser.getUid()).child(currentUserId).setValue(true);

                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                            database.child("chats").child(chatId).child("participants").child(otherUser.getUid()).setValue(true);

                            String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                            Log.d(TAG, "Match successful with user: " + matchedUserName);
                            usersWhoLikedMe.remove(otherUser);
                            fragment.updateUserList(usersWhoLikedMe);
                        } else {
                            Log.d(TAG, "checkForMatch: No mutual like found with user: " + otherUser.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "checkForMatch: Error checking match: " + error.getMessage());
                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                    }
                });
    }

    public void applyFilter(double maxDistance, int minAge, int maxAge, String residenceFilter) {
        this.maxDistance = maxDistance;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.residenceFilter = residenceFilter != null && !residenceFilter.isEmpty() ? residenceFilter : null;

        applyFilterToUsers(isLikesTabSelected ? usersWhoLikedMe : usersILiked);
        fragment.updateUserList(isLikesTabSelected ? usersWhoLikedMe : usersILiked);
    }

    private void applyFilterToUsers(List<xUser> users) {
        List<xUser> filteredUsers = new ArrayList<>();
        for (xUser user : users) {
            // Lọc theo khoảng cách
            double distance = calculateDistance(user);
            if (distance > maxDistance) {
                continue;
            }

            // Lọc theo độ tuổi
            int age = calculateAge(user);
            if (age < minAge || age > maxAge) {
                continue;
            }

            // Lọc theo nơi cư trú
            if (residenceFilter != null && !residenceFilter.equals(user.getResidence())) {
                continue;
            }

            filteredUsers.add(user);
        }
        users.clear();
        users.addAll(filteredUsers);
    }

    private double calculateDistance(xUser user) {
        // Tính khoảng cách dựa trên latitude và longitude (giả định)
        return 0; // Thay bằng logic thực tế nếu cần
    }

    private int calculateAge(xUser user) {
        // Tính tuổi dựa trên dateOfBirth (giả định)
        return 25; // Thay bằng logic thực tế nếu cần
    }

    public void onUserClicked(xUser user) {
        // Điều hướng đến ProfileMyFriendActivity hoặc fragment tương ứng
        Bundle bundle = new Bundle();
        bundle.putString("friendId", user.getUid());
        fragment.getNavController().navigate(R.id.action_likeFragment_to_profileMyFriendActivity, bundle);
    }

    public List<xUser> getUsersWhoLikedMe() {
        return new ArrayList<>(usersWhoLikedMe);
    }

    public List<xUser> getUsersILiked() {
        return new ArrayList<>(usersILiked);
    }

    public boolean isLikesTabSelected() {
        return isLikesTabSelected;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getResidenceFilter() {
        return residenceFilter;
    }
}