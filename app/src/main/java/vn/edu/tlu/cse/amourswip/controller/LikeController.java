package vn.edu.tlu.cse.amourswip.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.model.repository.LikeRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.LikeFragment;
import android.location.Location;

public class LikeController {

    private static final String TAG = "LikeController";
    private static final int PAGE_SIZE = 10;

    private final WeakReference<LikeFragment> fragmentRef;
    private final LikeRepository likeRepository;
    private final DatabaseReference database;
    private final String currentUserId;
    private List<User> usersWhoLikedMe;
    private List<User> usersILiked;
    private List<User> filteredUsersWhoLikedMe;
    private List<User> filteredUsersILiked;
    private boolean isLikesTabSelected;
    private double currentLatitude;
    private double currentLongitude;
    private String lastUserIdWhoLikedMe;
    private String lastUserIdILiked;
    private double maxDistance = Double.MAX_VALUE;
    private int minAge = 0;
    private int maxAge = Integer.MAX_VALUE;
    private String residenceFilter = null;
    private Set<String> userIdsWhoLikedMe = new HashSet<>();
    private Set<String> userIdsILiked = new HashSet<>();
    private Set<String> matchedUserIds = new HashSet<>();

    public LikeController(LikeFragment fragment) {
        this.fragmentRef = new WeakReference<>(fragment);
        this.likeRepository = new LikeRepository();
        this.database = FirebaseDatabase.getInstance().getReference();
        this.currentUserId = likeRepository.getCurrentUserId();
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.filteredUsersWhoLikedMe = new ArrayList<>();
        this.filteredUsersILiked = new ArrayList<>();
        this.isLikesTabSelected = true;
        this.lastUserIdWhoLikedMe = null;
        this.lastUserIdILiked = null;
        loadMatchedUsers();
        loadCurrentUserLocation();
    }

    private LikeFragment getFragment() {
        return fragmentRef.get();
    }

