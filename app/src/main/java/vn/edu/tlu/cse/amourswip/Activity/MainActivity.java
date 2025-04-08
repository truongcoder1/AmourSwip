package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.edu.tlu.cse.amourswip.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private LinearLayout navigationBar;
    private ImageButton navDownloadButton;
    private ImageButton navLikeButton;
    private ImageButton navChatButton;
    private ImageButton navProfileButton;

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

        // Khởi tạo các view bằng findViewById
        navigationBar = findViewById(R.id.navigation_bar);
        navDownloadButton = findViewById(R.id.nav_download_button);
        navLikeButton = findViewById(R.id.nav_like_button);
        navChatButton = findViewById(R.id.nav_chat_button);
        navProfileButton = findViewById(R.id.nav_profile_button);

        // Xử lý sự kiện nhấn các nút trong navigation bar
        navDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Swipe button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        navLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Like button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        navChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chat button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        navProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Profile button clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}