package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class EditProfileActivity extends AppCompatActivity {

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

        // Khởi tạo các view bằng findViewById
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

        // Khởi tạo Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        // Lấy và hiển thị thông tin người dùng
        loadUserProfile();

        // Xử lý nút quay lại
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng activity và quay lại màn hình trước
            }
        });

        // Xử lý nút cài đặt
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý nút sửa ảnh
        editPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, EditPhotosActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý nút xác nhận
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy thông tin người dùng
                    String name = snapshot.child("name").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String interests = snapshot.child("interests").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String relationship = snapshot.child("relationship").getValue(String.class);
                    String religion = snapshot.child("religion").getValue(String.class);
                    String education = snapshot.child("education").getValue(String.class);
                    String occupation = snapshot.child("occupation").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);

                    // Hiển thị thông tin lên TextView
                    String userInfoText = "Tên: " + (name != null ? name : "N/A") + "\n" +
                            "Mô tả: " + (description != null ? description : "N/A");
                    userInfo.setText(userInfoText);

                    // Hiển thị thông tin lên các EditText
                    editFullName.setText(name != null ? name : "");
                    editDob.setText(dob != null ? dob : "");
                    editInterests.setText(interests != null ? interests : "");
                    editLocation.setText(location != null ? location : "");
                    editRelationship.setText(relationship != null ? relationship : "");
                    editReligion.setText(religion != null ? religion : "");
                    editEducation.setText(education != null ? education : "");
                    editOccupation.setText(occupation != null ? occupation : "");

                    // Hiển thị ảnh đại diện
                    if (avatarUrl != null) {
                        Glide.with(EditProfileActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(avatarImage);
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Cập nhật thông tin lên Firebase
        userRef.child("name").setValue(name);
        userRef.child("dob").setValue(dob);
        userRef.child("interests").setValue(interests);
        userRef.child("location").setValue(location);
        userRef.child("relationship").setValue(relationship);
        userRef.child("religion").setValue(religion);
        userRef.child("education").setValue(education);
        userRef.child("occupation").setValue(occupation);
        userRef.child("description").setValue("Đang " + (relationship.isEmpty() ? "yêu" : relationship) + ", thích " + (interests.isEmpty() ? "đi du lịch..." : interests));

        Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();

        // Đóng activity và quay lại màn hình trước
        finish();
    }
}