    private void loadMatchedUsers() {
        database.child("matches").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUserIds.clear();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    matchedUserIds.add(matchedUserId);
                }
                Log.d(TAG, "loadMatchedUsers: Loaded " + matchedUserIds.size() + " matched users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadMatchedUsers: Error: " + error.getMessage());
                LikeFragment fragment = getFragment();
                if (fragment != null) {
                    fragment.showError("Lỗi tải danh sách match: " + error.getMessage());
                }
            }
        });
    }

    private void loadCurrentUserLocation() {
        likeRepository.getCurrentUserLocation(new LikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                LikeFragment fragment = getFragment();
                if (fragment != null) {
                    fragment.setCurrentLocation(latitude, longitude);
                    loadUsersILiked();
                    loadUsersWhoLikedMe();
                }
            }

            @Override
            public void onError(String error) {
                LikeFragment fragment = getFragment();
                if (fragment != null) {
                    fragment.showError(error);
                    currentLatitude = 0;
                    currentLongitude = 0;
                    fragment.setCurrentLocation(0, 0);
                    loadUsersILiked();
                    loadUsersWhoLikedMe();
                }
            }
        });
    }

    public void loadUsersWhoLikedMe() {
        likeRepository.getUsersWhoLikedMe(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<User> users) {
                Log.d(TAG, "loadUsersWhoLikedMe: Received " + users.size() + " users");
                if (lastUserIdWhoLikedMe == null) {
                    usersWhoLikedMe.clear();
                    userIdsWhoLikedMe.clear();
                }
                for (User user : users) {
                    if (user == null || user.getUid() == null) {
                        Log.e(TAG, "loadUsersWhoLikedMe: Invalid user data - user or UID is null");
                        continue;
                    }
                    Log.d(TAG, "loadUsersWhoLikedMe: Processing user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ")");
                    if (!matchedUserIds.contains(user.getUid()) && !userIdsWhoLikedMe.contains(user.getUid()) && !userIdsILiked.contains(user.getUid())) {
                        usersWhoLikedMe.add(user);
                        userIdsWhoLikedMe.add(user.getUid());
                        Log.d(TAG, "loadUsersWhoLikedMe: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ")");
                    } else {
                        Log.d(TAG, "loadUsersWhoLikedMe: Skipped user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ") due to already matched, in list, or already liked by you");
                    }
                }
                if (!users.isEmpty()) {
                    lastUserIdWhoLikedMe = users.get(users.size() - 1).getUid();
                } else {
                    Log.d(TAG, "loadUsersWhoLikedMe: No users to add after filtering conditions");
                }
                applyFilters();
                if (!isLikesTabSelected) {
                    LikeFragment fragment = getFragment();
                    if (fragment != null) {
                        Log.d(TAG, "loadUsersWhoLikedMe: Updating UI with " + filteredUsersWhoLikedMe.size() + " filtered users");
                        fragment.updateUserList(filteredUsersWhoLikedMe);
                        fragment.setActionButtons(true, LikeController.this::onLikeUser, LikeController.this::onDislikeUser);
                    }
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "loadUsersWhoLikedMe: No users found");
                if (lastUserIdWhoLikedMe == null && !isLikesTabSelected) {
                    LikeFragment fragment = getFragment();
                    if (fragment != null) {
                        fragment.updateUserList(new ArrayList<>());
                        fragment.setActionButtons(true, LikeController.this::onLikeUser, LikeController.this::onDislikeUser);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "loadUsersWhoLikedMe: Error: " + error);
                LikeFragment fragment = getFragment();
                if (fragment != null) {
                    fragment.showError(error);
                }
            }

            @Override
            public void onLoading() {
                Log.d(TAG, "loadUsersWhoLikedMe: Loading...");
            }
        }, lastUserIdWhoLikedMe, PAGE_SIZE);
    }

    public void loadUsersILiked() {
        likeRepository.getUsersILiked(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<User> users) {
                Log.d(TAG, "loadUsersILiked: Received " + users.size() + " users");
                if (lastUserIdILiked == null) {
                    usersILiked.clear();
                    userIdsILiked.clear();
                }
                for (User user : users) {
                    if (user == null || user.getUid() == null) {
                        Log.e(TAG, "loadUsersILiked: Invalid user data - user or UID is null");
                        continue;
                    }
                    Log.d(TAG, "loadUsersILiked: Processing user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ")");
                    if (!matchedUserIds.contains(user.getUid()) && !userIdsILiked.contains(user.getUid()) && !userIdsWhoLikedMe.contains(user.getUid())) {
                        usersILiked.add(user);
                        userIdsILiked.add(user.getUid());
                        Log.d(TAG, "loadUsersILiked: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ")");
                        listenForMatch(user);
                    } else {
                        Log.d(TAG, "loadUsersILiked: Skipped user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + user.getUid() + ") due to already matched, in list, or they liked you");
                    }
                }
                if (!users.isEmpty()) {
                    lastUserIdILiked = users.get(users.size() - 1).getUid();
                }
                applyFilters();
                if (isLikesTabSelected) {
                    LikeFragment fragment = getFragment();
                    if (fragment != null) {
                        Log.d(TAG, "loadUsersILiked: Updating UI with " + filteredUsersILiked.size() + " filtered users");
                        fragment.updateUserList(filteredUsersILiked);
                        fragment.setActionButtons(false, null, null);
                    }
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "loadUsersILiked: No users found");
                if (lastUserIdILiked == null && isLikesTabSelected) {
                    LikeFragment fragment = getFragment();
                    if (fragment != null) {
                        fragment.updateUserList(new ArrayList<>());
                        fragment.setActionButtons(false, null, null);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "loadUsersILiked: Error: " + error);
                LikeFragment fragment = getFragment();
                if (fragment != null) {
                    fragment.showError(error);
                }
            }

            @Override
            public void onLoading() {
                Log.d(TAG, "loadUsersILiked: Loading...");
            }
        }, lastUserIdILiked, PAGE_SIZE);
    }

    private void listenForMatch(User user) {
        database.child("likes").child(user.getUid()).child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "listenForMatch: Mutual like detected with user: " + user.getName());
                            String chatId = currentUserId.compareTo(user.getUid()) < 0
                                    ? currentUserId + "_" + user.getUid()
                                    : user.getUid() + "_" + currentUserId;

                            database.child("matches").child(currentUserId).child(user.getUid()).setValue(true);
                            database.child("matches").child(user.getUid()).child(currentUserId).setValue(true);

                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                            database.child("chats").child(chatId).child("participants").child(user.getUid()).setValue(true);

                            matchedUserIds.add(user.getUid());
                            usersILiked.remove(user);
                            userIdsILiked.remove(user.getUid());
                            usersWhoLikedMe.remove(user);
                            userIdsWhoLikedMe.remove(user.getUid());
                            applyFilters();
                            if (!isLikesTabSelected) {
                                LikeFragment fragment = getFragment();
                                if (fragment != null) {
                                    fragment.updateUserList(filteredUsersWhoLikedMe);
                                }
                            }

                            String matchedUserName = user.getName() != null ? user.getName() : "người dùng này";
                            showMatchDialog(matchedUserName, chatId, user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "listenForMatch: Error: " + error.getMessage());
                        LikeFragment fragment = getFragment();
                        if (fragment != null) {
                            fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                        }
                    }
                });
    }

    public void onLikeUser(User user) {
        LikeFragment fragment = getFragment();
        if (fragment == null) {
            Log.e(TAG, "onLikeUser: Fragment is null, cannot proceed");
            return;
        }
        Log.d(TAG, "onLikeUser: Liking user: " + user.getName() + " (uid: " + user.getUid() + ")");
        database.child("likes").child(currentUserId).child(user.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onLikeUser: Successfully liked user: " + user.getName());
                        database.child("likedBy").child(user.getUid()).child(currentUserId).setValue(true);
                        database.child("likes").child(user.getUid()).child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Log.d(TAG, "onLikeUser: Mutual like detected with user: " + user.getName());
                                            String chatId = currentUserId.compareTo(user.getUid()) < 0
                                                    ? currentUserId + "_" + user.getUid()
                                                    : user.getUid() + "_" + currentUserId;

                                            database.child("matches").child(currentUserId).child(user.getUid()).setValue(true);
                                            database.child("matches").child(user.getUid()).child(currentUserId).setValue(true);

                                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                                            database.child("chats").child(chatId).child("participants").child(user.getUid()).setValue(true);

                                            matchedUserIds.add(user.getUid());
                                            usersWhoLikedMe.remove(user);
                                            userIdsWhoLikedMe.remove(user.getUid());
                                            usersILiked.remove(user);
                                            userIdsILiked.remove(user.getUid());
                                            applyFilters();
                                            fragment.updateUserList(isLikesTabSelected ? filteredUsersILiked : filteredUsersWhoLikedMe);

                                            String matchedUserName = user.getName() != null ? user.getName() : "người dùng này";
                                            showMatchDialog(matchedUserName, chatId, user);
                                        } else {
                                            usersWhoLikedMe.remove(user);
                                            userIdsWhoLikedMe.remove(user.getUid());
                                            usersILiked.add(user);
                                            userIdsILiked.add(user.getUid());
                                            applyFilters();
                                            fragment.updateUserList(isLikesTabSelected ? filteredUsersILiked : filteredUsersWhoLikedMe);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "onLikeUser: Error checking match: " + error.getMessage());
                                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                                    }
                                });
                    } else {
                        Log.e(TAG, "onLikeUser: Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    public void onDislikeUser(User user) {
        LikeFragment fragment = getFragment();
        if (fragment == null) {
            Log.e(TAG, "onDislikeUser: Fragment is null, cannot proceed");
            return;
        }
        Log.d(TAG, "onDislikeUser: Disliking user: " + user.getName() + " (uid: " + user.getUid() + ")");

        database.child("likedBy").child(currentUserId).child(user.getUid()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        usersWhoLikedMe.remove(user);
                        userIdsWhoLikedMe.remove(user.getUid());

                        database.child("likes").child(currentUserId).child(user.getUid()).removeValue()
                                .addOnCompleteListener(likesTask -> {
                                    if (likesTask.isSuccessful()) {
                                        Log.d(TAG, "onDislikeUser: Removed user " + user.getName() + " from likes");
                                        usersILiked.remove(user);
                                        userIdsILiked.remove(user.getUid());
                                    } else {
                                        Log.e(TAG, "onDislikeUser: Error removing user from likes: " + likesTask.getException().getMessage());
                                        fragment.showError("Lỗi khi xóa khỏi danh sách đã thích: " + likesTask.getException().getMessage());
                                    }
                                    applyFilters();
                                    fragment.updateUserList(filteredUsersWhoLikedMe);
                                });
                    } else {
                        Log.e(TAG, "onDislikeUser: Error disliking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi từ chối: " + task.getException().getMessage());
                    }
                });
    }

    private void showMatchDialog(String matchedUserName, String chatId, User otherUser) {
        LikeFragment fragment = getFragment();
        if (fragment == null || !fragment.isAdded() || fragment.getContext() == null) {
            Log.e(TAG, "showMatchDialog: Fragment is not attached to an Activity, cannot show dialog");
            return;
        }

        Log.d(TAG, "showMatchDialog: Attempting to show match dialog for user: " + matchedUserName);
        try {
            Dialog matchDialog = new Dialog(fragment.getContext());
            matchDialog.setContentView(R.layout.match_dialog);

            TextView matchTitle = matchDialog.findViewById(R.id.match_title);
            ImageView currentUserImage = matchDialog.findViewById(R.id.current_user_image);
            ImageView otherUserImage = matchDialog.findViewById(R.id.other_user_image);
            Button sendMessageButton = matchDialog.findViewById(R.id.send_message_button);
            Button keepSwipingButton = matchDialog.findViewById(R.id.keep_swiping_button);

            if (matchTitle == null || currentUserImage == null || otherUserImage == null ||
                    sendMessageButton == null || keepSwipingButton == null) {
                Log.e(TAG, "showMatchDialog: One or more views in match_dialog.xml are null");
                return;
            }

            String message = "Bạn và " + matchedUserName + " đã match thành công!";
            matchTitle.setText(message);

            database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User currentUser = snapshot.getValue(User.class);
                    if (currentUser != null && currentUser.getPhotos() != null && !currentUser.getPhotos().isEmpty()) {
                        Glide.with(fragment.getContext())
                                .load(currentUser.getPhotos().get(0))
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(currentUserImage);
                    } else {
                        currentUserImage.setImageResource(R.drawable.gai1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "showMatchDialog: Error loading current user: " + error.getMessage());
                    currentUserImage.setImageResource(R.drawable.gai1);
                }
            });

            if (otherUser != null && otherUser.getPhotos() != null && !otherUser.getPhotos().isEmpty()) {
                Glide.with(fragment.getContext())
                        .load(otherUser.getPhotos().get(0))
                        .placeholder(R.drawable.gai2)
                        .error(R.drawable.gai2)
                        .into(otherUserImage);
            } else {
                otherUserImage.setImageResource(R.drawable.gai2);
            }

            sendMessageButton.setOnClickListener(v -> {
                Log.d(TAG, "Send message button clicked, navigating to chat with chatId: " + chatId);
                matchDialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("chatId", chatId);
                try {
                    fragment.getNavController().navigate(R.id.action_likeFragment_to_listChatFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to chat: " + e.getMessage());
                    fragment.showError("Lỗi điều hướng: " + e.getMessage());
                }
            });

            keepSwipingButton.setOnClickListener(v -> {
                Log.d(TAG, "Keep swiping button clicked, dismissing dialog");
                matchDialog.dismiss();
            });

            Log.d(TAG, "showMatchDialog: Showing dialog");
            matchDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing match dialog: " + e.getMessage(), e);
            fragment.showError("Lỗi hiển thị dialog match: " + e.getMessage());
        }
    }

    public void loadMoreUsers() {
        if (isLikesTabSelected) {
            loadUsersILiked();
        } else {
            loadUsersWhoLikedMe();
        }
    }

    public void onLikesTabClicked() {
        isLikesTabSelected = true;
        lastUserIdILiked = null;
        loadUsersILiked();
        LikeFragment fragment = getFragment();
        if (fragment != null) {
            fragment.updateTabSelection(true);
        }
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        lastUserIdWhoLikedMe = null;
        loadUsersWhoLikedMe();
        LikeFragment fragment = getFragment();
        if (fragment != null) {
            fragment.updateTabSelection(false);
        }
    }

    public void onUserClicked(User user) {
        LikeFragment fragment = getFragment();
        if (fragment == null) {
            Log.e(TAG, "onUserClicked: Fragment is null, cannot proceed");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("friendId", user.getUid());
        fragment.getNavController().navigate(R.id.action_likeFragment_to_profileMyFriendActivity, bundle);
    }

    public void applyFilter(double maxDistance, int minAge, int maxAge, String residence) {
        this.maxDistance = maxDistance;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.residenceFilter = residence != null && !residence.isEmpty() ? residence.toLowerCase() : null;
        applyFilters();
        LikeFragment fragment = getFragment();
        if (fragment != null) {
            fragment.updateUserList(isLikesTabSelected ? filteredUsersILiked : filteredUsersWhoLikedMe);
        }
    }

    private void applyFilters() {
        Log.d(TAG, "applyFilters: Before filtering - usersWhoLikedMe: " + usersWhoLikedMe.size() + ", usersILiked: " + usersILiked.size());
        filteredUsersWhoLikedMe = filterUsers(usersWhoLikedMe);
        filteredUsersILiked = filterUsers(usersILiked);
        Log.d(TAG, "applyFilters: After filtering - filteredUsersWhoLikedMe: " + filteredUsersWhoLikedMe.size() + ", filteredUsersILiked: " + filteredUsersILiked.size());
        if (filteredUsersILiked.isEmpty() && isLikesTabSelected) {
            Log.d(TAG, "applyFilters: No users to display in 'Đã thích' tab after filtering");
        }
        if (filteredUsersWhoLikedMe.isEmpty() && !isLikesTabSelected) {
            Log.d(TAG, "applyFilters: No users to display in 'Lượt thích' tab after filtering");
        }
    }

    private List<User> filterUsers(List<User> users) {
        List<User> filteredList = new ArrayList<>(users);
        filteredList = filteredList.stream()
                .filter(user -> {
                    double distance = calculateDistance(user.getLatitude(), user.getLongitude());
                    if (distance > maxDistance) {
                        Log.d(TAG, "filterUsers: User " + user.getName() + " filtered out due to distance: " + distance + " > " + maxDistance);
                        return false;
                    }
                    int age = user.getAge();
                    if (age == 0) {
                        Log.w(TAG, "filterUsers: User " + user.getName() + " has invalid age (0), possibly due to missing or incorrect dateOfBirth: " + user.getDateOfBirth());
                        return true;
                    }
                    if (age < minAge || age > maxAge) {
                        Log.d(TAG, "filterUsers: User " + user.getName() + " filtered out due to age: " + age + " not in range [" + minAge + ", " + maxAge + "]");
                        return false;
                    }
                    if (residenceFilter != null && !residenceFilter.isEmpty()) {
                        String residence = user.getResidence() != null ? user.getResidence().toLowerCase() : "";
                        if (!residence.contains(residenceFilter.toLowerCase())) {
                            Log.d(TAG, "filterUsers: User " + user.getName() + " filtered out due to residence: " + residence + " does not contain " + residenceFilter);
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        return filteredList;
    }

    private double calculateDistance(double latitude, double longitude) {
        if (currentLatitude == 0 || currentLongitude == 0) {
            Log.w(TAG, "calculateDistance: Current location not available, skipping distance filter");
            return 0;
        }

        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        Location userLocation = new Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        float distanceInMeters = currentLocation.distanceTo(userLocation);
        return distanceInMeters / 1000;
    }

    public List<User> getUsersWhoLikedMe() {
        return usersWhoLikedMe;
    }

    public List<User> getUsersILiked() {
        return usersILiked;
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