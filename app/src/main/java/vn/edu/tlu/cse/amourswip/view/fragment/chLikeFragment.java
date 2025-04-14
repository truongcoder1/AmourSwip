package vn.edu.tlu.cse.amourswip.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.controller.chLikeController;
import vn.edu.tlu.cse.amourswip.model.data.xUser;
import vn.edu.tlu.cse.amourswip.view.adapter.chUserGridAdapter;

public class chLikeFragment extends Fragment {

    private static final String TAG = "chLikeFragment";
    private static final String KEY_USERS_WHO_LIKED_ME = "usersWhoLikedMe";
    private static final String KEY_USERS_I_LIKED = "usersILiked";
    private static final String KEY_IS_LIKES_TAB_SELECTED = "isLikesTabSelected";
    private static final String KEY_MAX_DISTANCE = "maxDistance";
    private static final String KEY_MIN_AGE = "minAge";
    private static final String KEY_MAX_AGE = "maxAge";
    private static final String KEY_RESIDENCE_FILTER = "residenceFilter";
    private static final String KEY_FILTER_APPLIED = "filterApplied";

    private ImageButton likesTab;
    private ImageButton likedTab;
    private ImageButton filterButton;
    private TextView likesLabel;
    private TextView likedLabel;
    private RecyclerView userRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private chUserGridAdapter userAdapter;
    private List<xUser> userList;
    private List<xUser> usersWhoLikedMe;
    private List<xUser> usersILiked;
    private NavController navController;
    private chLikeController controller;
    private double currentLatitude;
    private double currentLongitude;
    private boolean isLoading;
    private boolean isFilterApplied;
    private boolean isLikesTabSelected; // Biến để theo dõi trạng thái tab
    private DatabaseReference matchNotificationsRef; // Thêm biến để lắng nghe thông báo match
    private ValueEventListener matchListener; // Listener cho thông báo match
    private String currentUserId; // ID của người dùng hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_like, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        matchNotificationsRef = FirebaseDatabase.getInstance().getReference("match_notifications").child(currentUserId);

