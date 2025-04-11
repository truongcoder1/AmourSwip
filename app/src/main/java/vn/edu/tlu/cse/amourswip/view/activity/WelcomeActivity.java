package vn.edu.tlu.cse.amourswip.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import vn.edu.tlu.cse.amourswip.R;

public class WelcomeActivity extends AppCompatActivity {
    private Button btnSignUp;
    private Button btnLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcomscreen);

        // Khởi tạo các view bằng findViewById
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);

        // Khởi tạo Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Kiểm tra trạng thái đăng nhập
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Xử lý sự kiện nhấn nút "Đăng ký"
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
            }
        });

        // Xử lý sự kiện nhấn nút "Đăng nhập"
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
            }
        });
    }
}