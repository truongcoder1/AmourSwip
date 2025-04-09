package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import vn.edu.tlu.cse.amourswip.Activity.MapActivity;
import vn.edu.tlu.cse.amourswip.Datalayer.Repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

import java.util.ArrayList;
import java.util.List;

public class EditPhotosActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private StorageReference storageRef;
    private List<String> photoUrls = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_myimage);

        userRepository = new UserRepository();
        storageRef = FirebaseStorage.getInstance().getReference("user_photos");

        Button btnUploadPhoto = findViewById(R.id.upload_button);
        Button btnTakePhoto = findViewById(R.id.camera_button);
        Button btnNext = findViewById(R.id.next_button);
        Button btnSkip = findViewById(R.id.skip_button);

        // Khởi tạo ActivityResultLauncher để chọn ảnh từ thư viện
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadPhoto(uri);
            }
        });

        // Khởi tạo ActivityResultLauncher để chụp ảnh bằng camera
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri == null) {
                    // Nếu không có URI (trường hợp chụp ảnh trực tiếp), lấy từ extras
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") != null) {
                        // Xử lý ảnh chụp (cần lưu ảnh vào bộ nhớ để lấy URI, ở đây tôi giả định bạn sẽ xử lý thêm)
                        Toast.makeText(this, "Ảnh đã chụp, nhưng cần xử lý thêm để lưu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    uploadPhoto(uri);
                }
            }
        });

        btnUploadPhoto.setOnClickListener(v -> {
            // Mở trình chọn ảnh
            pickImageLauncher.launch("image/*");
        });

        btnTakePhoto.setOnClickListener(v -> {
            // Mở camera để chụp ảnh
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (photoUrls.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất một ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật danh sách URL ảnh vào Realtime Database
            userRepository.updateUserField("photos", photoUrls, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    // Chuyển đến màn hình yêu cầu GPS
                    Intent intent = new Intent(EditPhotosActivity.this, MapActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(EditPhotosActivity.this, "Lỗi khi cập nhật ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSkip.setOnClickListener(v -> {
            // Chuyển đến màn hình yêu cầu GPS mà không lưu ảnh
            Intent intent = new Intent(EditPhotosActivity.this, MapActivity.class);
            startActivity(intent);
        });
    }

    private void uploadPhoto(Uri uri) {
        String userId = userRepository.getCurrentUserId();
        if (userId != null) {
            StorageReference photoRef = storageRef.child(userId + "/" + System.currentTimeMillis() + ".jpg");

            photoRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            photoUrls.add(downloadUri.toString());
                            Toast.makeText(EditPhotosActivity.this, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditPhotosActivity.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(EditPhotosActivity.this, "Không tìm thấy người dùng hiện tại", Toast.LENGTH_SHORT).show();
        }
    }
}