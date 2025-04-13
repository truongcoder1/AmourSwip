package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.model.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword, etResidence;
    private TextView tvWarning;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.email_input);
        etUsername = findViewById(R.id.username_input);
        etPassword = findViewById(R.id.password_input);
        etConfirmPassword = findViewById(R.id.confirm_password_input);
        etResidence = findViewById(R.id.residence_input); // Khởi tạo trường "Nơi ở"
        tvWarning = findViewById(R.id.warning_text);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        findViewById(R.id.sign_up_button).setOnClickListener(v -> {
            validateAndRegisterUser();
        });
    }

    private void validateAndRegisterUser() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String residence = etResidence.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning("Định dạng Email không hợp lệ (ví dụ: name@example.com)");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            showWarning("Tên người dùng không được để trống");
            etUsername.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showWarning("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showWarning("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            etConfirmPassword.setText("");
            return;
        }

        // Trường "Nơi ở" không bắt buộc, nên không cần kiểm tra TextUtils.isEmpty(residence)

        hideWarning();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserDataAndNavigate(email, username, residence);
                    } else {
                        String errorMessage = "Đăng ký thất bại.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "Địa chỉ Email này đã được sử dụng.";
                            etEmail.requestFocus();
                        } catch (Exception e) {
                            errorMessage = "Lỗi: " + e.getLocalizedMessage();
                        }
                        showWarning(errorMessage);
                    }
                });
    }

    private void saveUserDataAndNavigate(String email, String username, String residence) {
        if (mAuth.getCurrentUser() == null) {
            showWarning("Lỗi: Không tìm thấy người dùng sau khi đăng ký.");
            return;
        }

        User user = new User();
        user.setUid(mAuth.getCurrentUser().getUid());
        user.setEmail(email);
        user.setName(username);
        user.setResidence(residence); // Lưu trường "Nơi ở" vào đối tượng User

        userRepository.saveUser(user, new UserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, SelectGenderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                showWarning("Lỗi khi lưu dữ liệu: " + errorMessage);
            }
        });
    }

    private void showWarning(String message) {
        tvWarning.setText(message);
        tvWarning.setVisibility(View.VISIBLE);
    }

    private void hideWarning() {
        tvWarning.setVisibility(View.GONE);
    }
}