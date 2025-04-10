package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.content.res.ColorStateList; // Import
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import
import com.google.android.material.button.MaterialButton; // Import và sử dụng
import vn.edu.tlu.cse.amourswip.datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class PreferGenderActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private MaterialButton btnAll;
    private MaterialButton btnMale;
    private MaterialButton btnFemale;
    private MaterialButton btnNext;
    private String selectedPreferredGender = null; // Lưu lựa chọn

    // --- Cấu hình trạng thái nút ---
    private int selectedStrokeWidth;
    private ColorStateList selectedStrokeColor;
    private final int defaultStrokeWidth = 0;
    private final float SELECTED_ALPHA = 1.0f;
    private final float UNSELECTED_ALPHA = 0.65f;
    // -------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefergender);

        userRepository = new UserRepository();

        // --- Tìm các nút bằng ID ---
        btnAll = findViewById(R.id.all_button);
        btnMale = findViewById(R.id.male_button);
        btnFemale = findViewById(R.id.female_button);
        btnNext = findViewById(R.id.next_button);

        // --- Khởi tạo cấu hình đường viền ---
        selectedStrokeWidth = 6; // dp
        // Đảm bảo bạn đã định nghĩa màu này trong colors.xml
        selectedStrokeColor = ContextCompat.getColorStateList(this, R.color.selected_button_stroke_color);

        // --- Đặt trạng thái ban đầu cho các nút lựa chọn ---
        setButtonState(btnAll, false);
        setButtonState(btnMale, false);
        setButtonState(btnFemale, false);
        // --------------------------------------------------

        // --- Listener cho các nút chọn sở thích giới tính ---
        View.OnClickListener onPreferredGenderSelected = v -> {
            // Reset tất cả các nút về trạng thái không được chọn
            setButtonState(btnAll, false);
            setButtonState(btnMale, false);
            setButtonState(btnFemale, false);

            // Đặt nút được click vào trạng thái được chọn
            MaterialButton selectedButton = (MaterialButton) v;
            setButtonState(selectedButton, true);

            // Lưu lựa chọn
            int id = v.getId();
            if (id == R.id.all_button) {
                selectedPreferredGender = "Tất cả";
            } else if (id == R.id.male_button) {
                selectedPreferredGender = "Nam";
            } else if (id == R.id.female_button) {
                selectedPreferredGender = "Nữ";
            }

            // Kích hoạt nút "Tiếp"
            btnNext.setEnabled(selectedPreferredGender != null);
        };

        // Gán listener cho các nút lựa chọn
        btnAll.setOnClickListener(onPreferredGenderSelected);
        btnMale.setOnClickListener(onPreferredGenderSelected);
        btnFemale.setOnClickListener(onPreferredGenderSelected);

        // --- Listener cho nút "Tiếp" ---
        btnNext.setOnClickListener(v -> {
            if (selectedPreferredGender != null) {
                // Vô hiệu hóa các nút trong khi gửi dữ liệu
                btnNext.setEnabled(false);
                btnAll.setEnabled(false);
                btnMale.setEnabled(false);
                btnFemale.setEnabled(false);

                // Cập nhật sở thích giới tính vào Realtime Database
                userRepository.updateUserField("preferredGender", selectedPreferredGender, new UserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {
                        // Chuyển đến màn hình nhập ngày sinh
                        Intent intent = new Intent(PreferGenderActivity.this, DateOfBirthActivity.class);
                        startActivity(intent);
                        // Không cần enable lại nút nếu thành công và chuyển màn hình
                        // finish(); // Optional
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(PreferGenderActivity.this, "Lỗi khi cập nhật sở thích giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                        // Enable lại các nút nếu có lỗi
                        btnNext.setEnabled(true);
                        btnAll.setEnabled(true);
                        btnMale.setEnabled(true);
                        btnFemale.setEnabled(true);
                    }
                });
            } else {
                // Trường hợp này ít xảy ra nếu logic disable/enable đúng
                Toast.makeText(PreferGenderActivity.this, "Vui lòng chọn sở thích giới tính", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm tiện ích để đặt trạng thái trực quan cho một nút lựa chọn.
     * @param button Nút cần thay đổi.
     * @param isSelected true nếu nút này đang được chọn, false nếu không.
     */
    private void setButtonState(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setAlpha(SELECTED_ALPHA);
            button.setStrokeWidth(selectedStrokeWidth);
            button.setStrokeColor(selectedStrokeColor);
        } else {
            button.setAlpha(UNSELECTED_ALPHA);
            button.setStrokeWidth(defaultStrokeWidth);
            button.setStrokeColor(null);
        }
    }
}