package vn.edu.tlu.cse.amourswip.controller;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
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
import java.util.List;
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
        });

        likeButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });

        loadCurrentUser();
    }

    public void loadUsers() {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot load users");
            return;
        }

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d(TAG, "Loading users from Firebase...");
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !user.getUid().equals(currentUserId)) {
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
                        Log.d(TAG, "User skipped: " + (user == null ? "null user" : "current user"));
                    }
                }
                if (userList.isEmpty()) {
                    Log.w(TAG, "User list is empty after loading");
                    fragment.showError("Không có người dùng nào để hiển thị");
                } else {
                    Log.d(TAG, "User list loaded with " + userList.size() + " users");
                    adapter.notifyDataSetChanged();
                    cardStackView.scheduleLayoutAnimation(); // Làm mới CardStackView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading users: " + error.getMessage());
                fragment.showError("Lỗi tải danh sách người dùng: " + error.getMessage());
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
                    // Truyền vị trí của người dùng hiện tại vào adapter
                    if (currentUser.isLocationEnabled()) {
                        adapter.setCurrentUserLocation(currentUser.getLatitude(), currentUser.getLongitude());
                    } else {
                        adapter.setCurrentUserLocation(0.0, 0.0); // Vị trí mặc định nếu không bật định vị
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
        int index = topPosition - 1;
        if (index < 0 || index >= userList.size()) {
            Log.e(TAG, "Invalid index: " + index + ", topPosition: " + topPosition + ", userList size: " + userList.size());
            return;
        }

        User otherUser = userList.get(index);
        if (direction == Direction.Right) {
            likeUser(otherUser);
            fragment.showLikeAnimation();
        } else if (direction == Direction.Left) {
            fragment.showSkipAnimation();
        }
    }

    private void likeUser(User otherUser) {
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (checkMatchCondition(currentUser, otherUser)) {
                            checkForMatch(otherUser);
                        } else {
                            Log.d(TAG, "Like recorded but condition for match not met");
                            fragment.showError("Lượt thích đã được ghi lại, nhưng không thỏa mãn điều kiện để match!");
                        }
                    } else {
                        Log.e(TAG, "Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    private boolean checkMatchCondition(User currentUser, User otherUser) {
        double distance = calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                otherUser.getLatitude(), otherUser.getLongitude()
        );
        Log.d(TAG, "Distance between users: " + distance + " km");
        return distance < 5.0;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void checkForMatch(User otherUser) {
        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String chatId = currentUserId.compareTo(otherUser.getUid()) < 0
                                    ? currentUserId + "_" + otherUser.getUid()
                                    : otherUser.getUid() + "_" + currentUserId;

                            database.child("matches").child(currentUserId).child(otherUser.getUid()).setValue(true);
                            database.child("matches").child(otherUser.getUid()).child(currentUserId).setValue(true);

                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                            database.child("chats").child(chatId).child("participants").child(otherUser.getUid()).setValue(true);

                            // Hiển thị thông báo match thành công bằng animation
                            String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                            Log.d(TAG, "Match successful with user: " + matchedUserName);
                            onMatchSuccess(matchedUserName, chatId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking match: " + error.getMessage());
                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                    }
                });
    }

    private void onMatchSuccess(String matchedUserName, String chatId) {
        if (matchNotificationText == null || matchNotificationLayout == null) {
            fragment.showError("Lỗi: Không tìm thấy view thông báo match. matchNotificationText: " +
                    (matchNotificationText == null ? "null" : "not null") +
                    ", matchNotificationLayout: " +
                    (matchNotificationLayout == null ? "null" : "not null"));
            return;
        }

        // Cập nhật nội dung thông báo
        String message = "Bạn đã match với " + matchedUserName + "!";
        matchNotificationText.setText(message);

        // Hiển thị thông báo với animation trượt xuống
        Animation slideDown;
        try {
            slideDown = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.slide_down);
        } catch (Exception e) {
            Log.e(TAG, "Error loading slide down animation: " + e.getMessage());
            fragment.showError("Lỗi tải animation trượt xuống: " + e.getMessage());
            return;
        }
        matchNotificationLayout.setVisibility(View.VISIBLE);
        matchNotificationLayout.startAnimation(slideDown);

        // Ẩn thông báo sau 3 giây với animation trượt lên
        matchNotificationLayout.postDelayed(() -> {
            Animation slideUp;
            try {
                slideUp = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.slide_up);
            } catch (Exception e) {
                Log.e(TAG, "Error loading slide up animation: " + e.getMessage());
                fragment.showError("Lỗi tải animation trượt lên: " + e.getMessage());
                return;
            }
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    matchNotificationLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            matchNotificationLayout.startAnimation(slideUp);
        }, 3000); // 3 giây
    }
}