package vn.edu.tlu.cse.amourswip.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.repository.UserRepository;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private UserRepository userRepository;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MaterialButton allowButton = findViewById(R.id.allow_button);
        MaterialButton skipButton = findViewById(R.id.skip_button);

        allowButton.setOnClickListener(v -> {
            requestLocationPermission();
        });

        skipButton.setOnClickListener(v -> {
            // Bỏ qua việc lấy vị trí, đặt locationEnabled = false
            updateUserLocation(false, 0.0, 0.0);
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
                updateUserLocation(false, 0.0, 0.0);
            }
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateUserLocation(true, latitude, longitude);
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                        updateUserLocation(false, 0.0, 0.0);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUserLocation(false, 0.0, 0.0);
                });
    }

    private void updateUserLocation(boolean locationEnabled, double latitude, double longitude) {
        userRepository.updateUserField("locationEnabled", locationEnabled, new UserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                userRepository.updateUserField("latitude", latitude, new UserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {
                        userRepository.updateUserField("longitude", longitude, new UserRepository.OnUserActionListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MapActivity.this, "Đã cập nhật vị trí", Toast.LENGTH_SHORT).show();
                                // Điều hướng đến MainActivity
                                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(MapActivity.this, "Lỗi khi cập nhật kinh độ: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(MapActivity.this, "Lỗi khi cập nhật vĩ độ: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MapActivity.this, "Lỗi khi cập nhật trạng thái vị trí: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}