package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.Manifest;
import android.content.ActivityNotFoundException;
// import android.content.ContentValues; // Không sử dụng, có thể xóa
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView; // Thêm import này
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
// --- Optional: Import Glide/Picasso for better image loading ---
// import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.repository.xUserRepository;

public class xMyimageActivity extends AppCompatActivity {

    private static final String TAG = "MyimageActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private xUserRepository userRepository;
    private StorageReference storageRef;
    private final List<String> photoUrls = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private Uri photoUri; // Uri cho ảnh chụp từ camera

    // --- Biến tham chiếu đến ImageView ---
    private ImageView selectedPhotoPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo tên layout khớp với tên file XML của bạn
        setContentView(R.layout.acitivity_myimage); // Hoặc tên layout đúng

        userRepository = new xUserRepository();
        storageRef = FirebaseStorage.getInstance().getReference("user_photos");

        // --- Lấy tham chiếu đến các View ---
        selectedPhotoPreview = findViewById(R.id.selected_photo_preview); // Tìm ImageView
        Button btnUploadPhoto = findViewById(R.id.upload_button);
        Button btnTakePhoto = findViewById(R.id.camera_button);
        Button btnNext = findViewById(R.id.next_button);
        Button btnSkip = findViewById(R.id.skip_button);

        // --- Khởi tạo các Launchers ---
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Ảnh được chọn từ thư viện: " + uri.toString());

                // --- Hiển thị ảnh lên ImageView ---
                displayImage(uri); // Gọi hàm hiển thị ảnh

