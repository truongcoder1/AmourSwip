package vn.edu.tlu.cse.amourswip.view.activity.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import vn.edu.tlu.cse.amourswip.view.adapter.trPhotoAdapter;
import vn.edu.tlu.cse.amourswip.R;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class trEditPhotosActivity extends AppCompatActivity {

    private ImageView backArrow;
    private ImageView settingsIcon;
    private GridView photoGrid;
    private Button addButton;
    private Button deleteButton;
    private Button confirmButton;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private List<String> photoUrls = new ArrayList<>();
    private trPhotoAdapter photoAdapter;
    private int selectedPosition = -1; // Vị trí ảnh được chọn để xóa
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photos);

        // Khởi tạo các view bằng findViewById
        backArrow = findViewById(R.id.back_arrow);
        settingsIcon = findViewById(R.id.settings_icon);
        photoGrid = findViewById(R.id.photo_grid);
        addButton = findViewById(R.id.add_button);
        deleteButton = findViewById(R.id.delete_button);
        confirmButton = findViewById(R.id.confirm_button);

        // Khởi tạo Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference().child("user_photos").child(userId);

        // Khởi tạo GridView adapter
        photoAdapter = new trPhotoAdapter(this, photoUrls);
        photoGrid.setAdapter(photoAdapter);

        // Lấy danh sách ảnh từ Firebase
        loadPhotos();

        // Khởi tạo ActivityResultLauncher để chọn ảnh
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    uploadImageToFirebase(imageUri);
                }
            }
        });

        // Xử lý nút quay lại
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút cài đặt
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(trEditPhotosActivity.this, xSettingActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý nút thêm ảnh
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pickImageLauncher.launch(intent);
            }
        });

        // Xử lý nút xóa ảnh
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != -1) {
                    deletePhoto(selectedPosition);
                    selectedPosition = -1; // Reset vị trí đã chọn
                } else {
                    Toast.makeText(trEditPhotosActivity.this, "Vui lòng chọn một ảnh để xóa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý sự kiện chọn ảnh trong GridView
        photoGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                Toast.makeText(trEditPhotosActivity.this, "Đã chọn ảnh số " + (position + 1), Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút xác nhận
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhotosToFirebase();
            }
        });
    }

    private void loadPhotos() {
        userRef.child("photos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoUrls.clear();
                for (DataSnapshot photoSnapshot : snapshot.getChildren()) {
                    String photoUrl = photoSnapshot.getValue(String.class);
                    if (photoUrl != null) {
                        photoUrls.add(photoUrl);
                    }
                }
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(trEditPhotosActivity.this, "Lỗi khi tải ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Tạo tên file duy nhất cho ảnh
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        // Tải ảnh lên Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL của ảnh đã tải lên
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        photoUrls.add(downloadUrl);
                        photoAdapter.notifyDataSetChanged();
                        Toast.makeText(trEditPhotosActivity.this, "Thêm ảnh thành công", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(trEditPhotosActivity.this, "Lỗi khi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletePhoto(int position) {
        if (position >= 0 && position < photoUrls.size()) {
            String photoUrl = photoUrls.get(position);
            // Xóa ảnh khỏi danh sách
            photoUrls.remove(position);
            photoAdapter.notifyDataSetChanged();

            // Xóa ảnh khỏi Firebase Storage (nếu cần)
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(trEditPhotosActivity.this, "Xóa ảnh thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(trEditPhotosActivity.this, "Lỗi khi xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void savePhotosToFirebase() {
        // Lưu danh sách ảnh mới lên Firebase Realtime Database
        userRef.child("photos").setValue(photoUrls)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(trEditPhotosActivity.this, "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(trEditPhotosActivity.this, "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}