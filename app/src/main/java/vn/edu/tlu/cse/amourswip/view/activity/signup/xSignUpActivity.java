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

import vn.edu.tlu.cse.amourswip.model.data.xUser;
import vn.edu.tlu.cse.amourswip.model.repository.xUserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class xSignUpActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword, etResidence;
    private TextView tvWarning;
    private FirebaseAuth mAuth;
    private xUserRepository userRepository;

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
        userRepository = new xUserRepository();

        findViewById(R.id.sign_up_button).setOnClickListener(v -> {
            validateAndRegisterUser();
        });
    }

    private void validateAndRegisterUser() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String residence = etResidence.getText().toString().trim(); // Lấy và cắt khoảng trắng thừa

        // --- Bắt đầu kiểm tra dữ liệu đầu vào ---

        // Kiểm tra Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning("Định dạng Email không hợp lệ (ví dụ: name@example.com)");
            etEmail.requestFocus();
            return;
        }

        // Kiểm tra Tên người dùng
        if (TextUtils.isEmpty(username)) {
            showWarning("Tên người dùng không được để trống");
            etUsername.requestFocus();
            return;
        }

        // Kiểm tra Mật khẩu
        if (password.length() < 6) {
            showWarning("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        // Kiểm tra Xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            showWarning("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            etConfirmPassword.setText(""); // Xóa nội dung ô xác nhận mật khẩu
            return;
        }

        // Kiểm tra Nơi ở (chỉ kiểm tra nếu người dùng có nhập)
        if (!TextUtils.isEmpty(residence)) {
            // Sử dụng biểu thức chính quy (regex) để kiểm tra
            // ^            : Bắt đầu chuỗi
            // [\\p{L}\\s]+ : Chấp nhận một hoặc nhiều ký tự là chữ cái Unicode (\p{L})
            //               hoặc khoảng trắng (\s). \p{L} bao gồm cả tiếng Việt có dấu.
            // $            : Kết thúc chuỗi
            String residencePattern = "^[\\p{L}\\s]+$";
            if (!residence.matches(residencePattern)) {
                showWarning("Nơi ở chỉ được chứa chữ cái và khoảng trắng, không nhập số hoặc ký tự đặc biệt.");
                etResidence.requestFocus();
                return; // Dừng lại nếu không hợp lệ
            }
        }
        // --- Kết thúc kiểm tra dữ liệu đầu vào ---


        // Nếu tất cả kiểm tra đều qua, ẩn cảnh báo và tiến hành đăng ký
        hideWarning();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công, lưu thông tin người dùng và chuyển màn hình
                        saveUserDataAndNavigate(email, username, residence);
                    } else {
                        // Xử lý lỗi đăng ký
                        String errorMessage = "Đăng ký thất bại.";
                        try {
                            // Ném ngoại lệ để bắt các loại lỗi cụ thể
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            // Lỗi email đã tồn tại
                            errorMessage = "Địa chỉ Email này đã được sử dụng.";
                            etEmail.requestFocus(); // Đặt lại focus vào ô email
                        } catch (Exception e) {
                            // Các lỗi khác
                            errorMessage = "Lỗi: " + e.getLocalizedMessage();
                        }
                        // Hiển thị thông báo lỗi
                        showWarning(errorMessage);
                    }
                });
    }

    private void saveUserDataAndNavigate(String email, String username, String residence) {
        // Kiểm tra xem người dùng hiện tại có tồn tại không (dù ít khi xảy ra sau khi đăng ký thành công)
        if (mAuth.getCurrentUser() == null) {
            showWarning("Lỗi: Không tìm thấy người dùng sau khi đăng ký.");
            return;
        }

        // Tạo đối tượng User mới
        xUser user = new xUser();
        user.setUid(mAuth.getCurrentUser().getUid());
        user.setEmail(email);
        user.setName(username);
        // Chỉ lưu nơi ở nếu người dùng đã nhập (đã được trim())
        if (!TextUtils.isEmpty(residence)) {
            user.setResidence(residence);
        } else {
            user.setResidence(null); // Hoặc chuỗi rỗng "", tùy vào cách bạn muốn xử lý trong DB
        }


        // Gọi phương thức lưu User từ UserRepository
        userRepository.saveUser(user, new xUserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                // Lưu thành công, hiển thị thông báo và chuyển sang màn hình chọn giới tính
                Toast.makeText(xSignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(xSignUpActivity.this, xSelectGenderActivity.class);
                // Xóa các activity trước đó khỏi stack để người dùng không quay lại màn hình đăng ký
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Kết thúc SignUpActivity
            }

            @Override
            public void onFailure(String errorMessage) {
                // Lỗi khi lưu dữ liệu vào cơ sở dữ liệu (Firestore/Realtime DB)
                showWarning("Lỗi khi lưu dữ liệu người dùng: " + errorMessage);
                // Cân nhắc: Có nên xóa tài khoản Firebase Auth nếu lưu DB thất bại không?
                // Điều này phức tạp hơn và cần xử lý cẩn thận. Tạm thời chỉ báo lỗi.
            }
        });
    }

    // Hiển thị thông báo cảnh báo
    private void showWarning(String message) {
        tvWarning.setText(message);
        tvWarning.setVisibility(View.VISIBLE);
    }

    // Ẩn thông báo cảnh báo
    private void hideWarning() {
        tvWarning.setVisibility(View.GONE);
    }
}