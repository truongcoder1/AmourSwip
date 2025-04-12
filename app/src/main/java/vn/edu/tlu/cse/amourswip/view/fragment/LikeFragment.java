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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.controller.LikeController;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.view.adapter.UserGridAdapter;

public class LikeFragment extends Fragment {

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
    private UserGridAdapter userAdapter;
    private List<User> userList;
    private NavController navController;
    private LikeController controller;
    private double currentLatitude;
    private double currentLongitude;
    private boolean isLoading;
    private boolean isFilterApplied;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_like, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        likesTab = view.findViewById(R.id.likes_tab);
        likedTab = view.findViewById(R.id.liked_tab);
        filterButton = view.findViewById(R.id.filter_button);
        likesLabel = view.findViewById(R.id.likes_label);
        likedLabel = view.findViewById(R.id.liked_label);
        userRecyclerView = view.findViewById(R.id.user_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        userList = new ArrayList<>();
        userAdapter = new UserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
        userRecyclerView.setAdapter(userAdapter);
        userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Thêm listener để tải thêm dữ liệu khi cuộn đến cuối
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

        // Thêm listener để làm mới danh sách và hủy bộ lọc
        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearFilter(); // Hủy bộ lọc khi làm mới
            controller.onLikesTabClicked();
            controller.onLikedTabClicked();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Thêm listener cho nút lọc
        filterButton.setOnClickListener(v -> showFilterDialog());

        controller = new LikeController(this);

        // Khôi phục trạng thái nếu có
        if (savedInstanceState != null) {
            userList = savedInstanceState.getParcelableArrayList(KEY_USERS_WHO_LIKED_ME);
            if (userList == null) {
                userList = savedInstanceState.getParcelableArrayList(KEY_USERS_I_LIKED);
            }
            if (userList != null) {
                userAdapter.updateList(userList);
            }
            isFilterApplied = savedInstanceState.getBoolean(KEY_FILTER_APPLIED, false);
            Log.d("LikeFragment", "Restored isFilterApplied: " + isFilterApplied);
            controller.applyFilter(
                    savedInstanceState.getDouble(KEY_MAX_DISTANCE, Double.MAX_VALUE),
                    savedInstanceState.getInt(KEY_MIN_AGE, 0),
                    savedInstanceState.getInt(KEY_MAX_AGE, Integer.MAX_VALUE),
                    savedInstanceState.getString(KEY_RESIDENCE_FILTER)
            );
            if (savedInstanceState.getBoolean(KEY_IS_LIKES_TAB_SELECTED, true)) {
                controller.onLikesTabClicked();
            } else {
                controller.onLikedTabClicked();
            }
        }

        // Xử lý sự kiện click trên các tab
        likesTab.setOnClickListener(v -> controller.onLikesTabClicked());
        likedTab.setOnClickListener(v -> controller.onLikedTabClicked());

        // Đặt tab "Lượt thích" được chọn mặc định
        updateTabSelection(true);
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

    private void showFilterDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_filter);

        // Tăng kích thước dialog
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
            Log.d("LikeFragment", "Applied filter - isFilterApplied: " + isFilterApplied);
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
        Log.d("LikeFragment", "Cleared filter - isFilterApplied: " + isFilterApplied);
    }

    public void setCurrentLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        userAdapter = new UserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
        userRecyclerView.setAdapter(userAdapter);
    }

    public void updateUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);
        userAdapter.updateList(users);
        isLoading = false;
    }

    public void updateTabSelection(boolean isLikesTab) {
        if (isLikesTab) {
            likesTab.animate().alpha(1f).setDuration(200).start();
            likedTab.animate().alpha(0.5f).setDuration(200).start();
            likesLabel.setTextColor(getResources().getColor(android.R.color.white));
            likedLabel.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            likesTab.animate().alpha(0.5f).setDuration(200).start();
            likedTab.animate().alpha(1f).setDuration(200).start();
            likesLabel.setTextColor(getResources().getColor(android.R.color.black));
            likedLabel.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    public void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

    public NavController getNavController() {
        return navController;
    }

    private void onUserClicked(User user) {
        controller.onUserClicked(user);
    }
}