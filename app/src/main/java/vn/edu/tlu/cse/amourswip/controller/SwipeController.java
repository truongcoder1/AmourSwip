package vn.edu.tlu.cse.amourswip.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.view.adapter.CardStackAdapter;
import vn.edu.tlu.cse.amourswip.view.fragment.SwipeFragment;

public class SwipeController {

    private static final String TAG = "SwipeController";

    private final SwipeFragment fragment;
    private final CardStackView cardStackView;
    private final View skipCircle;
    private final View likeCircle;
    private final ImageButton skipButton;
    private final ImageButton likeButton;
    private final TextView matchNotificationText;
    private final View matchNotificationLayout;
    private final NavController navController;
    private final DatabaseReference database;
    private final String currentUserId;
    private final List<User> userList;
    private final CardStackAdapter adapter;
    private final CardStackLayoutManager layoutManager;
    private User currentUser;
    private Set<String> matchedUserIds;

    public SwipeController(SwipeFragment fragment, CardStackView cardStackView, View skipCircle, View likeCircle,
                           ImageButton skipButton, ImageButton likeButton, TextView matchNotificationText,
                           View matchNotificationLayout, NavController navController,
                           List<User> userList, CardStackAdapter adapter) {
        this.fragment = fragment;
        this.cardStackView = cardStackView;
        this.skipCircle = skipCircle;
        this.likeCircle = likeCircle;
        this.skipButton = skipButton;
        this.likeButton = likeButton;
        this.matchNotificationText = matchNotificationText;
        this.matchNotificationLayout = matchNotificationLayout;
        this.navController = navController;
        this.database = FirebaseDatabase.getInstance().getReference();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.userList = userList;
        this.adapter = adapter;
        this.layoutManager = new CardStackLayoutManager(fragment.getContext());
        this.matchedUserIds = new HashSet<>();
        initializeCardStack();
    }

    private void initializeCardStack() {
        SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeAnimationSetting);

        skipButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
            fragment.showSkipAnimation(); // Thay đổi để gọi showSkipAnimation
        });

        likeButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
            fragment.showLikeAnimationOnButton(likeButton);
        });

        loadCurrentUser();
    }

    public void loadUsers() {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot load users");
            return;
        }

        // Lấy danh sách người đã match trước
        database.child("matches").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUserIds.clear();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    matchedUserIds.add(matchedUserId);
                }
                Log.d(TAG, "Matched users: " + matchedUserIds);

                // Tải danh sách người dùng và loại bỏ những người đã match
                database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        Log.d(TAG, "Loading users from Firebase...");
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null && !user.getUid().equals(currentUserId) && !matchedUserIds.contains(user.getUid())) {
                                String preferredGender = currentUser.getPreferredGender();
                                String userGender = user.getGender();
                                Log.d(TAG, "User: " + user.getName() + ", Gender: " + userGender + ", Preferred Gender: " + preferredGender);
                                if (preferredGender != null && userGender != null && preferredGender.equals(userGender)) {
                                    userList.add(user);
                                    Log.d(TAG, "User added (preferred gender match): " + user.getName());
                                } else {
                                    userList.add(user);
                                    Log.d(TAG, "User added (no preferred gender match): " + user.getName());
                                }
                            } else {
                                Log.d(TAG, "User skipped: " + (user == null ? "null user" : (matchedUserIds.contains(user.getUid()) ? "already matched" : "current user")));
                            }
                        }
                        if (userList.isEmpty()) {
                            Log.w(TAG, "User list is empty after loading");
                            fragment.showError("Không có người dùng nào để hiển thị");
                        } else {
                            Log.d(TAG, "User list loaded with " + userList.size() + " users");
                            adapter.notifyDataSetChanged();
                            cardStackView.scheduleLayoutAnimation();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading users: " + error.getMessage());
                        fragment.showError("Lỗi tải danh sách người dùng: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading matches: " + error.getMessage());
                fragment.showError("Lỗi tải danh sách match: " + error.getMessage());
            }
        });
    }

    private void loadCurrentUser() {
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    Log.d(TAG, "Current user loaded: " + currentUser.getName() + ", Preferred Gender: " + currentUser.getPreferredGender());
                    if (currentUser.isLocationEnabled()) {
                        adapter.setCurrentUserLocation(currentUser.getLatitude(), currentUser.getLongitude());
                    } else {
                        adapter.setCurrentUserLocation(0.0, 0.0);
                    }
                    loadUsers();
                } else {
                    Log.e(TAG, "Current user is null");
                    fragment.showError("Không tìm thấy thông tin người dùng hiện tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading current user: " + error.getMessage());
                fragment.showError("Lỗi tải thông tin người dùng: " + error.getMessage());
            }
        });
    }

    public void handleCardSwiped(Direction direction) {
        if (userList.isEmpty()) {
            Log.w(TAG, "User list is empty during swipe");
            return;
        }

        int topPosition = layoutManager.getTopPosition();
        int index = topPosition;
        if (index < 0 || index >= userList.size()) {
            Log.e(TAG, "Invalid index: " + index + ", topPosition: " + topPosition + ", userList size: " + userList.size());
            if (topPosition <= 0 && !userList.isEmpty()) {
                Log.d(TAG, "handleCardSwiped: topPosition is " + topPosition + ", resetting CardStackView");
                adapter.notifyDataSetChanged();
                cardStackView.scheduleLayoutAnimation();
                return;
            }
            return;
        }

        User otherUser = userList.get(index);
        if (direction == Direction.Right) {
            likeUser(otherUser);
            fragment.showLikeAnimation();
        } else if (direction == Direction.Left) {
            fragment.showSkipAnimation();
        }

        // Kiểm tra match ngay sau khi vuốt
        checkForMatch(otherUser);
    }

    private void likeUser(User otherUser) {
        Log.d(TAG, "Liking user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully liked user: " + otherUser.getName());
                    } else {
                        Log.e(TAG, "Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    private void checkForMatch(User otherUser) {
        Log.d(TAG, "Checking for match with user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");
        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "Mutual like detected, match successful!");
                            String chatId = currentUserId.compareTo(otherUser.getUid()) < 0
                                    ? currentUserId + "_" + otherUser.getUid()
                                    : otherUser.getUid() + "_" + currentUserId;

                            database.child("matches").child(currentUserId).child(otherUser.getUid()).setValue(true);
                            database.child("matches").child(otherUser.getUid()).child(currentUserId).setValue(true);

                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                            database.child("chats").child(chatId).child("participants").child(otherUser.getUid()).setValue(true);

                            String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                            Log.d(TAG, "Match successful with user: " + matchedUserName);
                            showMatchDialog(matchedUserName, chatId, otherUser);
                        } else {
                            Log.d(TAG, "No mutual like found with user: " + otherUser.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking match: " + error.getMessage());
                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                    }
                });
    }

    private void showMatchDialog(String matchedUserName, String chatId, User otherUser) {
        Log.d(TAG, "showMatchDialog: Attempting to show match dialog for user: " + matchedUserName);
        try {
            Dialog matchDialog = new Dialog(fragment.getContext());
            matchDialog.setContentView(R.layout.match_dialog);

            // Tìm các view trong dialog
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

            // Cập nhật tiêu đề
            String message = "Bạn và " + matchedUserName + " đã match thành công!";
            matchTitle.setText(message);

            // Tải hình ảnh của người dùng hiện tại (nếu có)
            if (currentUser != null && currentUser.getPhotos() != null && !currentUser.getPhotos().isEmpty()) {
                Glide.with(fragment.getContext())
                        .load(currentUser.getPhotos().get(0))
                        .placeholder(R.drawable.gai1)
                        .error(R.drawable.gai1)
                        .into(currentUserImage);
            } else {
                currentUserImage.setImageResource(R.drawable.gai1);
            }

            // Tải hình ảnh của người dùng được match (nếu có)
            if (otherUser != null && otherUser.getPhotos() != null && !otherUser.getPhotos().isEmpty()) {
                Glide.with(fragment.getContext())
                        .load(otherUser.getPhotos().get(0))
                        .placeholder(R.drawable.gai2)
                        .error(R.drawable.gai2)
                        .into(otherUserImage);
            } else {
                otherUserImage.setImageResource(R.drawable.gai2);
            }

            // Xử lý sự kiện nút "Nhắn tin"
            sendMessageButton.setOnClickListener(v -> {
                Log.d(TAG, "Send message button clicked, navigating to chat with chatId: " + chatId);
                matchDialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("chatId", chatId);
                try {
                    navController.navigate(R.id.action_swipeFragment_to_listChatFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to chat: " + e.getMessage());
                    fragment.showError("Lỗi điều hướng: " + e.getMessage());
                }
            });

            // Xử lý sự kiện nút "Tiếp tục vuốt"
            keepSwipingButton.setOnClickListener(v -> {
                Log.d(TAG, "Keep swiping button clicked, dismissing dialog");
                matchedUserIds.add(otherUser.getUid());
                userList.remove(otherUser);
                adapter.notifyDataSetChanged();
                cardStackView.scheduleLayoutAnimation();
                matchDialog.dismiss();
            });

            // Hiển thị dialog
            Log.d(TAG, "showMatchDialog: Showing dialog");
            matchDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing match dialog: " + e.getMessage(), e);
            fragment.showError("Lỗi hiển thị dialog match: " + e.getMessage());
        }
    }
}