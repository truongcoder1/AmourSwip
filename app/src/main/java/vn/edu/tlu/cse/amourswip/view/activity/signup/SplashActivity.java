package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.signup.WelcomeActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView animationView = findViewById(R.id.animation_view);

        // Tải GIF với Glide và kiểm tra trạng thái tải
        Glide.with(this)
                .asGif()
                .load(R.drawable.heart_rain)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        // Xử lý lỗi tải GIF (tùy chọn)
                        startNextActivity();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        // GIF đã tải xong, bắt đầu đếm thời gian delay
                        new Handler(Looper.getMainLooper()).postDelayed(() -> startNextActivity(), 3700);
                        return false;
                    }
                })
                .into(animationView);
    }

    private void startNextActivity() {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}