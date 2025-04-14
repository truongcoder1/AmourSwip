package vn.edu.tlu.cse.amourswip.view.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.controller.chSwipeController;
import vn.edu.tlu.cse.amourswip.view.adapter.chCardStackAdapter;
import vn.edu.tlu.cse.amourswip.model.data.xUser;
import android.media.MediaPlayer;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class chSwipeFragment extends Fragment {

    private static final String TAG = "SwipeFragment";
    private static final int REQUEST_CODE_LOCATION = 102;

    private CardStackView cardStackView;
    private ImageButton skipButton;
    private ImageButton likeButton;
    private ImageButton undoButton;
    private View skipCircle;
    private View likeCircle;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout errorLayout;
    private TextView errorMessage;
    private Button retryButton;
    private NavController navController;
    private chSwipeController controller;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private DatabaseReference matchNotificationsRef; // Thêm để lắng nghe thông báo match
    private ValueEventListener matchListener; // Listener cho thông báo match
    private String currentUserId;
    private List<xUser> userList;
    private chCardStackAdapter adapter;
    private CardStackLayoutManager layoutManager;
    private View currentStamp;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude;
    private double currentLongitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating activity_swipe layout");
        return inflater.inflate(R.layout.activity_swipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: Initializing SwipeFragment");

        navController = Navigation.findNavController(view);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "onViewCreated: User not logged in");
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
        matchNotificationsRef = database.child("match_notifications").child(currentUserId);
        Log.d(TAG, "onViewCreated: Current user ID: " + currentUserId);

        cardStackView = view.findViewById(R.id.card_stack_view);
        skipButton = view.findViewById(R.id.skip_button);
        likeButton = view.findViewById(R.id.like_button);
        undoButton = view.findViewById(R.id.undo_button);
        skipCircle = view.findViewById(R.id.skip_circle);
        likeCircle = view.findViewById(R.id.like_circle);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        errorMessage = view.findViewById(R.id.error_message);
        retryButton = view.findViewById(R.id.retry_button);

        userList = new ArrayList<>();
        adapter = new chCardStackAdapter(userList);

        layoutManager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                if (currentStamp != null) {
                    ViewGroup parent = (ViewGroup) currentStamp.getParent();
                    if (parent != null) {
                        parent.removeView(currentStamp);
                    }
                    currentStamp = null;
                }

                int topPosition = layoutManager.getTopPosition();
                CardStackView.ViewHolder viewHolder = cardStackView.findViewHolderForAdapterPosition(topPosition);
                View topView = viewHolder != null ? viewHolder.itemView : null;

                if (topView != null) {
                    if (direction == Direction.Left && ratio > 0.3) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View nopeStamp = inflater.inflate(R.layout.nope_stamp, (ViewGroup) topView, false);
                        ((ViewGroup) topView).addView(nopeStamp);

                        nopeStamp.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int textWidth = nopeStamp.getMeasuredWidth();
                        nopeStamp.setX(topView.getWidth() - textWidth - 16);
                        nopeStamp.setY(16);
                        currentStamp = nopeStamp;
                    } else if (direction == Direction.Right && ratio > 0.3) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View likeStamp = inflater.inflate(R.layout.like_stamp, (ViewGroup) topView, false);
                        ((ViewGroup) topView).addView(likeStamp);

                        likeStamp.setX(16);
                        likeStamp.setY(16);
                        currentStamp = likeStamp;
                    }
                }
            }

            @Override
            public void onCardSwiped(Direction direction) {
                Log.d(TAG, "onCardSwiped: Direction = " + direction.name());
                if (currentStamp != null) {
                    ViewGroup parent = (ViewGroup) currentStamp.getParent();
                    if (parent != null) {
                        parent.removeView(currentStamp);
                    }
                    currentStamp = null;
                }

                skipCircle.setVisibility(View.INVISIBLE);
                likeCircle.setVisibility(View.INVISIBLE);
                skipButton.setImageResource(R.drawable.ic_dislike2);
                likeButton.setImageResource(R.drawable.ic_like);

                if (userList.isEmpty()) {
                    Toast.makeText(getActivity(), "Không có người dùng nào để hiển thị", Toast.LENGTH_SHORT).show();
                    return;
                }

                controller.handleCardSwiped(direction);
            }

            @Override
            public void onCardRewound() {
                Log.d(TAG, "onCardRewound: Card rewound");
                if (currentStamp != null) {
                    ViewGroup parent = (ViewGroup) currentStamp.getParent();
                    if (parent != null) {
                        parent.removeView(currentStamp);
                    }
                    currentStamp = null;
                }

                skipCircle.setVisibility(View.INVISIBLE);
                likeCircle.setVisibility(View.INVISIBLE);
                skipButton.setImageResource(R.drawable.ic_dislike2);
                likeButton.setImageResource(R.drawable.ic_like);
            }

            @Override
            public void onCardCanceled() {
                if (currentStamp != null) {
                    ViewGroup parent = (ViewGroup) currentStamp.getParent();
                    if (parent != null) {
                        parent.removeView(currentStamp);
                    }
                    currentStamp = null;
                }

                skipCircle.setVisibility(View.INVISIBLE);
                likeCircle.setVisibility(View.INVISIBLE);
                skipButton.setImageResource(R.drawable.ic_dislike2);
                likeButton.setImageResource(R.drawable.ic_like);
            }

            @Override
            public void onCardAppeared(View view, int position) {}

            @Override
            public void onCardDisappeared(View view, int position) {
                if (position >= userList.size() - 1) {
                    loadingIndicator.setVisibility(View.VISIBLE);
                    controller.loadUsers();
                }
            }
        });

        cardStackView.setLayoutManager(layoutManager);
        cardStackView.setAdapter(adapter);

        // Lấy vị trí hiện tại của người dùng
        requestLocationPermission();

        // Thêm listener để làm mới danh sách
        swipeRefreshLayout.setOnRefreshListener(() -> {
            controller.resetPagination();
            controller.loadUsers();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Thêm listener cho nút hoàn tác
        undoButton.setOnClickListener(v -> {
            controller.undoLastSwipe();
        });

        retryButton.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            controller.resetPagination();
            controller.loadUsers();
        });

        controller = new chSwipeController(this, cardStackView, skipCircle, likeCircle, skipButton, likeButton,
                null, null, navController, userList, adapter);

        // Lắng nghe thông báo match từ Firebase
        setupMatchListener();
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

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập vị trí để tính khoảng cách", Toast.LENGTH_LONG).show();
                adapter.setCurrentUserLocation(0, 0); // Nếu không có quyền, khoảng cách sẽ hiển thị "N/A"
            }
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                adapter.setCurrentUserLocation(currentLatitude, currentLongitude);
                Log.d(TAG, "User location: " + currentLatitude + ", " + currentLongitude);
            } else {
                Log.w(TAG, "Cannot get user location");
                adapter.setCurrentUserLocation(0, 0); // Nếu không lấy được vị trí, khoảng cách sẽ hiển thị "N/A"
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            adapter.setCurrentUserLocation(0, 0);
            Toast.makeText(getContext(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void showLikeAnimation() {}

    public void showSkipAnimation() {}

    public void showLikeAnimationOnButton(View button) {
        CardStackLayoutManager layoutManager = (CardStackLayoutManager) cardStackView.getLayoutManager();
        if (layoutManager != null) {
            int topPosition = layoutManager.getTopPosition();
            Log.d(TAG, "showLikeAnimationOnButton: topPosition = " + topPosition);
            CardStackView.ViewHolder viewHolder = cardStackView.findViewHolderForAdapterPosition(topPosition);
            View topView = viewHolder != null ? viewHolder.itemView : null;

            if (topView != null) {
                Log.d(TAG, "showLikeAnimationOnButton: topView found, showing LIKE stamp");
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View likeStamp = inflater.inflate(R.layout.like_stamp, (ViewGroup) topView, false);
                ((ViewGroup) topView).addView(likeStamp);

                likeStamp.setX(16);
                likeStamp.setY(16);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ((ViewGroup) topView).removeView(likeStamp);
                }, 500);

                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(mp -> mp.release());
                }
            } else {
                Log.e(TAG, "showLikeAnimationOnButton: topView is null");
            }
        } else {
            Log.e(TAG, "showLikeAnimationOnButton: layoutManager is null");
        }
    }

    public void showSkipAnimationOnButton(View button) {
        CardStackLayoutManager layoutManager = (CardStackLayoutManager) cardStackView.getLayoutManager();
        if (layoutManager != null) {
            int topPosition = layoutManager.getTopPosition();
            Log.d(TAG, "showSkipAnimationOnButton: topPosition = " + topPosition);
            CardStackView.ViewHolder viewHolder = cardStackView.findViewHolderForAdapterPosition(topPosition);
            View topView = viewHolder != null ? viewHolder.itemView : null;

            if (topView != null) {
                Log.d(TAG, "showSkipAnimationOnButton: topView found, showing NOPE stamp");
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View nopeStamp = inflater.inflate(R.layout.nope_stamp, (ViewGroup) topView, false);
                ((ViewGroup) topView).addView(nopeStamp);

                nopeStamp.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int textWidth = nopeStamp.getMeasuredWidth();
                nopeStamp.setX(topView.getWidth() - textWidth - 16);
                nopeStamp.setY(16);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ((ViewGroup) topView).removeView(nopeStamp);
                }, 500);

                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(mp -> mp.release());
                }
            } else {
                Log.e(TAG, "showSkipAnimationOnButton: topView is null");
            }
        } else {
            Log.e(TAG, "showSkipAnimationOnButton: layoutManager is null");
        }
    }

    public void showError(String error) {
        if (getContext() != null) {
            errorMessage.setText(error);
            errorLayout.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
            cardStackView.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "showError: Cannot show error layout, context is null. Error message: " + error);
        }
    }

    public void showUsers() {
        cardStackView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

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
                    navController.navigate(R.id.action_swipeFragment_to_listChatFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to chat: " + e.getMessage());
                    showError("Lỗi điều hướng: " + e.getMessage());
                }
            });

            keepSwipingButton.setOnClickListener(v -> {
                Log.d(TAG, "Keep swiping button clicked, dismissing dialog");
                matchDialog.dismiss();
                controller.loadUsers();
            });

            Log.d(TAG, "showMatchDialog: Showing dialog");
            matchDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing match dialog: " + e.getMessage(), e);
            showError("Lỗi hiển thị dialog match: " + e.getMessage());
        }
    }
}