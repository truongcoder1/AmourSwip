package vn.edu.tlu.cse.amourswip.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.core.content.ContextCompat;
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
    private View skipCircle;
    private View likeCircle;
    private NavController navController;
    private SwipeController controller;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String currentUserId;
    private List<User> userList;
    private CardStackAdapter adapter;
    private CardStackLayoutManager layoutManager;
    private User currentUser;

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
            Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "onViewCreated: Current user ID: " + currentUserId);

        cardStackView = view.findViewById(R.id.card_stack_view);
        skipButton = view.findViewById(R.id.skip_button);
        likeButton = view.findViewById(R.id.like_button);
        skipCircle = view.findViewById(R.id.skip_circle);
        likeCircle = view.findViewById(R.id.like_circle);

        userList = new ArrayList<>();
        adapter = new CardStackAdapter(userList);

        // Gắn adapter và layout manager ngay tại đây để tránh lỗi "No adapter attached; skipping layout"
        layoutManager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                if (direction == Direction.Right) {
                    likeCircle.setVisibility(View.VISIBLE);
                    likeCircle.setAlpha(ratio);
                    likeButton.setImageResource(R.drawable.ic_like_black);
                    skipCircle.setVisibility(View.INVISIBLE);
                    skipButton.setImageResource(R.drawable.ic_dislike2);
                } else if (direction == Direction.Left) {
                    skipCircle.setVisibility(View.VISIBLE);
                    skipCircle.setAlpha(ratio);
                    skipButton.setImageResource(R.drawable.ic_dislike2_black);
                    likeCircle.setVisibility(View.INVISIBLE);
                    likeButton.setImageResource(R.drawable.ic_like);
                }
            }

            @Override
            public void onCardSwiped(Direction direction) {
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
            public void onCardRewound() {}

            @Override
            public void onCardCanceled() {
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
                    controller.loadUsers();
                }
            }
        });

        cardStackView.setLayoutManager(layoutManager);
        cardStackView.setAdapter(adapter);

        // Khởi tạo Controller, không cần truyền matchNotificationText và matchNotificationLayout nữa
        controller = new SwipeController(this, cardStackView, skipCircle, likeCircle, skipButton, likeButton,
                null, null, navController, userList, adapter);
    }

    public void showLikeAnimation() {
        CardStackLayoutManager layoutManager = (CardStackLayoutManager) cardStackView.getLayoutManager();
        if (layoutManager != null) {
            int topPosition = layoutManager.getTopPosition();
            CardStackView.ViewHolder viewHolder = cardStackView.findViewHolderForAdapterPosition(topPosition);
            View topView = viewHolder != null ? viewHolder.itemView : null;

            if (topView != null) {
                topView.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .start();

                TextView likeText = new TextView(getContext());
                likeText.setText("LIKE");
                likeText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                likeText.setTextSize(48);
                likeText.setTypeface(null, android.graphics.Typeface.BOLD);
                likeText.setBackgroundResource(R.drawable.like_background);
                likeText.setPadding(24, 12, 24, 12);
                likeText.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                ((ViewGroup) topView).addView(likeText);

                likeText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int textWidth = likeText.getMeasuredWidth();
                int textHeight = likeText.getMeasuredHeight();

                likeText.setX(16);
                likeText.setY(16);
                likeText.setRotation(45f);

                likeText.setScaleX(0f);
                likeText.setScaleY(0f);
                likeText.setAlpha(1f);
                likeText.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .alpha(0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ((ViewGroup) topView).removeView(likeText);
                            }
                        })
                        .start();

                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(mp -> mp.release());
                }
            }
        }
    }

    public void showSkipAnimation() {
        CardStackLayoutManager layoutManager = (CardStackLayoutManager) cardStackView.getLayoutManager();
        if (layoutManager != null) {
            int topPosition = layoutManager.getTopPosition();
            CardStackView.ViewHolder viewHolder = cardStackView.findViewHolderForAdapterPosition(topPosition);
            View topView = viewHolder != null ? viewHolder.itemView : null;

            if (topView != null) {
                topView.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .start();

                TextView nopeText = new TextView(getContext());
                nopeText.setText("NOPE");
                nopeText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                nopeText.setTextSize(48);
                nopeText.setTypeface(null, android.graphics.Typeface.BOLD);
                nopeText.setBackgroundResource(R.drawable.nope_background);
                nopeText.setPadding(24, 12, 24, 12);
                nopeText.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                ((ViewGroup) topView).addView(nopeText);

                nopeText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int textWidth = nopeText.getMeasuredWidth();
                int textHeight = nopeText.getMeasuredHeight();

                nopeText.setX(topView.getWidth() - textWidth - 32);
                nopeText.setY(32);
                nopeText.setRotation(-45f);

                nopeText.setScaleX(0f);
                nopeText.setScaleY(0f);
                nopeText.setAlpha(1f);
                nopeText.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .alpha(0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ((ViewGroup) topView).removeView(nopeText);
                            }
                        })
                        .start();

                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(mp -> mp.release());
                }
            }
        }
    }

    // Thêm animation "LIKE" cho nút Like
    public void showLikeAnimationOnButton(View button) {
        button.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .start();

        TextView likeText = new TextView(getContext());
        likeText.setText("LIKE");
        likeText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        likeText.setTextSize(48);
        likeText.setTypeface(null, android.graphics.Typeface.BOLD);
        likeText.setBackgroundResource(R.drawable.like_background);
        likeText.setPadding(24, 12, 24, 12);
        likeText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ((ViewGroup) button.getParent()).addView(likeText);

        likeText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int textWidth = likeText.getMeasuredWidth();
        int textHeight = likeText.getMeasuredHeight();

        likeText.setX(button.getX() + 16);
        likeText.setY(button.getY() + 16);
        likeText.setRotation(45f);

        likeText.setScaleX(0f);
        likeText.setScaleY(0f);
        likeText.setAlpha(1f);
        likeText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0f)
                .rotationBy(10f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ((ViewGroup) button.getParent()).removeView(likeText);
                    }
                })
                .start();

        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mp.release());
        }
    }

    public void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }
}