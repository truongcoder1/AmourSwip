package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.signup.WelcomeActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Khởi tạo ImageView để hiển thị GIF
        ImageView animationView = findViewById(R.id.animation_view);

        // Sử dụng Glide để tải và hiển thị GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.heart_rain) // File GIF đã lưu trong res/drawable
                .into(animationView);

        // Chuyển đến WelcomeActivity sau 4 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }, 3700); // Thời gian delay: 4 giây
    }
}