        likesTab = view.findViewById(R.id.likes_tab);
        likedTab = view.findViewById(R.id.liked_tab);
        filterButton = view.findViewById(R.id.filter_button);
        likesLabel = view.findViewById(R.id.likes_label);
        likedLabel = view.findViewById(R.id.liked_label);
        userRecyclerView = view.findViewById(R.id.user_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        userList = new ArrayList<>();
        usersWhoLikedMe = new ArrayList<>();
        usersILiked = new ArrayList<>();
        userAdapter = new chUserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
        userRecyclerView.setAdapter(userAdapter);
        userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        userRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true;
                    controller.loadMoreUsers();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearFilter();
            controller.onLikesTabClicked();
            controller.onLikedTabClicked();
            swipeRefreshLayout.setRefreshing(false);
        });

        filterButton.setOnClickListener(v -> showFilterDialog());

        controller = new chLikeController(this);

        if (savedInstanceState != null) {
            userList = savedInstanceState.getParcelableArrayList(KEY_USERS_I_LIKED);
            if (userList == null) {
                userList = savedInstanceState.getParcelableArrayList(KEY_USERS_WHO_LIKED_ME);
            }
            if (userList != null) {
                userAdapter.updateList(userList);
            }
            isFilterApplied = savedInstanceState.getBoolean(KEY_FILTER_APPLIED, false);
            isLikesTabSelected = savedInstanceState.getBoolean(KEY_IS_LIKES_TAB_SELECTED, true);
            Log.d("chLikeFragment", "Restored isFilterApplied: " + isFilterApplied);
            controller.applyFilter(
                    savedInstanceState.getDouble(KEY_MAX_DISTANCE, Double.MAX_VALUE),
                    savedInstanceState.getInt(KEY_MIN_AGE, 0),
                    savedInstanceState.getInt(KEY_MAX_AGE, Integer.MAX_VALUE),
                    savedInstanceState.getString(KEY_RESIDENCE_FILTER)
            );
            if (isLikesTabSelected) {
                controller.onLikesTabClicked();
            } else {
                controller.onLikedTabClicked();
            }
        } else {
            isLikesTabSelected = true; // Giá trị mặc định khi khởi tạo
        }

        likesTab.setOnClickListener(v -> controller.onLikesTabClicked());
        likedTab.setOnClickListener(v -> controller.onLikedTabClicked());

        updateTabSelection(isLikesTabSelected);

        // Lắng nghe thông báo match từ Firebase
        setupMatchListener();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_USERS_WHO_LIKED_ME, new ArrayList<>(controller.getUsersWhoLikedMe()));
        outState.putParcelableArrayList(KEY_USERS_I_LIKED, new ArrayList<>(controller.getUsersILiked()));
        outState.putBoolean(KEY_IS_LIKES_TAB_SELECTED, controller.isLikesTabSelected());
        outState.putDouble(KEY_MAX_DISTANCE, controller.getMaxDistance());
        outState.putInt(KEY_MIN_AGE, controller.getMinAge());
        outState.putInt(KEY_MAX_AGE, controller.getMaxAge());
        outState.putString(KEY_RESIDENCE_FILTER, controller.getResidenceFilter());
        outState.putBoolean(KEY_FILTER_APPLIED, isFilterApplied);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy lắng nghe sự kiện match khi fragment bị hủy
        if (matchNotificationsRef != null && matchListener != null) {
            matchNotificationsRef.removeEventListener(matchListener);
        }
    }

    private void setupMatchListener() {
        matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchId = matchSnapshot.getKey();
                    String otherUserId = matchSnapshot.child("otherUserId").getValue(String.class);
                    String chatId = matchSnapshot.child("chatId").getValue(String.class);

                    if (otherUserId != null && chatId != null) {
                        // Lấy thông tin người dùng match để hiển thị dialog
                        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
                        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                xUser otherUser = userSnapshot.getValue(xUser.class);
                                if (otherUser != null) {
                                    String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                                    showMatchDialog(matchedUserName, chatId, otherUser);
                                }
                                // Xóa thông báo sau khi hiển thị
                                matchNotificationsRef.child(matchId).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error loading matched user: " + error.getMessage());
                                showError("Lỗi tải thông tin người dùng match: " + error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening for match notifications: " + error.getMessage());
                showError("Lỗi lắng nghe thông báo match: " + error.getMessage());
            }
        };
        matchNotificationsRef.addValueEventListener(matchListener);
    }

    private void showFilterDialog() {
        if (!isAdded() || getContext() == null) {
            Log.e("chLikeFragment", "Cannot show filter dialog: Fragment is not attached to an Activity");
            return;
        }
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_filter);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        EditText distanceFilter = dialog.findViewById(R.id.distance_filter);
        EditText ageMinFilter = dialog.findViewById(R.id.age_min_filter);
        EditText ageMaxFilter = dialog.findViewById(R.id.age_max_filter);
        EditText residenceFilter = dialog.findViewById(R.id.residence_filter);
        Button applyButton = dialog.findViewById(R.id.apply_filter_button);
        Button clearFilterButton = dialog.findViewById(R.id.clear_filter_button);

        applyButton.setOnClickListener(v -> {
            double maxDistance = Double.MAX_VALUE;
            int minAge = 0;
            int maxAge = Integer.MAX_VALUE;
            String residence = residenceFilter.getText().toString().trim();

            try {
                String distanceStr = distanceFilter.getText().toString().trim();
                if (!distanceStr.isEmpty()) {
                    maxDistance = Double.parseDouble(distanceStr);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Khoảng cách không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String minAgeStr = ageMinFilter.getText().toString().trim();
                if (!minAgeStr.isEmpty()) {
                    minAge = Integer.parseInt(minAgeStr);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tuổi tối thiểu không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String maxAgeStr = ageMaxFilter.getText().toString().trim();
                if (!maxAgeStr.isEmpty()) {
                    maxAge = Integer.parseInt(maxAgeStr);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tuổi tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            controller.applyFilter(maxDistance, minAge, maxAge, residence);
            isFilterApplied = true;
            Log.d("chLikeFragment", "Applied filter - isFilterApplied: " + isFilterApplied);
            dialog.dismiss();
        });

        clearFilterButton.setOnClickListener(v -> {
            clearFilter();
            distanceFilter.setText("");
            ageMinFilter.setText("");
            ageMaxFilter.setText("");
            residenceFilter.setText("");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void clearFilter() {
        controller.applyFilter(Double.MAX_VALUE, 0, Integer.MAX_VALUE, null);
        isFilterApplied = false;
        Log.d("chLikeFragment", "Cleared filter - isFilterApplied: " + isFilterApplied);
    }

    public void setCurrentLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        userAdapter = new chUserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
        userRecyclerView.setAdapter(userAdapter);
    }

    public void updateUserList(List<xUser> users) {
        Log.d("chLikeFragment", "updateUserList: Updating with " + (users != null ? users.size() : 0) + " users");
        if (users == null) {
            users = new ArrayList<>(); // Đảm bảo danh sách không null
        }

        // Sử dụng DiffUtil để tối ưu hóa cập nhật danh sách
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(userList, users));
        userList.clear();
        userList.addAll(users);

        // Cập nhật cả hai danh sách để đồng bộ
        if (isLikesTabSelected) {
            usersWhoLikedMe.clear();
            usersWhoLikedMe.addAll(users);
            // Đồng bộ danh sách usersILiked với dữ liệu từ controller
            usersILiked.clear();
            usersILiked.addAll(controller.getUsersILiked());
        } else {
            usersILiked.clear();
            usersILiked.addAll(users);
            // Đồng bộ danh sách usersWhoLikedMe với dữ liệu từ controller
            usersWhoLikedMe.clear();
            usersWhoLikedMe.addAll(controller.getUsersWhoLikedMe());
        }

        diffResult.dispatchUpdatesTo(userAdapter);
        isLoading = false;
        if (users.isEmpty()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Không có người dùng nào để hiển thị", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setActionButtons(boolean show, Consumer<xUser> onLikeClicked, Consumer<xUser> onDislikeClicked) {
        Log.d("chLikeFragment", "setActionButtons: show=" + show);
        userAdapter.setShowActionButtons(show, onLikeClicked, onDislikeClicked);
    }

    public void updateTabSelection(boolean isLikesTab) {
        Log.d("chLikeFragment", "updateTabSelection: isLikesTab=" + isLikesTab);
        this.isLikesTabSelected = isLikesTab; // Cập nhật trạng thái tab
        if (isLikesTab) {
            // Tab "Lượt thích" hiển thị danh sách người đã thích bạn
            likesTab.animate().alpha(1f).setDuration(200).start();
            likedTab.animate().alpha(0.5f).setDuration(200).start();
            likesLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            likedLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            setActionButtons(true, controller::onLikeUser, controller::onDislikeUser); // Tab "Lượt thích" có nút X và Trái tim
            updateUserList(usersWhoLikedMe); // Cập nhật giao diện với danh sách đã lưu
        } else {
            // Tab "Đã thích" hiển thị danh sách người bạn đã thích
            likesTab.animate().alpha(0.5f).setDuration(200).start();
            likedTab.animate().alpha(1f).setDuration(200).start();
            likesLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            likedLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            setActionButtons(false, null, null); // Tab "Đã thích" không có nút X và Trái tim
            updateUserList(usersILiked); // Cập nhật giao diện với danh sách đã lưu
        }
    }

    public void showError(String error) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("chLikeFragment", "Cannot show error: Fragment is not attached to an Activity");
        }
    }

    public NavController getNavController() {
        return navController;
    }

    private void onUserClicked(xUser user) {
        controller.onUserClicked(user);
    }

    // Phương thức hiển thị dialog match
    public void showMatchDialog(String matchedUserName, String chatId, xUser otherUser) {
        Log.d(TAG, "showMatchDialog: Attempting to show match dialog for user: " + matchedUserName);
        try {
            Dialog matchDialog = new Dialog(getContext());
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

            // Lấy ảnh của người dùng hiện tại
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    xUser currentUser = snapshot.getValue(xUser.class);
                    if (currentUser != null && currentUser.getPhotos() != null && !currentUser.getPhotos().isEmpty()) {
                        Glide.with(getContext())
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
                    Log.e(TAG, "Error loading current user: " + error.getMessage());
                    currentUserImage.setImageResource(R.drawable.gai1);
                }
            });

            // Hiển thị ảnh của người được match
            if (otherUser != null && otherUser.getPhotos() != null && !otherUser.getPhotos().isEmpty()) {
                Glide.with(getContext())
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
                    navController.navigate(R.id.action_likeFragment_to_listChatFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to chat: " + e.getMessage());
                    showError("Lỗi điều hướng: " + e.getMessage());
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
            showError("Lỗi hiển thị dialog match: " + e.getMessage());
        }
    }

    // DiffUtil Callback để tối ưu hóa cập nhật danh sách
    private static class UserDiffCallback extends DiffUtil.Callback {
        private final List<xUser> oldList;
        private final List<xUser> newList;

        UserDiffCallback(List<xUser> oldList, List<xUser> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getUid().equals(newList.get(newItemPosition).getUid());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            xUser oldUser = oldList.get(oldItemPosition);
            xUser newUser = newList.get(newItemPosition);

            // Kiểm tra null trước khi gọi equals()
            String oldName = oldUser.getName() != null ? oldUser.getName() : "";
            String newName = newUser.getName() != null ? newUser.getName() : "";
            List<String> oldPhotos = oldUser.getPhotos() != null ? oldUser.getPhotos() : new ArrayList<>();
            List<String> newPhotos = newUser.getPhotos() != null ? newUser.getPhotos() : new ArrayList<>();

            return oldName.equals(newName) &&
                    oldPhotos.equals(newPhotos) &&
                    oldUser.getLatitude() == newUser.getLatitude() &&
                    oldUser.getLongitude() == newUser.getLongitude();
        }
    }
}