                uploadPhoto(uri); // Tiếp tục tải ảnh lên
            } else {
                Log.d(TAG, "Không có ảnh nào được chọn từ thư viện.");
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (photoUri != null) {
                    Log.d(TAG, "Ảnh chụp thành công, Uri: " + photoUri.toString());

                    // --- Hiển thị ảnh lên ImageView ---
                    displayImage(photoUri); // Gọi hàm hiển thị ảnh

                    // Không cần gọi addPicToGallery nếu dùng getExternalFilesDir
                    // addPicToGallery(photoUri);

                    uploadPhoto(photoUri); // Tiếp tục tải ảnh lên
                } else {
                    Log.e(TAG, "Chụp ảnh thành công nhưng photoUri bị null!");
                    Toast.makeText(this, "Đã xảy ra lỗi khi lấy ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Người dùng hủy chụp ảnh hoặc có lỗi: " + result.getResultCode());
                // Xóa Uri nếu người dùng hủy hoặc lỗi, tránh upload nhầm ảnh cũ
                photoUri = null;
                // Có thể xóa ảnh khỏi ImageView nếu muốn
                // selectedPhotoPreview.setImageResource(0); // Hoặc set placeholder
            }
        });

        // --- Thiết lập Listeners cho Buttons ---
        btnUploadPhoto.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnTakePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Nút Chụp ảnh được nhấn.");
            checkAndRequestCameraPermission();
        });

        btnNext.setOnClickListener(v -> {
            // Kiểm tra nếu không có URL nào được tải lên thành công (KHÔNG phải là kiểm tra ảnh hiển thị)
            if (photoUrls.isEmpty()) {
                Toast.makeText(this, "Vui lòng đợi ảnh tải lên hoặc thêm ảnh", Toast.LENGTH_SHORT).show();
                // Hoặc bạn có thể kiểm tra nếu selectedPhotoPreview có ảnh hay không
                // if (selectedPhotoPreview.getDrawable() == null) { ... }
                return;
            }
            Log.d(TAG, "Chuẩn bị cập nhật ảnh lên DB: " + photoUrls.toString());
            userRepository.updateUserField("photos", photoUrls, new xUserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Cập nhật URL ảnh thành công.");
                    goToNextActivity(); // Chuyển sang Activity tiếp theo
                }
                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Lỗi khi cập nhật URL ảnh: " + errorMessage);
                    Toast.makeText(xMyimageActivity.this, "Lỗi khi cập nhật ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSkip.setOnClickListener(v -> {
            Log.d(TAG, "Người dùng chọn bỏ qua.");
            goToNextActivity(); // Chuyển sang Activity tiếp theo
        });
    }

    // --- Hàm hiển thị ảnh lên ImageView ---
    private void displayImage(Uri imageUri) {
        if (imageUri != null && selectedPhotoPreview != null) {
            Log.d(TAG, "Hiển thị ảnh: " + imageUri.toString());
            // Cách 1: Sử dụng setImageURI (đơn giản nhất)
            selectedPhotoPreview.setImageURI(imageUri);

            // Cách 2: Sử dụng Glide (Khuyến nghị cho hiệu năng tốt hơn, cần thêm thư viện)
            // Glide.with(this).load(imageUri).into(selectedPhotoPreview);

            // Cách 3: Sử dụng Picasso (Tương tự Glide, cần thêm thư viện)
            // Picasso.get().load(imageUri).into(selectedPhotoPreview);
        } else {
            Log.w(TAG, "Không thể hiển thị ảnh - Uri hoặc ImageView bị null");
        }
    }

    // --- Logic Xử lý Quyền và Mở Camera (Giữ nguyên) ---
    private void checkAndRequestCameraPermission() {
        // ... (code giữ nguyên)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Quyền camera đã được cấp. Mở camera...");
            openCamera();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Log.d(TAG, "Cần giải thích lý do yêu cầu quyền camera.");
            Toast.makeText(this, "Cần quyền truy cập camera để chụp ảnh.", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Yêu cầu quyền camera lần đầu hoặc không cần giải thích.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // ... (code giữ nguyên)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Người dùng đã cấp quyền camera.");
                openCamera();
            } else {
                Log.w(TAG, "Người dùng từ chối quyền camera.");
                Toast.makeText(this, "Bạn đã từ chối quyền camera. Không thể chụp ảnh.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        // ... (code giữ nguyên, đảm bảo authority khớp với Manifest)
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Sử dụng resolveActivity để đảm bảo có ứng dụng camera
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Lỗi nghiêm trọng khi tạo file ảnh: ", ex);
                Toast.makeText(this, "Không thể tạo tệp để lưu ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                // Đảm bảo authority khớp với khai báo trong AndroidManifest.xml và file paths.xml
                String authority = getPackageName() + ".fileprovider"; // Cách lấy authority an toàn hơn
                try {
                    photoUri = FileProvider.getUriForFile(this,
                            authority,
                            photoFile);

                    Log.d(TAG, "Uri được tạo cho camera: " + photoUri.toString() + " với authority: " + authority);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    Log.d(TAG, "Chuẩn bị khởi chạy camera...");
                    takePictureLauncher.launch(takePictureIntent);
                    Log.d(TAG, "Đã gọi takePictureLauncher.launch()");

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Lỗi khi gọi FileProvider.getUriForFile. Authority: " + authority + ". Kiểm tra khai báo Provider trong Manifest và file paths.xml.", e);
                    Toast.makeText(this, "Lỗi cấu hình chia sẻ file ảnh", Toast.LENGTH_LONG).show();
                    photoUri = null; // Reset Uri nếu lỗi
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG,"Không tìm thấy Activity để xử lý ACTION_IMAGE_CAPTURE", e);
                    Toast.makeText(this, "Không tìm thấy ứng dụng camera phù hợp", Toast.LENGTH_SHORT).show();
                    photoUri = null;
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi không xác định khi chuẩn bị mở camera", e);
                    Toast.makeText(this, "Đã xảy ra lỗi khi mở camera", Toast.LENGTH_SHORT).show();
                    photoUri = null;
                }
            } else {
                Log.e(TAG, "photoFile là null sau khi gọi createImageFile mà không có IOException?");
                photoUri = null;
            }
        } else {
            Log.w(TAG, "Không thể resolve Activity cho ACTION_IMAGE_CAPTURE");
            Toast.makeText(this, "Không tìm thấy ứng dụng camera trên thiết bị", Toast.LENGTH_SHORT).show();
            photoUri = null;
        }
    }

    private File createImageFile() throws IOException {
        // ... (code giữ nguyên)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()){
            if (!storageDir.mkdirs()){
                Log.e(TAG, "Không thể tạo thư mục: " + storageDir.getPath());
                // Vẫn có thể thử tạo file trong thư mục gốc của getExternalFilesDir
            }
        } else if (storageDir == null) {
            Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_PICTURES) trả về null");
            throw new IOException("Không thể truy cập thư mục lưu trữ ngoài.");
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        Log.d(TAG, "File ảnh được tạo tại: " + image.getAbsolutePath());
        return image;
    }

    // --- Logic Tải ảnh lên Firebase (Giữ nguyên) ---
    private void uploadPhoto(Uri uri) {
        // ... (code giữ nguyên)
        String userId = userRepository.getCurrentUserId();
        if (userId != null && uri != null) {
            // Tạo tên file duy nhất trên Storage
            String fileName = System.currentTimeMillis() + "_" + uri.getLastPathSegment(); // Thêm timestamp để tránh trùng lặp
            StorageReference photoRef = storageRef.child(userId + "/" + fileName);

            Log.d(TAG, "Bắt đầu tải lên: " + uri.toString() + " đến " + photoRef.getPath());
            photoRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            // Chỉ thêm URL nếu tải lên thành công và lấy được URL
                            photoUrls.add(url);
                            Log.d(TAG, "Tải lên thành công, URL: " + url);
                            Toast.makeText(xMyimageActivity.this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi lấy download URL: ", e);
                            Toast.makeText(xMyimageActivity.this,"Lỗi khi lấy link ảnh", Toast.LENGTH_SHORT).show();
                            // Cân nhắc xóa ảnh khỏi ImageView hoặc thông báo lỗi rõ hơn
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tải ảnh lên Firebase: ", e);
                        Toast.makeText(xMyimageActivity.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
                        // Cân nhắc xóa ảnh khỏi ImageView
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "Tiến trình tải lên: " + String.format(Locale.US, "%.2f", progress) + "%");
                        // Có thể cập nhật UI tiến trình ở đây nếu muốn
                    });
        } else {
            String errorMsg = "Không thể tải ảnh lên: ";
            if (userId == null) errorMsg += "userId bị null. ";
            if (uri == null) errorMsg += "uri bị null.";
            Log.e(TAG, errorMsg);
            Toast.makeText(xMyimageActivity.this, "Không thể tải ảnh (thiếu thông tin)", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Hàm chuyển sang Activity tiếp theo ---
    private void goToNextActivity() {
        Intent intent = new Intent(xMyimageActivity.this, xMapActivity.class); // Thay MapActivity bằng Activity đích của bạn
        startActivity(intent);
        finish(); // Kết thúc Activity hiện tại
    }

    // Hàm addPicToGallery không còn cần thiết khi dùng getExternalFilesDir
    // private void addPicToGallery(Uri contentUri) { ... }
}