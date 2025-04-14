package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.content.res.ColorStateList; // Import
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import
import com.google.android.material.button.MaterialButton; // Import và sử dụng
import vn.edu.tlu.cse.amourswip.model.repository.xUserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class xPreferGenderActivity extends AppCompatActivity {

    private xUserRepository userRepository;
    private MaterialButton btnAll;
    private MaterialButton btnMale;
    private MaterialButton btnFemale;
    private MaterialButton btnNext;
    private String selectedPreferredGender = null;
    private int selectedStrokeWidth;
    private ColorStateList selectedStrokeColor;
    private final int defaultStrokeWidth = 0;
    private final float SELECTED_ALPHA = 1.0f;
    private final float UNSELECTED_ALPHA = 0.65f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefergender);

        userRepository = new xUserRepository();

        // --- Tìm các nút bằng ID ---
        btnAll = findViewById(R.id.all_button);
        btnMale = findViewById(R.id.male_button);
        btnFemale = findViewById(R.id.female_button);
        btnNext = findViewById(R.id.next_button);


        selectedStrokeWidth = 6;
        selectedStrokeColor = ContextCompat.getColorStateList(this, R.color.selected_button_stroke_color);

        // --- Đặt trạng thái ban đầu cho các nút lựa chọn ---
        setButtonState(btnAll, false);
        setButtonState(btnMale, false);
        setButtonState(btnFemale, false);


        // --- Listener cho các nút chọn sở thích giới tính ---
        View.OnClickListener onPreferredGenderSelected = v -> {
            setButtonState(btnAll, false);
            setButtonState(btnMale, false);
            setButtonState(btnFemale, false);

            // Đặt nút được click vào trạng thái được chọn
            MaterialButton selectedButton = (MaterialButton) v;
            setButtonState(selectedButton, true);

            // Lưu lựa chọn
            int id = v.getId();
            if (id == R.id.all_button) {
                selectedPreferredGender = "Khác";
            } else if (id == R.id.male_button) {
                selectedPreferredGender = "Nam";
            } else if (id == R.id.female_button) {
                selectedPreferredGender = "Nữ";
            }


            btnNext.setEnabled(selectedPreferredGender != null);
        };

        // Gán listener cho các nút lựa chọn
        btnAll.setOnClickListener(onPreferredGenderSelected);
        btnMale.setOnClickListener(onPreferredGenderSelected);
        btnFemale.setOnClickListener(onPreferredGenderSelected);

        // --- Listener cho nút "Tiếp" ---
        btnNext.setOnClickListener(v -> {
            if (selectedPreferredGender != null) {

                btnNext.setEnabled(false);
                btnAll.setEnabled(false);
                btnMale.setEnabled(false);
                btnFemale.setEnabled(false);

                // Cập nhật sở thích giới tính vào Realtime Database
                userRepository.updateUserField("preferredGender", selectedPreferredGender, new xUserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {

                        Intent intent = new Intent(xPreferGenderActivity.this, xDateOfBirthActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(xPreferGenderActivity.this, "Lỗi khi cập nhật sở thích giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                        // Enable lại các nút nếu có lỗi
                        btnNext.setEnabled(true);
                        btnAll.setEnabled(true);
                        btnMale.setEnabled(true);
                        btnFemale.setEnabled(true);
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