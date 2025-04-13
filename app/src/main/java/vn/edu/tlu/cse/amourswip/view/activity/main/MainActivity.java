package vn.edu.tlu.cse.amourswip.view.activity.main;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.signup.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_NOTIFICATIONS = 101;
    private static final String CHANNEL_ID = "MatchNotifications";
    private static final String CHANNEL_NAME = "Match Notifications";
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase Authentication và Database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Kiểm tra trạng thái đăng nhập
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Yêu cầu quyền thông báo
        requestNotificationPermission();

        // Lắng nghe match mới
        listenForMatches();

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
                        int destinationId = destination.getId();
                        if (destinationId == R.id.chatUserFragment || destinationId == R.id.chatAIFragment) {
                            // Ẩn BottomNavigationView trong ChatUserFragment và ChatAIFragment
                            bottomNavigationView.setVisibility(View.GONE);
                        } else {
                            // Hiện BottomNavigationView trong các fragment khác
                            bottomNavigationView.setVisibility(View.VISIBLE);
                        }
                    });

                    // Xử lý điều hướng từ Intent (ví dụ: từ ProfileMyFriendActivity)
                    Intent intent = getIntent();
                    if (intent != null && intent.hasExtra("navigateTo")) {
                        String navigateTo = intent.getStringExtra("navigateTo");
                        if ("chatUserFragment".equals(navigateTo)) {
                            String friendId = intent.getStringExtra("friendId");
                            Bundle bundle = new Bundle();
                            bundle.putString("userId", friendId);
                            // Có thể lấy userName từ Firebase nếu cần
                            bundle.putString("userName", ""); // Cập nhật sau nếu cần userName
                            navController.navigate(R.id.chatUserFragment, bundle);
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

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền thông báo để gửi thông tin khi có match mới", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void listenForMatches() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference matchesRef = database.child("matches").child(userId);

        // Lắng nghe sự kiện khi có match mới
        matchesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String matchedUserId = snapshot.getKey();

                // Kiểm tra trạng thái bật/tắt thông báo từ SharedPreferences
                boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
                if (!notificationsEnabled) {
                    // Thông báo bị tắt, không gửi thông báo
                    return;
                }

                // Lấy thông tin người match
                database.child("users").child(matchedUserId).child("name").get().addOnCompleteListener(nameTask -> {
                    if (nameTask.isSuccessful()) {
                        String matchedUserName = nameTask.getResult().getValue(String.class);
                        if (matchedUserName == null) matchedUserName = "Một người dùng";

                        // Gửi thông báo cục bộ
                        sendLocalNotification(matchedUserName);
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi lắng nghe match: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendLocalNotification(String matchedUserName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Tạo kênh thông báo (cho Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo Intent để mở MainActivity khi nhấn vào thông báo
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.gai1) // Thay bằng icon của bạn
                .setContentTitle("Bạn có match mới!")
                .setContentText("Bạn đã match với " + matchedUserName + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Gửi thông báo
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}