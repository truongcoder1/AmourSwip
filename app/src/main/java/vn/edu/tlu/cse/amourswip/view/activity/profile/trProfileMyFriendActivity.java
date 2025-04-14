package vn.edu.tlu.cse.amourswip.view.activity.profile;

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

import vn.edu.tlu.cse.amourswip.view.activity.main.MainActivity;
import vn.edu.tlu.cse.amourswip.view.adapter.trPhotoAdapter;
import vn.edu.tlu.cse.amourswip.R;
import java.util.ArrayList;
import java.util.List;

public class trProfileMyFriendActivity extends AppCompatActivity {

    private ImageView backArrow;
    private ImageView avatarImage;
    private TextView title;
    private TextView userInfo;
    private Button cancelMatchButton;
    private GridView photoGrid;
    private DatabaseReference userRef;
    private List<String> photoUrls = new ArrayList<>();
    private trPhotoAdapter photoAdapter;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_myfriend);

        backArrow = findViewById(R.id.back_arrow);
        avatarImage = findViewById(R.id.avatar_image);
        title = findViewById(R.id.title);
        userInfo = findViewById(R.id.user_info);
        cancelMatchButton = findViewById(R.id.cancel_match_button);
        photoGrid = findViewById(R.id.photo_grid);

        friendId = getIntent().getStringExtra("friendId");
        boolean fromLikeFragment = getIntent().getBooleanExtra("fromLikeFragment", false);

        if (friendId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ẩn nút "Hủy ghép" nếu từ likeFragment
        if (fromLikeFragment) {
            cancelMatchButton.setVisibility(View.GONE);
        }

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId);

        photoAdapter = new trPhotoAdapter(this, photoUrls);
        photoGrid.setAdapter(photoAdapter);

        loadFriendProfile();

        backArrow.setOnClickListener(v -> finish());

        cancelMatchButton.setOnClickListener(v -> cancelMatch());
    }

    private void loadFriendProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String interests = snapshot.child("interests").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);

                    double distance = calculateDistance(latitude, longitude);

                    String userInfoText = "Tên: " + (name != null ? name : "N/A") + "\n" +
                            "Giới tính: " + (gender != null ? gender : "N/A") + "\n" +
                            "Sở thích: " + (interests != null ? interests : "N/A") + "\n" +
                            "Mô tả: " + (description != null ? description : "N/A") + "\n\n" +
                            "Khoảng cách: " + String.format("%.1f km", distance);
                    userInfo.setText(userInfoText);

                    title.setText("Hồ sơ của " + (name != null ? name.split(" ")[0] : "N/A"));

                    // Lấy danh sách ảnh từ trường "photos"
                    DataSnapshot photosSnapshot = snapshot.child("photos");
                    photoUrls.clear();
                    String avatarUrl = null;
                    if (photosSnapshot.exists()) {
                        for (DataSnapshot photo : photosSnapshot.getChildren()) {
                            String photoUrl = photo.getValue(String.class);
                            if (photoUrl != null) {
                                photoUrls.add(photoUrl);
                                if (avatarUrl == null) { // Lấy ảnh đầu tiên làm avatar
                                    avatarUrl = photoUrl;
                                }
                            }
                        }
                    }

                    // Hiển thị ảnh đại diện
                    if (avatarUrl != null) {
                        Glide.with(trProfileMyFriendActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(avatarImage);
                    } else {
                        avatarImage.setImageResource(R.drawable.gai1);
                    }

                    photoAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(trProfileMyFriendActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(trProfileMyFriendActivity.this, "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistance(Double friendLat, Double friendLon) {
        double myLat = 21.0285; // Vị trí của bạn (Hà Nội - ví dụ)
        double myLon = 105.8542;

        if (friendLat == null || friendLon == null) {
            return 0.0;
        }

        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(friendLat - myLat);
        double lonDistance = Math.toRadians(friendLon - myLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(myLat)) * Math.cos(Math.toRadians(friendLat)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void cancelMatch() {
        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        matchesRef.child(currentUserId).child(friendId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    matchesRef.child(friendId).child(currentUserId).removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(trProfileMyFriendActivity.this, "Đã hủy ghép đôi", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(trProfileMyFriendActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(trProfileMyFriendActivity.this, "Lỗi khi hủy ghép đôi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}