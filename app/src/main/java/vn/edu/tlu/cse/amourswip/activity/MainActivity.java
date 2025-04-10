package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.activity.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase Authentication và Database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        // Kiểm tra trạng thái đăng nhập
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Khởi tạo BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Đảm bảo layout đã được inflate hoàn toàn trước khi khởi tạo NavController
        View navHostFragmentView = findViewById(R.id.nav_host_fragment);
        if (navHostFragmentView != null) {
            navHostFragmentView.post(() -> {
                try {
                    navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                    // Liên kết BottomNavigationView với NavController
                    NavigationUI.setupWithNavController(bottomNavigationView, navController);

                    // Xử lý sự kiện nhấn vào các mục trong BottomNavigationView
                    bottomNavigationView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.swipeFragment) {
                            navController.navigate(R.id.swipeFragment);
                            return true;
                        } else if (itemId == R.id.listChatFragment) {
                            Toast.makeText(MainActivity.this, "Chat button clicked - Chưa triển khai", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.profileFragment) {
                            Toast.makeText(MainActivity.this, "Profile button clicked - Chưa triển khai", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.likeFragment) {
                            Toast.makeText(MainActivity.this, "Like button clicked - Chưa triển khai", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    });
                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, "Lỗi khởi tạo NavController: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Không tìm thấy NavHostFragment", Toast.LENGTH_LONG).show();
        }
    }
}