package vn.edu.tlu.cse.amourswip.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.controller.SwipeController;
import vn.edu.tlu.cse.amourswip.view.adapter.CardStackAdapter;
import vn.edu.tlu.cse.amourswip.model.data.User;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;

public class SwipeFragment extends Fragment {

    private static final String TAG = "SwipeFragment";

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
    private SwipeController controller;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String currentUserId;
    private List<User> userList;
    private CardStackAdapter adapter;
    private CardStackLayoutManager layoutManager;
    private View currentStamp;

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

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "onViewCreated: User not logged in");
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
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
        adapter = new CardStackAdapter(userList);

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

        controller = new SwipeController(this, cardStackView, skipCircle, likeCircle, skipButton, likeButton,
                null, null, navController, userList, adapter);
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
}