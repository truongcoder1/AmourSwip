package vn.edu.tlu.cse.amourswip.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.edu.tlu.cse.amourswip.view.adapter.PhotoAdapter;
import vn.edu.tlu.cse.amourswip.R;
import java.util.ArrayList;
import java.util.List;

public class ProfileMyFriendActivity extends AppCompatActivity {

    private ImageView backArrow;
    private ImageView avatarImage;
    private TextView title;
    private TextView userInfo;
    private Button messageButton;
    private Button cancelMatchButton;
    private GridView photoGrid;
    private DatabaseReference userRef;
    private List<String> photoUrls = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private String friendId; // ID của người bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_myfriend);

        // Khởi tạo các view bằng findViewById
        backArrow = findViewById(R.id.back_arrow);
        avatarImage = findViewById(R.id.avatar_image);
        title = findViewById(R.id.title);
        userInfo = findViewById(R.id.user_info);
        messageButton = findViewById(R.id.message_button);
        cancelMatchButton = findViewById(R.id.cancel_match_button);
        photoGrid = findViewById(R.id.photo_grid);

        // Lấy friendId từ Intent
        friendId = getIntent().getStringExtra("friendId");

        if (friendId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId);

        // Khởi tạo GridView adapter
        photoAdapter = new PhotoAdapter(this, photoUrls);
        photoGrid.setAdapter(photoAdapter);

        // Lấy và hiển thị thông tin người dùng
        loadFriendProfile();

        // Xử lý nút quay lại
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút nhắn tin
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Điều hướng về ChatFragment với friendId
                Intent intent = new Intent(ProfileMyFriendActivity.this, MainActivity.class);
                intent.putExtra("friendId", friendId);
                intent.putExtra("navigateTo", "chatFragment");
                startActivity(intent);
                finish();
            }
        });

        // Xử lý nút hủy ghép đôi
        cancelMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelMatch();
            }
        });
    }

    private void loadFriendProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy thông tin người dùng
                    String name = snapshot.child("name").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String interests = snapshot.child("interests").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);

                    // Tính khoảng cách (giả định vị trí của người dùng hiện tại)
                    double distance = calculateDistance(latitude, longitude);

                    // Hiển thị thông tin lên TextView
                    String userInfoText = "Tên: " + (name != null ? name : "N/A") + "\n" +
                            "Giới tính: " + (gender != null ? gender : "N/A") + "\n" +
                            "Sở thích: " + (interests != null ? interests : "N/A") + "\n" +
                            "Mô tả: " + (description != null ? description : "N/A") + "\n\n" +
                            "Khoảng cách: " + String.format("%.1f km", distance);
                    userInfo.setText(userInfoText);

                    // Cập nhật tiêu đề
                    title.setText("Hồ sơ của " + (name != null ? name.split(" ")[0] : "N/A"));

                    // Hiển thị ảnh đại diện
                    if (avatarUrl != null) {
                        Glide.with(ProfileMyFriendActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(avatarImage);
                    }

                    // Lấy danh sách ảnh từ trường "photos"
                    DataSnapshot photosSnapshot = snapshot.child("photos");
                    photoUrls.clear();
                    for (DataSnapshot photo : photosSnapshot.getChildren()) {
                        String photoUrl = photo.getValue(String.class);
                        if (photoUrl != null) {
                            photoUrls.add(photoUrl);
                        }
                    }
                    photoAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ProfileMyFriendActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileMyFriendActivity.this, "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistance(Double friendLat, Double friendLon) {
        // Giả định vị trí của người dùng hiện tại (cần lấy từ GPS hoặc dữ liệu người dùng)
        double myLat = 21.0285; // Ví dụ: Vị trí của bạn (Hà Nội)
        double myLon = 105.8542;

        if (friendLat == null || friendLon == null) {
            return 0.0;
        }

        // Tính khoảng cách bằng công thức Haversine
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(friendLat - myLat);
        double lonDistance = Math.toRadians(friendLon - myLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(myLat)) * Math.cos(Math.toRadians(friendLat)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Khoảng cách tính bằng km
    }

    private void cancelMatch() {
        // Giả định bạn có một node "matches" trong Firebase để lưu thông tin ghép đôi
        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Xóa thông tin ghép đôi
        matchesRef.child(currentUserId).child(friendId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    matchesRef.child(friendId).child(currentUserId).removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(ProfileMyFriendActivity.this, "Đã hủy ghép đôi", Toast.LENGTH_SHORT).show();
                                // Quay lại màn hình chính
                                Intent intent = new Intent(ProfileMyFriendActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileMyFriendActivity.this, "Lỗi khi hủy ghép đôi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}