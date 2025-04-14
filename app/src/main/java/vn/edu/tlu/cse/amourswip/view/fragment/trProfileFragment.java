package vn.edu.tlu.cse.amourswip.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.profile.trEditProfileActivity;
import vn.edu.tlu.cse.amourswip.view.activity.profile.xSettingActivity;
import vn.edu.tlu.cse.amourswip.view.adapter.trPhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class trProfileFragment extends Fragment {

    private ImageView backArrow;
    private ImageView settingsIcon;
    private ImageView avatarImage;
    private TextView userInfo;
    private Button editButton;
    private GridView photoGrid;
    private DatabaseReference userRef;
    private List<String> photoUrls = new ArrayList<>();
    private trPhotoAdapter photoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        backArrow = view.findViewById(R.id.back_arrow);
        settingsIcon = view.findViewById(R.id.settings_icon);
        avatarImage = view.findViewById(R.id.avatar_image);
        userInfo = view.findViewById(R.id.user_info);
        editButton = view.findViewById(R.id.edit_button);
        photoGrid = view.findViewById(R.id.photo_grid);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        photoAdapter = new trPhotoAdapter(getContext(), photoUrls);
        photoGrid.setAdapter(photoAdapter);

        loadUserProfile();

        backArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), xSettingActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), trEditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String interests = snapshot.child("interests").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);

                    String userInfoText = "Tên: " + (name != null ? name : "N/A") + "\n" +
                            "Giới tính: " + (gender != null ? gender : "N/A") + "\n" +
                            "Sở thích: " + (interests != null ? interests : "N/A") + "\n" +
                            "Mô tả: " + (description != null ? description : "N/A");
                    userInfo.setText(userInfoText);

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
                        Glide.with(getContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(avatarImage);
                    } else {
                        avatarImage.setImageResource(R.drawable.gai1);
                    }

                    photoAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}