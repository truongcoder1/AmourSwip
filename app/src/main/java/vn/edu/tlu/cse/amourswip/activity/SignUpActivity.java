package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import vn.edu.tlu.cse.amourswip.Activity.SelectGenderActivity;
import vn.edu.tlu.cse.amourswip.Datalayer.model.User;
import vn.edu.tlu.cse.amourswip.Datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.email_input);
        etPassword = findViewById(R.id.password_input);
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        findViewById(R.id.sign_up_button).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Đăng ký tài khoản trên Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Tạo đối tượng User
                            User user = new User();
                            user.setUid(mAuth.getCurrentUser().getUid());
                            user.setEmail(email);

                            // Lưu thông tin cơ bản vào Realtime Database
                            userRepository.saveUser(user, new UserRepository.OnUserActionListener() {
                                @Override
                                public void onSuccess() {
                                    // Chuyển đến màn hình chọn giới tính
                                    Intent intent = new Intent(SignUpActivity.this, SelectGenderActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(SignUpActivity.this, "Lỗi khi lưu dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}