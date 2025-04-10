package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import vn.edu.tlu.cse.amourswip.datalayer.model.User;
import vn.edu.tlu.cse.amourswip.datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SignUpActivity extends AppCompatActivity {

    // Khai báo thêm các EditText và TextView cảnh báo
    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private TextView tvWarning;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Tìm view bằng ID
        etEmail = findViewById(R.id.email_input);
        etUsername = findViewById(R.id.username_input);
        etPassword = findViewById(R.id.password_input);
        etConfirmPassword = findViewById(R.id.confirm_password_input);
        tvWarning = findViewById(R.id.warning_text); // Thêm dòng này

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // Xử lý sự kiện nhấn nút đăng ký
        findViewById(R.id.sign_up_button).setOnClickListener(v -> {
            validateAndRegisterUser();
        });
    }

    // Hàm kiểm tra dữ liệu nhập và tiến hành đăng ký
    private void validateAndRegisterUser() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning("Định dạng Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }



        if (password.length() < 6) {
            showWarning("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }



        //  Kiểm tra Mật khẩu và Xác nhận Mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            showWarning("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            etConfirmPassword.setText("");
            return;
        }

        hideWarning();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserDataAndNavigate(email);
                    } else {
                        String errorMessage = "Đăng ký thất bại.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "Địa chỉ Email này đã được sử dụng.";
                            etEmail.requestFocus(); // Focus lại vào ô email
                        } catch (Exception e) {
                            errorMessage = "Lỗi: " + e.getLocalizedMessage();
                        }
                        showWarning(errorMessage);                     }
                });
    }

    // Hàm lưu dữ liệu người dùng và chuyển màn hình
    private void saveUserDataAndNavigate(String email) {
        if (mAuth.getCurrentUser() == null) {
            showWarning("Lỗi: Không tìm thấy người dùng sau khi đăng ký.");
            return;
        }

        User user = new User();
        user.setUid(mAuth.getCurrentUser().getUid());
        user.setEmail(email);


        userRepository.saveUser(user, new UserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show(); // Thông báo thành công
                Intent intent = new Intent(SignUpActivity.this, SelectGenderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Kết thúc SignUpActivity
            }

            @Override
            public void onFailure(String errorMessage) {
                showWarning("Lỗi khi lưu dữ liệu: " + errorMessage);
            }
        });
    }


    // Hàm tiện ích để hiển thị cảnh báo
    private void showWarning(String message) {
        tvWarning.setText(message);
        tvWarning.setVisibility(View.VISIBLE);
    }

    // Hàm tiện ích để ẩn cảnh báo
    private void hideWarning() {
        tvWarning.setVisibility(View.GONE);
    }
}


