package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.main.MainActivity;

public class xSignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText emailInput;
    private EditText passwordInput;
    private Button signInButton;
    private TextView signUpPrompt;
    private TextView warningTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Log.d(TAG, "SignInActivity onCreate started.");

        auth = FirebaseAuth.getInstance();

        // --- Kiểm tra trạng thái đăng nhập ban đầu ---
        FirebaseUser currentUser = auth.getCurrentUser();
        Log.d(TAG, "Current user before check: " + (currentUser == null ? "null" : currentUser.getEmail()));

        if (currentUser != null) {
            Log.d(TAG, "Redirecting to MainActivity because user is already logged in.");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "User is null, proceeding with SignInActivity setup.");
        // --- Khởi tạo các view ---
        emailInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        signInButton = findViewById(R.id.sign_in_button);
        signUpPrompt = findViewById(R.id.sign_up_prompt);
        warningTextView = findViewById(R.id.warning_text);

        // --- Xử lý sự kiện nhấn nút "Đăng nhập" ---
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // --- Xử lý sự kiện nhấn nút "Đăng ký" ---
        signUpPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(xSignInActivity.this, xSignUpActivity.class));
            }
        });
    }



    private void loginUser() {
        // --- Chuẩn bị trước khi đăng nhập ---
        warningTextView.setVisibility(View.GONE);
        warningTextView.setText("");
        emailInput.setError(null);
        passwordInput.setError(null);

        // --- Lấy dữ liệu ---
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // --- Kiểm tra dữ liệu đầu vào ---
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập địa chỉ email");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.requestFocus();
            warningTextView.setText("Định dạng Email không hợp lệ (ví dụ: name@example.com)");
            warningTextView.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            passwordInput.requestFocus();
            return;
        }

        // --- Thực hiện đăng nhập bằng Firebase ---
        signInButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        signInButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(xSignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            warningTextView.setVisibility(View.GONE);
                            startActivity(new Intent(xSignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Đăng nhập thất bại
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            try {
                                throw task.getException();
                            }  catch (FirebaseAuthInvalidCredentialsException e) {
                                warningTextView.setText("Email hoặc mật khẩu không chính xác.");
                                warningTextView.setVisibility(View.VISIBLE);
                                passwordInput.requestFocus();
                                passwordInput.setText("");
                            } catch (Exception e) {
                                Log.e(TAG, "Caught generic Exception", e);
                                warningTextView.setText("Đăng nhập thất bại: Có lỗi xảy ra.");
                                warningTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}