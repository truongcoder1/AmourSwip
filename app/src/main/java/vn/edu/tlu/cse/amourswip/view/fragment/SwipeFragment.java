package vn.edu.tlu.cse.amourswip.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.google.android.material.button.MaterialButton;
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
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.adapter.CardStackAdapter;
import vn.edu.tlu.cse.amourswip.model.data.User;

public class SwipeFragment extends Fragment {

    private CardStackView cardStackView;
    private ImageButton skipButton;
    private ImageButton likeButton;
    private View skipCircle;
    private View likeCircle;
    private View rootView;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String currentUserId;
    private List<User> userList;
    private CardStackAdapter adapter;
    private CardStackLayoutManager layoutManager;
    private User currentUser;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_swipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;
        navController = Navigation.findNavController(view);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();

        cardStackView = view.findViewById(R.id.card_stack_view);
        skipButton = view.findViewById(R.id.skip_button);
        likeButton = view.findViewById(R.id.like_button);
        skipCircle = view.findViewById(R.id.skip_circle);
        likeCircle = view.findViewById(R.id.like_circle);

        userList = new ArrayList<>();

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

                User otherUser = userList.get(layoutManager.getTopPosition() - 1);
                if (direction == Direction.Right) {
                    likeUser(otherUser);
                    showLikeAnimation();
                } else if (direction == Direction.Left) {
                    showSkipAnimation();
                }
            }

            @Override
            public void onCardRewound() {
            }

            @Override
            public void onCardCanceled() {
                skipCircle.setVisibility(View.INVISIBLE);
                likeCircle.setVisibility(View.INVISIBLE);
                skipButton.setImageResource(R.drawable.ic_dislike2);
                likeButton.setImageResource(R.drawable.ic_like);
            }

            @Override
            public void onCardAppeared(View view, int position) {
            }

            @Override
            public void onCardDisappeared(View view, int position) {
                if (position >= userList.size() - 1) {
                    loadUsers();
                }
            }
        });

        SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeAnimationSetting);
        cardStackView.setLayoutManager(layoutManager);

        adapter = new CardStackAdapter(userList);
        cardStackView.setAdapter(adapter);

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

    private void loadCurrentUser() {
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null && currentUser.getPreferredGender() != null) {
                    // Truyền vị trí của người dùng hiện tại vào adapter
                    if (currentUser.isLocationEnabled()) {
                        adapter.setCurrentUserLocation(currentUser.getLatitude(), currentUser.getLongitude());
                    } else {
                        adapter.setCurrentUserLocation(0.0, 0.0); // Vị trí mặc định nếu không bật định vị
                    }
                    loadUsers();
                } else {
                    Toast.makeText(getActivity(), "Không tìm thấy thông tin người dùng hiện tại hoặc thiếu dữ liệu giới tính ưa thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi tải thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        if (currentUser == null) return;

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !user.getUid().equals(currentUserId)) {
                        String preferredGender = currentUser.getPreferredGender();
                        String userGender = user.getGender();
                        if (preferredGender != null && userGender != null && preferredGender.equals(userGender)) {
                            userList.add(user);
                        }
                    }
                }
                if (userList.isEmpty()) {
                    Toast.makeText(getActivity(), "Không có người dùng nào để hiển thị", Toast.LENGTH_LONG).show();
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi tải danh sách người dùng: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void likeUser(User otherUser) {
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (checkMatchCondition(currentUser, otherUser)) {
                            checkForMatch(otherUser);
                        } else {
                            Toast.makeText(getActivity(), "Lượt thích đã được ghi lại, nhưng không thỏa mãn điều kiện để match!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Lỗi khi thích: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean checkMatchCondition(User currentUser, User otherUser) {
        double distance = calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                otherUser.getLatitude(), otherUser.getLongitude()
        );
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

                            showMatchDialog(otherUser, chatId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "Lỗi kiểm tra match: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showMatchDialog(User otherUser, String chatId) {
        Dialog matchDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        matchDialog.setContentView(R.layout.match_notification);

        ImageButton closeButton = matchDialog.findViewById(R.id.close_button);
        TextView matchTitle = matchDialog.findViewById(R.id.match_title);
        MaterialButton chatButton = matchDialog.findViewById(R.id.chat_button);
        MaterialButton continueButton = matchDialog.findViewById(R.id.continue_button);

        String matchMessage = otherUser.getName() != null && !otherUser.getName().isEmpty()
                ? "Chúc mừng! Bạn và " + otherUser.getName() + " đã kết nối thành công!"
                : "Chúc mừng! Bạn đã kết nối thành công!";
        matchTitle.setText(matchMessage);

        closeButton.setOnClickListener(v -> matchDialog.dismiss());

        chatButton.setOnClickListener(v -> {
            try {
                matchDialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("chatId", chatId);
                if (navController == null) {
                    Toast.makeText(getContext(), "Lỗi: NavController chưa được khởi tạo", Toast.LENGTH_LONG).show();
                    return;
                }
                navController.navigate(R.id.action_swipeFragment_to_listChatFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        continueButton.setOnClickListener(v -> matchDialog.dismiss());

        matchDialog.show();
    }

    private void showLikeAnimation() {
        View topView = layoutManager.getTopView();
        if (topView != null) {
            topView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start();

            TextView likeText = new TextView(getContext());
            likeText.setText("LIKE");
            likeText.setTextColor(getResources().getColor(android.R.color.white));
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

    private void showSkipAnimation() {
        View topView = layoutManager.getTopView();
        if (topView != null) {
            topView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start();

            TextView nopeText = new TextView(getContext());
            nopeText.setText("NOPE");
            nopeText.setTextColor(getResources().getColor(android.R.color.white));
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