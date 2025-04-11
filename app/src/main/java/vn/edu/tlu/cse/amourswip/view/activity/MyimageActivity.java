package vn.edu.tlu.cse.amourswip.view.activity;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.repository.UserRepository;

public class MyimageActivity extends AppCompatActivity {

    private static final String TAG = "MyimageActivity";
    // Mã yêu cầu quyền Camera
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private UserRepository userRepository;
    private StorageReference storageRef;
    private final List<String> photoUrls = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private Uri photoUri;

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

        // --- Khởi tạo các Launchers ---
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Ảnh được chọn từ thư viện: " + uri.toString());
                uploadPhoto(uri);
            } else {
                Log.d(TAG, "Không có ảnh nào được chọn từ thư viện.");
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (photoUri != null) {
                    Log.d(TAG, "Ảnh chụp thành công, Uri: " + photoUri.toString());
                    uploadPhoto(photoUri);
                } else {
                    Log.e(TAG, "Chụp ảnh thành công nhưng photoUri bị null!");
                    Toast.makeText(this, "Đã xảy ra lỗi khi lấy ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Người dùng hủy chụp ảnh hoặc có lỗi: " + result.getResultCode());
                photoUri = null;
            }
        });

        btnUploadPhoto.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnTakePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Nút Chụp ảnh được nhấn.");
            // 1. Kiểm tra quyền Camera trước
            checkAndRequestCameraPermission();
        });

        btnNext.setOnClickListener(v -> {
            if (photoUrls.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất một ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "Chuẩn bị cập nhật ảnh lên DB: " + photoUrls.toString());
            userRepository.updateUserField("photos", photoUrls, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Cập nhật URL ảnh thành công.");
                    Intent intent = new Intent(MyimageActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Lỗi khi cập nhật URL ảnh: " + errorMessage);
                    Toast.makeText(MyimageActivity.this, "Lỗi khi cập nhật ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSkip.setOnClickListener(v -> {

            Log.d(TAG, "Người dùng chọn bỏ qua.");
            Intent intent = new Intent(MyimageActivity.this, MapActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // --- Logic Xử lý Quyền và Mở Camera ---

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp, tiến hành mở camera
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                String authority = "vn.edu.tlu.cse.amourswip.fileprovider";
                try {
                    photoUri = FileProvider.getUriForFile(this,
                            authority,
                            photoFile);

                    Log.d(TAG, "Uri được tạo cho camera: " + photoUri.toString());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    Log.d(TAG, "Chuẩn bị khởi chạy camera...");
                    takePictureLauncher.launch(takePictureIntent);
                    Log.d(TAG, "Đã gọi takePictureLauncher.launch()");

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Lỗi khi gọi FileProvider.getUriForFile. Authority: " + authority, e);
                    Toast.makeText(this, "Lỗi cấu hình chia sẻ file ảnh", Toast.LENGTH_LONG).show();
                    photoUri = null;
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG,"Không tìm thấy Activity để xử lý ACTION_IMAGE_CAPTURE", e);
                    Toast.makeText(this, "Không tìm thấy ứng dụng camera phù hợp", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi không xác định khi chuẩn bị mở camera", e);
                    Toast.makeText(this, "Đã xảy ra lỗi khi mở camera", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "photoFile là null sau khi gọi createImageFile mà không có IOException?");
            }
        } else {
            Log.w(TAG, "Không thể resolve Activity cho ACTION_IMAGE_CAPTURE");
            Toast.makeText(this, "Không tìm thấy ứng dụng camera trên thiết bị", Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()){
            if (!storageDir.mkdirs()){
                Log.e(TAG, "Không thể tạo thư mục: " + storageDir.getPath());
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

    private void uploadPhoto(Uri uri) {
        String userId = userRepository.getCurrentUserId();
        if (userId != null && uri != null) {
            StorageReference photoRef = storageRef.child(userId + "/" + System.currentTimeMillis() + ".jpg");
            Log.d(TAG, "Bắt đầu tải lên: " + uri.toString() + " đến " + photoRef.getPath());
            photoRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            photoUrls.add(url);
                            Log.d(TAG, "Tải lên thành công, URL: " + url);
                            Toast.makeText(MyimageActivity.this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi lấy download URL: ", e);
                            Toast.makeText(MyimageActivity.this,"Lỗi khi lấy link ảnh", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tải ảnh lên Firebase: ", e);
                        Toast.makeText(MyimageActivity.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "Tiến trình tải lên: " + String.format(Locale.US, "%.2f", progress) + "%");
                    });
        } else {
            String errorMsg = "Không thể tải ảnh lên: ";
            if (userId == null) errorMsg += "userId bị null. ";
            if (uri == null) errorMsg += "uri bị null.";
            Log.e(TAG, errorMsg);
            Toast.makeText(MyimageActivity.this, "Không thể tải ảnh (thiếu thông tin)", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPicToGallery(Uri contentUri) {
        Log.d(TAG, "Chức năng addPicToGallery được gọi với uri: " + contentUri);
    }
}