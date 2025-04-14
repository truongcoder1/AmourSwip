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
import android.util.Log;
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
import vn.edu.tlu.cse.amourswip.view.activity.signup.xSignInActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth auth;
    private DatabaseReference database;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_NOTIFICATIONS = 101;
    private static final String CHANNEL_ID_MATCH = "MatchNotifications";
    private static final String CHANNEL_NAME_MATCH = "Match Notifications";
    private static final String CHANNEL_ID_LIKE = "LikeNotifications";
    private static final String CHANNEL_NAME_LIKE = "Like Notifications";
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, xSignInActivity.class));
            finish();
            return;
        }

        requestNotificationPermission();
        listenForMatches();
        listenForLikes();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        View navHostFragmentView = findViewById(R.id.nav_host_fragment);
        if (navHostFragmentView != null) {
            navHostFragmentView.post(() -> {
                try {
                    navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(bottomNavigationView, navController);

                    // Debug điều hướng
                    bottomNavigationView.setOnItemSelectedListener(item -> {
                        Log.d(TAG, "BottomNavigation item selected: " + item.getItemId() + " (" + getResourceName(item.getItemId()) + ")");
                        try {
                            boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
                            Log.d(TAG, "NavigationUI.onNavDestinationSelected result: " + navigated);
                            if (!navigated) {
                                // Điều hướng thủ công nếu NavigationUI thất bại
                                int destinationId = item.getItemId();
                                if (destinationId == R.id.swipeFragment || destinationId == R.id.likeFragment ||
                                        destinationId == R.id.listChatFragment || destinationId == R.id.profileFragment) {
                                    navController.navigate(destinationId);
                                    Log.d(TAG, "Manually navigated to: " + getResourceName(destinationId));
                                    return true;
                                }
                            }
                            return navigated;
                        } catch (Exception e) {
                            Log.e(TAG, "Navigation error: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });

                    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                        int destinationId = destination.getId();
                        Log.d(TAG, "Destination changed to: " + destinationId + " (" + getResourceName(destinationId) + ")");
                        if (destinationId == R.id.chatUserFragment || destinationId == R.id.chatAIFragment) {
                            bottomNavigationView.setVisibility(View.GONE);
                            Log.d(TAG, "BottomNavigationView visibility: GONE");
                        } else {
                            bottomNavigationView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "BottomNavigationView visibility: VISIBLE");
                        }
                    });

                    Intent intent = getIntent();
                    if (intent != null && intent.hasExtra("navigateTo")) {
                        String navigateTo = intent.getStringExtra("navigateTo");
                        if ("chatUserFragment".equals(navigateTo)) {
                            String friendId = intent.getStringExtra("friendId");
                            Bundle bundle = new Bundle();
                            bundle.putString("userId", friendId);
                            bundle.putString("userName", "");
                            navController.navigate(R.id.chatUserFragment, bundle);
                        }
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "NavController initialization error: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Lỗi khởi tạo NavController: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Không tìm thấy NavHostFragment", Toast.LENGTH_LONG).show();
        }
    }

    // Hàm phụ để lấy tên tài nguyên từ ID
    private String getResourceName(int resId) {
        try {
            return getResources().getResourceName(resId);
        } catch (Exception e) {
            return "Unknown";
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
                Toast.makeText(this, "Ứng dụng cần quyền thông báo để gửi thông tin khi có lượt thích hoặc match mới", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void listenForMatches() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference matchesRef = database.child("matches").child(userId);

        matchesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String matchedUserId = snapshot.getKey();
                boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
                if (!notificationsEnabled) {
                    return;
                }
                database.child("users").child(matchedUserId).child("name").get().addOnCompleteListener(nameTask -> {
                    if (nameTask.isSuccessful()) {
                        String matchedUserName = nameTask.getResult().getValue(String.class);
                        if (matchedUserName == null) matchedUserName = "Một người dùng";
                        sendLocalNotification(CHANNEL_ID_MATCH, CHANNEL_NAME_MATCH, "Bạn có match mới!", "Bạn đã match với " + matchedUserName + "!", MainActivity.class);
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

    private void listenForLikes() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference likedByRef = database.child("likedBy").child(userId);

        likedByRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String likerUserId = snapshot.getKey();
                boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
                if (!notificationsEnabled) {
                    return;
                }
                database.child("users").child(likerUserId).child("name").get().addOnCompleteListener(nameTask -> {
                    if (nameTask.isSuccessful()) {
                        String likerName = nameTask.getResult().getValue(String.class);
                        if (likerName == null) likerName = "Một người dùng";
                        sendLocalNotification(CHANNEL_ID_LIKE, CHANNEL_NAME_LIKE, "Lượt thích mới!", likerName + " đã thích bạn!", MainActivity.class);
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
                Toast.makeText(MainActivity.this, "Lỗi lắng nghe lượt thích: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendLocalNotification(String channelId, String channelName, String title, String message, Class<?> targetActivity) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.gai1)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}