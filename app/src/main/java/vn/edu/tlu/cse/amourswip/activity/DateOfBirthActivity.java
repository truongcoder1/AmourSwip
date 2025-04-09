package vn.edu.tlu.cse.amourswip.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import vn.edu.tlu.cse.amourswip.datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

import java.util.Calendar;

public class DateOfBirthActivity extends AppCompatActivity {

    private TextInputEditText birthdayInput;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dateofbirth);

        birthdayInput = findViewById(R.id.birthday_input);
        MaterialButton nextButton = findViewById(R.id.next_button);
        userRepository = new UserRepository();

        // Hiển thị DatePickerDialog khi nhấn vào TextInputEditText
        birthdayInput.setOnClickListener(v -> showDatePickerDialog());

        nextButton.setOnClickListener(v -> {
            String dateOfBirth = birthdayInput.getText().toString().trim();

            // Kiểm tra dữ liệu đầu vào
            if (!isValidDateFormat(dateOfBirth)) {
                Toast.makeText(DateOfBirthActivity.this, "Vui lòng chọn ngày sinh theo định dạng DD/MM/YYYY", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra ngày sinh hợp lệ
            String[] parts = dateOfBirth.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            if (!isValidDate(day, month, year)) {
                Toast.makeText(DateOfBirthActivity.this, "Ngày sinh không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật ngày sinh vào Realtime Database
            userRepository.updateUserField("dateOfBirth", dateOfBirth, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    // Chuyển đến màn hình thêm ảnh
                    Intent intent = new Intent(DateOfBirthActivity.this, EditPhotosActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(DateOfBirthActivity.this, "Lỗi khi cập nhật ngày sinh: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDatePickerDialog() {
        // Lấy ngày hiện tại làm giá trị mặc định
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog với calendar mode
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerDialogTheme,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    birthdayInput.setText(date);
                },
                year, month, day
        );

        // Hiển thị dialog
        datePickerDialog.show();
    }

    // Kiểm tra định dạng DD/MM/YYYY
    private boolean isValidDateFormat(String date) {
        if (date.length() != 10) return false;
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) return false;

        String[] parts = date.split("/");
        if (parts.length != 3) return false;

        try {
            Integer.parseInt(parts[0]); // Ngày
            Integer.parseInt(parts[1]); // Tháng
            Integer.parseInt(parts[2]); // Năm
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    // Kiểm tra ngày sinh hợp lệ
    private boolean isValidDate(int day, int month, int year) {
        // Kiểm tra năm (ví dụ: từ 1900 đến năm hiện tại)
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        if (year < 1900 || year > currentYear) {
            return false;
        }

        // Kiểm tra tháng
        if (month < 1 || month > 12) {
            return false;
        }

        // Kiểm tra ngày
        if (day < 1 || day > 31) {
            return false;
        }

        // Kiểm tra số ngày trong tháng
        if (month == 2) { // Tháng 2
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (isLeapYear) {
                return day <= 29;
            } else {
                return day <= 28;
            }
        } else if (month == 4 || month == 6 || month == 9 || month == 11) { // Các tháng 30 ngày
            return day <= 30;
        }

        return true;
    }
}