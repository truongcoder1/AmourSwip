package vn.edu.tlu.cse.amourswip.view.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import vn.edu.tlu.cse.amourswip.R;

public class trEditProfileActivity extends AppCompatActivity {

    private ImageView backArrow;
    private ImageView settingsIcon;
    private ImageView avatarImage;
    private TextView userInfo;
    private Button editPhotoButton;
    private EditText editFullName;
    private EditText editDob;
    private EditText editInterests;
    private EditText editLocation;
    private EditText editRelationship;
    private EditText editReligion;
    private EditText editEducation;
    private EditText editOccupation;
    private Button confirmButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        backArrow = findViewById(R.id.back_arrow);
        settingsIcon = findViewById(R.id.settings_icon);
        avatarImage = findViewById(R.id.avatar_image);
        userInfo = findViewById(R.id.user_info);
        editPhotoButton = findViewById(R.id.edit_photo_button);
        editFullName = findViewById(R.id.edit_full_name);
        editDob = findViewById(R.id.edit_dob);
        editInterests = findViewById(R.id.edit_interests);
        editLocation = findViewById(R.id.edit_location);
        editRelationship = findViewById(R.id.edit_relationship);
        editReligion = findViewById(R.id.edit_religion);
        editEducation = findViewById(R.id.edit_education);
        editOccupation = findViewById(R.id.edit_occupation);
        confirmButton = findViewById(R.id.confirm_button);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        loadUserProfile();

        backArrow.setOnClickListener(v -> finish());

        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(trEditProfileActivity.this, xSettingActivity.class);
            startActivity(intent);
        });

        editPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(trEditProfileActivity.this, trEditPhotosActivity.class);
            startActivity(intent);
        });

        confirmButton.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String interests = snapshot.child("interests").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String relationship = snapshot.child("relationship").getValue(String.class);
                    String religion = snapshot.child("religion").getValue(String.class);
                    String education = snapshot.child("education").getValue(String.class);
                    String occupation = snapshot.child("occupation").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);

                    String userInfoText = "Tên: " + (name != null ? name : "N/A") + "\n" +
                            "Mô tả: " + (description != null ? description : "N/A");
                    userInfo.setText(userInfoText);

                    editFullName.setText(name != null ? name : "");
                    editDob.setText(dob != null ? dob : "");
                    editInterests.setText(interests != null ? interests : "");
                    editLocation.setText(location != null ? location : "");
                    editRelationship.setText(relationship != null ? relationship : "");
                    editReligion.setText(religion != null ? religion : "");
                    editEducation.setText(education != null ? education : "");
                    editOccupation.setText(occupation != null ? occupation : "");

                    // Lấy danh sách ảnh từ trường "photos"
                    DataSnapshot photosSnapshot = snapshot.child("photos");
                    String avatarUrl = null;
                    if (photosSnapshot.exists()) {
                        for (DataSnapshot photo : photosSnapshot.getChildren()) {
                            String photoUrl = photo.getValue(String.class);
                            if (photoUrl != null && avatarUrl == null) { // Lấy ảnh đầu tiên làm avatar
                                avatarUrl = photoUrl;
                                break;
                            }
                        }
                    }

                    // Hiển thị ảnh đại diện
                    if (avatarUrl != null) {
                        Glide.with(trEditProfileActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(avatarImage);
                    } else {
                        avatarImage.setImageResource(R.drawable.gai1);
                    }
                } else {
                    Toast.makeText(trEditProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(trEditProfileActivity.this, "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String name = editFullName.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String interests = editInterests.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String relationship = editRelationship.getText().toString().trim();
        String religion = editReligion.getText().toString().trim();
        String education = editEducation.getText().toString().trim();
        String occupation = editOccupation.getText().toString().trim();

        userRef.child("name").setValue(name);
        userRef.child("dob").setValue(dob);
        userRef.child("interests").setValue(interests);
        userRef.child("location").setValue(location);
        userRef.child("relationship").setValue(relationship);
        userRef.child("religion").setValue(religion);
        userRef.child("education").setValue(education);
        userRef.child("occupation").setValue(occupation);
        userRef.child("description").setValue("Đang " + (relationship.isEmpty() ? "yêu" : relationship) + ", thích " + (interests.isEmpty() ? "đi du lịch..." : interests));

        Toast.makeText(trEditProfileActivity.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
        finish();
    }
}