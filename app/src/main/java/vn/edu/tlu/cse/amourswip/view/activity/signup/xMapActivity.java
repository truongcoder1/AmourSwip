package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.activity.main.MainActivity;

public class xMapActivity extends AppCompatActivity {

    private LinearLayout layoutDefault;
    private LinearLayout layoutAdjustDistance;
    private SeekBar seekBarDistance;
    private TextView tvDistance;
    private Button btnSaveDistance;
    private Button allowButton;
    private Button skipButton;
    private SharedPreferences sharedPreferences;
    private DatabaseReference database;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String DISTANCE_KEY = "searchDistance";
    private static final int REQUEST_CODE_LOCATION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Khởi tạo SharedPreferences và Firebase
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo các view
        layoutDefault = findViewById(R.id.layout_default);
        layoutAdjustDistance = findViewById(R.id.layout_adjust_distance);
        seekBarDistance = findViewById(R.id.seekbar_distance);
        tvDistance = findViewById(R.id.tv_distance);
        btnSaveDistance = findViewById(R.id.btn_save_distance);
        allowButton = findViewById(R.id.allow_button);
        skipButton = findViewById(R.id.skip_button);

        // Kiểm tra xem người dùng có vào từ SettingActivity không
        boolean fromSettings = getIntent().getBooleanExtra("fromSettings", false);

        if (fromSettings) {
            // Hiển thị giao diện điều chỉnh khoảng cách
            layoutDefault.setVisibility(View.GONE);
            layoutAdjustDistance.setVisibility(View.VISIBLE);

            // Lấy giá trị khoảng cách đã lưu (mặc định 10 km)
            int savedDistance = sharedPreferences.getInt(DISTANCE_KEY, 10);
            seekBarDistance.setProgress(savedDistance);
            tvDistance.setText("Khoảng cách: " + savedDistance + " km");

            // Lắng nghe thay đổi trên SeekBar
            seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 1) {
                        progress = 1;
                        seekBar.setProgress(1);
                    }
                    tvDistance.setText("Khoảng cách: " + progress + " km");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            // Lưu khoảng cách khi nhấn nút "Lưu"
            btnSaveDistance.setOnClickListener(v -> {
                int newDistance = seekBarDistance.getProgress();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(DISTANCE_KEY, newDistance);
                editor.apply();
                Toast.makeText(xMapActivity.this, "Đã lưu phạm vi tìm kiếm: " + newDistance + " km", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại SettingActivity
            });
        } else {
            // Hiển thị giao diện ban đầu (luồng đăng ký)
            layoutDefault.setVisibility(View.VISIBLE);
            layoutAdjustDistance.setVisibility(View.GONE);

            // Logic cho nút "Cho phép"
            allowButton.setOnClickListener(v -> {
                requestLocationPermission();
            });

            // Logic cho nút "Bỏ qua"
            skipButton.setOnClickListener(v -> {
                // Chuyển đến MainActivity mà không lấy vị trí
                startActivity(new Intent(xMapActivity.this, MainActivity.class));
                finish();
            });
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền truy cập vị trí để hoạt động", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Lưu vị trí vào Firebase
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                database.child("users").child(userId).child("latitude").setValue(latitude);
                database.child("users").child(userId).child("longitude").setValue(longitude);

                // Chuyển đến MainActivity
                startActivity(new Intent(xMapActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Không thể lấy vị trí, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}