package vn.edu.tlu.cse.amourswip.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;

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

                    // Lắng nghe sự thay đổi của fragment để ẩn/hiện BottomNavigationView
                    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                        if (destination.getId() == R.id.chatFragment) {
                            // Ẩn BottomNavigationView trong ChatFragment
                            bottomNavigationView.setVisibility(View.GONE);
                        } else {
                            // Hiện BottomNavigationView trong các fragment khác
                            bottomNavigationView.setVisibility(View.VISIBLE);
                        }
                    });

                    // Xử lý sự kiện nhấn vào các mục trong BottomNavigationView
                    bottomNavigationView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.swipeFragment) {
                            navController.navigate(R.id.swipeFragment);
                            return true;
                        } else if (itemId == R.id.listChatFragment) {
                            navController.navigate(R.id.listChatFragment);
                            return true;
                        } else if (itemId == R.id.profileFragment) {
                            navController.navigate(R.id.profileFragment);
                            return true;
                        } else if (itemId == R.id.likeFragment) {
                            navController.navigate(R.id.likeFragment);
                            Toast.makeText(MainActivity.this, "Like button clicked - Chưa triển khai", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    });

                    // Xử lý điều hướng từ Intent (ví dụ: từ ProfileMyFriendActivity)
                    Intent intent = getIntent();
                    if (intent != null && intent.hasExtra("navigateTo")) {
                        String navigateTo = intent.getStringExtra("navigateTo");
                        if ("chatFragment".equals(navigateTo)) {
                            String friendId = intent.getStringExtra("friendId");
                            Bundle bundle = new Bundle();
                            bundle.putString("friendId", friendId);
                            navController.navigate(R.id.chatFragment, bundle);
                        }
                    }
                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, "Lỗi khởi tạo NavController: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Không tìm thấy NavHostFragment", Toast.LENGTH_LONG).show();
        }
    }
}