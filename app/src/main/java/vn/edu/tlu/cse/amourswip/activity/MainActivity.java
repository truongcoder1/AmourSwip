package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.Activity.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private BottomNavigationView bottomNavigationView;

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

        // Khởi tạo BottomNavigationView bằng findViewById
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Xử lý sự kiện nhấn các tab trong BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.swipeFragment) {
                    Toast.makeText(MainActivity.this, "Swipe button clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.listChatFragment) {
                    Toast.makeText(MainActivity.this, "Chat button clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.profileFragment) {
                    Toast.makeText(MainActivity.this, "Profile button clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.likeFragment) {
                    Toast.makeText(MainActivity.this, "Like button clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
}