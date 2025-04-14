package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.main.MainActivity;
import android.animation.ObjectAnimator;
import java.util.Random;

public class xWelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";

    private Button btnSignUp;
    private Button btnLogin;
    private FirebaseAuth auth;
    private RelativeLayout heartContainer;
    private Random random;
    private Handler handler;
    private static final int HEART_COUNT = 20; // Số lượng trái tim
    private static final int ANIMATION_DURATION = 2000; // Thời gian mỗi trái tim rơi xuống (ms)
    private static final int HEART_SPAWN_INTERVAL = 200; // Khoảng thời gian giữa các trái tim (ms)
    private static final int SHOW_BUTTONS_DELAY = 3000; // Thời gian chờ trước khi hiển thị nút (ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Setting content view from activity_welcomscreen.xml");
        setContentView(R.layout.activity_welcomscreen);

        // Khởi tạo các view bằng findViewById
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        heartContainer = findViewById(R.id.heart_container);

        // Kiểm tra các view
        if (btnSignUp == null || btnLogin == null) {
            Log.e(TAG, "onCreate: Button (btnSignUp or btnLogin) is null. Check activity_welcomscreen.xml layout.");
            return;
        }
        if (heartContainer == null) {
            Log.e(TAG, "onCreate: RelativeLayout (heart_container) is null. Check activity_welcomscreen.xml layout.");
            return;
        }

        // Khởi tạo Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Kiểm tra trạng thái đăng nhập
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: User is already logged in, navigating to MainActivity");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Khởi tạo hiệu ứng
        random = new Random();
        handler = new Handler(Looper.getMainLooper());

        // Bắt đầu hiệu ứng trái tim rơi xuống
        startHeartAnimation();

        // Hiển thị các nút sau khi hiệu ứng hoàn tất
        handler.postDelayed(() -> {
            Log.d(TAG, "onCreate: Showing buttons with fade-in animation");
            showButtonsWithFadeIn();
        }, SHOW_BUTTONS_DELAY);

        // Xử lý sự kiện nhấn nút "Đăng ký"
        btnSignUp.setOnClickListener(v -> {
            Log.d(TAG, "btnSignUp clicked: Navigating to SignUpActivity");
            startActivity(new Intent(xWelcomeActivity.this, xSignUpActivity.class));
        });

        // Xử lý sự kiện nhấn nút "Đăng nhập"
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "btnLogin clicked: Navigating to SignInActivity");
            startActivity(new Intent(xWelcomeActivity.this, xSignInActivity.class));
        });
    }

    private void startHeartAnimation() {
        Log.d(TAG, "startHeartAnimation: Waiting for heartContainer layout");
        heartContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove listener to avoid multiple calls
                heartContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (heartContainer.getWidth() > 30) {
                    Log.d(TAG, "startHeartAnimation: Layout ready, starting heart animation");
                    for (int i = 0; i < HEART_COUNT; i++) {
                        handler.postDelayed(() -> createHeart(), i * HEART_SPAWN_INTERVAL);
                    }
                } else {
                    Log.e(TAG, "startHeartAnimation: heartContainer width too small: " + heartContainer.getWidth());
                }
            }
        });
    }

    private void createHeart() {
        // Tạo một ImageView cho trái tim
        ImageView heartView = new ImageView(this);
        heartView.setImageResource(R.drawable.ic_heartwel);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200); // Kích thước trái tim
        params.leftMargin = random.nextInt(heartContainer.getWidth() - 30); // Vị trí ngẫu nhiên theo chiều ngang
        params.topMargin = -30; // Bắt đầu từ trên cùng
        heartView.setLayoutParams(params);

        // Thêm trái tim vào container
        heartContainer.addView(heartView);

        // Hiệu ứng rơi xuống
        ObjectAnimator moveDown = ObjectAnimator.ofFloat(heartView, "translationY", heartContainer.getHeight());
        moveDown.setDuration(ANIMATION_DURATION);
        moveDown.setRepeatCount(0);

        // Hiệu ứng mờ dần
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(heartView, "alpha", 1f, 0f);
        fadeOut.setDuration(ANIMATION_DURATION);
        fadeOut.setRepeatCount(0);

        // Bắt đầu hiệu ứng
        moveDown.start();
        fadeOut.start();

        // Xóa trái tim sau khi hiệu ứng hoàn tất
        moveDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "Heart animation started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                heartContainer.removeView(heartView);
                Log.d(TAG, "Heart animation ended and view removed");
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void showButtonsWithFadeIn() {
        // Hiển thị nút với hiệu ứng mờ dần
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000); // Thời gian mờ dần: 1000ms
        fadeIn.setFillAfter(true); // Giữ trạng thái sau khi animation kết thúc

        btnSignUp.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        btnSignUp.startAnimation(fadeIn);
        btnLogin.startAnimation(fadeIn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng tạo trái tim khi Activity bị hủy
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "onDestroy: Stopped heart animation");
        }
    }
}