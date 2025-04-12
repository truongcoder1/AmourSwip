package vn.edu.tlu.cse.amourswip.view.activity.profile;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.signup.MapActivity;
import vn.edu.tlu.cse.amourswip.view.activity.signup.SignInActivity;

public class SettingActivity extends AppCompatActivity {

    private Button notificationButton;
    private Button locationButton;
    private Button changePasswordButton;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";
    private static final String TAG = "SettingActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Khởi tạo các view
        notificationButton = findViewById(R.id.notification_button);
        locationButton = findViewById(R.id.location_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutButton = findViewById(R.id.logout_button);

        updateNotificationButtonState();

        // --- Xử lý sự kiện click ---
        notificationButton.setOnClickListener(v -> toggleNotifications());
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, MapActivity.class);
            startActivity(intent);
        });
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    // --- Logic cho các chức năng ---

    private void updateNotificationButtonState() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
    }

    private void toggleNotifications() {
        boolean currentStatus = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
        boolean newStatus = !currentStatus;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOTIFICATIONS_ENABLED_KEY, newStatus);
        editor.apply();

        Toast.makeText(SettingActivity.this, newStatus ? "Đã bật thông báo" : "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
        // TODO: Add actual notification logic (e.g., FCM topic subscription)
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        final EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        final EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_new_password);


        builder.setTitle("Đổi Mật Khẩu");
        builder.setPositiveButton("Xác nhận", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                etCurrentPassword.setError(null);
                etNewPassword.setError(null);
                etConfirmNewPassword.setError(null);

                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

                // 1. Kiểm tra mật khẩu hiện tại có trống không
                if (TextUtils.isEmpty(currentPassword)) {
                    etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
                    etCurrentPassword.requestFocus();
                    return;
                }

                // 2. Xác thực mật khẩu hiện tại với Firebase trước
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null || user.getEmail() == null) {
                    Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                Toast.makeText(SettingActivity.this, "Đang xác thực...", Toast.LENGTH_SHORT).show();
                positiveButton.setEnabled(false); // Vô hiệu hóa nút trong khi chờ

                user.reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            positiveButton.setEnabled(true);

                            if (reauthTask.isSuccessful()) {
                                // ---- Xác thực thành công ----
                                Log.d(TAG, "Re-authentication successful.");

                                // 3. Bây giờ mới kiểm tra mật khẩu mới
                                boolean newPasswordValid = true;
                                if (TextUtils.isEmpty(newPassword)) {
                                    etNewPassword.setError("Vui lòng nhập mật khẩu mới");
                                    etNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (newPassword.length() < 6) {
                                    etNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
                                    etNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (TextUtils.isEmpty(confirmNewPassword)) {
                                    etConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu mới");
                                    etConfirmNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (!newPassword.equals(confirmNewPassword)) {
                                    etConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
                                    etConfirmNewPassword.requestFocus();
                                    newPasswordValid = false;
                                }

                                // 4. Nếu mật khẩu mới hợp lệ -> Tiến hành cập nhật
                                if (newPasswordValid) {
                                    updatePasswordInFirebase(user, newPassword, dialog, positiveButton);
                                }


                            } else {
                                // ---- Xác thực thất bại ----
                                Log.w(TAG, "Re-authentication failed", reauthTask.getException());
                                // Hiển thị lỗi cụ thể cho mật khẩu hiện tại
                                if (reauthTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                                } else {
                                    etCurrentPassword.setError("Xác thực thất bại");
                                    Toast.makeText(SettingActivity.this, "Lỗi: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                etCurrentPassword.requestFocus();

                            }
                        });
            });
        });

        dialog.show();
    }

    // Hàm riêng để cập nhật mật khẩu sau khi đã xác thực và kiểm tra pass mới
    private void updatePasswordInFirebase(FirebaseUser user, String newPassword, AlertDialog dialog, Button positiveButton) {
        // Hiển thị tiến trình
        Toast.makeText(SettingActivity.this, "Đang cập nhật mật khẩu...", Toast.LENGTH_SHORT).show();
        if (positiveButton != null) positiveButton.setEnabled(false);

        user.updatePassword(newPassword)
                .addOnCompleteListener(updateTask -> {
                    if (positiveButton != null) positiveButton.setEnabled(true);

                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                        Toast.makeText(SettingActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Log.w(TAG, "Error updating password", updateTask.getException());
                        Toast.makeText(SettingActivity.this, "Đổi mật khẩu thất bại: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(SettingActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}