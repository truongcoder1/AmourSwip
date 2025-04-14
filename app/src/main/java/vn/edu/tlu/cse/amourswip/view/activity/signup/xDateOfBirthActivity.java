package vn.edu.tlu.cse.amourswip.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.repository.xUserRepository;

public class xDateOfBirthActivity extends AppCompatActivity {

    private static final String TAG = "DateOfBirthActivity";
    private static final String DATE_FORMAT = "%02d/%02d/%04d";
    private static final int MIN_YEAR_LOGIC = 1900;
    private static final int MIN_AGE = 18;

    private DatePicker datePickerSpinner;
    private MaterialButton nextButton;
    private xUserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dateofbirth);
        datePickerSpinner = findViewById(R.id.date_picker_spinner);
        nextButton = findViewById(R.id.next_button);
        userRepository = new xUserRepository();
        Calendar today = Calendar.getInstance();
        Calendar defaultDisplayDate = Calendar.getInstance();
        defaultDisplayDate.add(Calendar.YEAR, -MIN_AGE);
        datePickerSpinner.updateDate(
                defaultDisplayDate.get(Calendar.YEAR),
                defaultDisplayDate.get(Calendar.MONTH),
                defaultDisplayDate.get(Calendar.DAY_OF_MONTH)
        );
        nextButton.setOnClickListener(v -> proceedToNextStep());
    }
    private void proceedToNextStep() {
        // Lấy ngày tháng trực tiếp từ DatePicker widget
        int year = datePickerSpinner.getYear();
        int month = datePickerSpinner.getMonth();
        int day = datePickerSpinner.getDayOfMonth();
        // Tạo chuỗi ngày sinh theo định dạng DD/MM/YYYY
        String dateOfBirthString = String.format(Locale.getDefault(), DATE_FORMAT, day, month + 1, year);
        Log.d(TAG, "Selected date from spinner: " + dateOfBirthString);
        if (!isValidDate(day, month + 1, year)) {
            Toast.makeText(this, R.string.error_invalid_date_logical, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isUserAgeValid(day, month + 1, year)) {
            Toast.makeText(this, R.string.error_user_underage, Toast.LENGTH_SHORT).show();
            return;
        }
        saveDateOfBirth(dateOfBirthString);
    }
    private boolean isUserAgeValid(int day, int month, int year) {
        Calendar dobCalendar = Calendar.getInstance();
        dobCalendar.set(year, month - 1, day);
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -MIN_AGE);
        cutoffDate.set(Calendar.HOUR_OF_DAY, 0); cutoffDate.set(Calendar.MINUTE, 0); cutoffDate.set(Calendar.SECOND, 0); cutoffDate.set(Calendar.MILLISECOND, 0);
        dobCalendar.set(Calendar.HOUR_OF_DAY, 0); dobCalendar.set(Calendar.MINUTE, 0); dobCalendar.set(Calendar.SECOND, 0); dobCalendar.set(Calendar.MILLISECOND, 0);
        return !dobCalendar.after(cutoffDate);
    }
    private void saveDateOfBirth(String dateOfBirth) {
        nextButton.setEnabled(false);
        userRepository.updateUserField("dateOfBirth", dateOfBirth, new xUserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Date of birth updated successfully.");
                Intent intent = new Intent(xDateOfBirthActivity.this, xMyimageActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to update date of birth: " + errorMessage);
                Toast.makeText(xDateOfBirthActivity.this, getString(R.string.error_saving_dob) + errorMessage, Toast.LENGTH_LONG).show();
                nextButton.setEnabled(true);
            }
        });
    }


    private boolean isValidDate(int day, int month, int year) {
        if (year <= 0) {
            Log.w(TAG, "isValidDate check: Year is zero or negative (" + year + ")");
            return false;
        }
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        if (month == 2) {
            boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (day > (isLeap ? 29 : 28)) return false;
        } else if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
            return false;
        }
        return true;
    }
}