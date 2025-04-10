package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import vn.edu.tlu.cse.amourswip.datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SelectGenderActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private MaterialButton btnMale;
    private MaterialButton btnFemale;
    private MaterialButton btnOther;
    private MaterialButton btnNext;
    private String selectedGender = null;

    // --- Cấu hình cho trạng thái nút ---
    private int selectedStrokeWidth;
    private ColorStateList selectedStrokeColor;
    private final int defaultStrokeWidth = 0;
    private final float SELECTED_ALPHA = 1.0f; // Alpha cho nút được chọn (không đổi)
    private final float UNSELECTED_ALPHA = 0.65f; // Alpha cho nút KHÔNG được chọn (mờ hơn)
    // ---------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectedgender);

        userRepository = new UserRepository();

        btnMale = findViewById(R.id.male_button);
        btnFemale = findViewById(R.id.female_button);
        btnOther = findViewById(R.id.other_button);
        btnNext = findViewById(R.id.next_button);

        // Khởi tạo giá trị đường viền
        selectedStrokeWidth = 6; // dp
        selectedStrokeColor = ContextCompat.getColorStateList(this, R.color.selected_button_stroke_color);

        // --- Ban đầu, làm mờ tất cả các nút giới tính một chút ---
        // (Trừ khi bạn muốn một nút được chọn mặc định)
        setButtonState(btnMale, false); // false = không được chọn
        setButtonState(btnFemale, false);
        setButtonState(btnOther, false);
        // ----------------------------------------------------


        // Listener for gender selection buttons
        View.OnClickListener onGenderSelected = v -> {
            // --- Đặt lại trạng thái "không được chọn" cho TẤT CẢ các nút giới tính ---
            setButtonState(btnMale, false);
            setButtonState(btnFemale, false);
            setButtonState(btnOther, false);

            // --- Áp dụng trạng thái "được chọn" cho nút ĐƯỢC CHỌN ---
            MaterialButton selectedButton = (MaterialButton) v;
            setButtonState(selectedButton, true); // true = được chọn

            // --- Lưu giới tính đã chọn ---
            int id = v.getId();
            if (id == R.id.male_button) {
                selectedGender = "Nam";
            } else if (id == R.id.female_button) {
                selectedGender = "Nữ";
            } else if (id == R.id.other_button) {
                selectedGender = "Khác";
            }

            // --- Kích hoạt nút Next ---
            btnNext.setEnabled(selectedGender != null);
        };

        // Set the listener to gender buttons
        btnMale.setOnClickListener(onGenderSelected);
        btnFemale.setOnClickListener(onGenderSelected);
        btnOther.setOnClickListener(onGenderSelected);

        // Listener for the Next button (không thay đổi nhiều, chỉ thêm xử lý enable/disable)
        btnNext.setOnClickListener(v -> {
            if (selectedGender != null) {
                btnNext.setEnabled(false); // Vô hiệu hóa tạm thời
                // Disable luôn các nút chọn giới tính để tránh thay đổi trong lúc gửi
                btnMale.setEnabled(false);
                btnFemale.setEnabled(false);
                btnOther.setEnabled(false);

                userRepository.updateUserField("gender", selectedGender, new UserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(SelectGenderActivity.this, PreferGenderActivity.class);
                        startActivity(intent);
                        // Nếu thành công và chuyển màn hình, không cần enable lại các nút ở đây
                        // finish(); // Optional
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(SelectGenderActivity.this, "Lỗi khi cập nhật giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                        // Enable lại các nút nếu có lỗi để người dùng thử lại
                        btnNext.setEnabled(true);
                        btnMale.setEnabled(true);
                        btnFemale.setEnabled(true);
                        btnOther.setEnabled(true);
                    }
                });
            } else {
                Toast.makeText(SelectGenderActivity.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm tiện ích để đặt trạng thái trực quan cho một nút giới tính.
     * @param button Nút cần thay đổi.
     * @param isSelected true nếu nút này đang được chọn, false nếu không.
     */
    private void setButtonState(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setAlpha(SELECTED_ALPHA); // Đặt lại alpha về đầy đủ
            button.setStrokeWidth(selectedStrokeWidth);
            button.setStrokeColor(selectedStrokeColor);
        } else {
            button.setAlpha(UNSELECTED_ALPHA); // Làm mờ nút
            button.setStrokeWidth(defaultStrokeWidth);
            button.setStrokeColor(null); // Xóa màu đường viền
        }
    }
}