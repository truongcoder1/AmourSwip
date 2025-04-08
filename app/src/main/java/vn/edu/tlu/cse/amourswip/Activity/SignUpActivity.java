package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import vn.edu.tlu.cse.amourswip.Datalayer.Model.User;
import vn.edu.tlu.cse.amourswip.Datalayer.Repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button signUpButton;
    private FirebaseAuth auth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo các view bằng findViewById
        emailInput = findViewById(R.id.email_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signUpButton = findViewById(R.id.sign_up_button);

        // Khởi tạo Firebase Authentication và UserRepository
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // Xử lý sự kiện nhấn nút "Đăng ký"
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                // Kiểm tra dữ liệu đầu vào
                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Đăng ký tài khoản với Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Lưu thông tin người dùng vào Realtime Database
                                String userId = auth.getCurrentUser().getUid();
                                User user = new User(userId, username, 0, new ArrayList<>(), "", email);
                                userRepository.saveUser(user);

                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}