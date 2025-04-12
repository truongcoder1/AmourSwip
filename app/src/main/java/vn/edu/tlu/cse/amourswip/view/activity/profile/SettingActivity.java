package vn.edu.tlu.cse.amourswip.view.activity.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.signup.MapActivity;
import vn.edu.tlu.cse.amourswip.view.activity.signup.SignInActivity;

public class SettingActivity extends AppCompatActivity {

    private Button notificationButton;
    private Button locationButton;
    private Button changePasswordButton;
    private Button logoutButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Khởi tạo các view bằng findViewById
        notificationButton = findViewById(R.id.notification_button);
        locationButton = findViewById(R.id.location_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutButton = findViewById(R.id.logout_button);

        // Xử lý nút Bật/Tắt thông báo (chưa triển khai logic bật/tắt thông báo, chỉ hiển thị Toast)
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, "Chức năng Bật/Tắt thông báo chưa được triển khai", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút Phần vị tìm kiếm (giả định điều hướng đến MapActivity)
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý nút Đổi mật khẩu (chưa triển khai logic đổi mật khẩu, chỉ hiển thị Toast)
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, "Chức năng Đổi mật khẩu chưa được triển khai", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút Đăng xuất
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(SettingActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                // Điều hướng về màn hình đăng nhập (giả định là LoginActivity)
                Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}