package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import vn.edu.tlu.cse.amourswip.model.repository.xUserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class xSelectGenderActivity extends AppCompatActivity {

    private xUserRepository userRepository;
    private MaterialButton btnMale;
    private MaterialButton btnFemale;
    private MaterialButton btnOther;
    private MaterialButton btnNext;
    private String selectedGender = null;


    private int selectedStrokeWidth;
    private ColorStateList selectedStrokeColor;
    private final int defaultStrokeWidth = 0;
    private final float SELECTED_ALPHA = 1.0f;
    private final float UNSELECTED_ALPHA = 0.65f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectedgender);

        userRepository = new xUserRepository();

        btnMale = findViewById(R.id.male_button);
        btnFemale = findViewById(R.id.female_button);
        btnOther = findViewById(R.id.other_button);
        btnNext = findViewById(R.id.next_button);

        // Khởi tạo giá trị đường viền
        selectedStrokeWidth = 6; // dp
        selectedStrokeColor = ContextCompat.getColorStateList(this, R.color.selected_button_stroke_color);

        // --- Ban đầu, làm mờ tất cả các nút giới tính một chút ---

        setButtonState(btnMale, false);
        setButtonState(btnFemale, false);
        setButtonState(btnOther, false);


        // Listener for gender selection buttons
        View.OnClickListener onGenderSelected = v -> {
            // --- Đặt lại trạng thái "không được chọn" cho TẤT CẢ các nút giới tính ---
            setButtonState(btnMale, false);
            setButtonState(btnFemale, false);
            setButtonState(btnOther, false);

            // --- Áp dụng trạng thái "được chọn" cho nút ĐƯỢC CHỌN ---
            MaterialButton selectedButton = (MaterialButton) v;
            setButtonState(selectedButton, true);


            int id = v.getId();
            if (id == R.id.male_button) {
                selectedGender = "Nam";
            } else if (id == R.id.female_button) {
                selectedGender = "Nữ";
            } else if (id == R.id.other_button) {
                selectedGender = "Khác";
            }


            btnNext.setEnabled(selectedGender != null);
        };


        btnMale.setOnClickListener(onGenderSelected);
        btnFemale.setOnClickListener(onGenderSelected);
        btnOther.setOnClickListener(onGenderSelected);


        btnNext.setOnClickListener(v -> {
            if (selectedGender != null) {
                btnNext.setEnabled(false);

                btnMale.setEnabled(false);
                btnFemale.setEnabled(false);
                btnOther.setEnabled(false);

                userRepository.updateUserField("gender", selectedGender, new xUserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(xSelectGenderActivity.this, xPreferGenderActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(xSelectGenderActivity.this, "Lỗi khi cập nhật giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                        // Enable lại các nút nếu có lỗi để người dùng thử lại
                        btnNext.setEnabled(true);
                        btnMale.setEnabled(true);
                        btnFemale.setEnabled(true);
                        btnOther.setEnabled(true);
                    }
                });
            }
        });
    }


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