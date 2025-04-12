package vn.edu.tlu.cse.amourswip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import vn.edu.tlu.cse.amourswip.datalayer.repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class PreferGenderActivity extends AppCompatActivity {

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefergender);

        userRepository = new UserRepository();

        Button btnAll = findViewById(R.id.all_button);
        Button btnMale = findViewById(R.id.male_button);
        Button btnFemale = findViewById(R.id.female_button);

        View.OnClickListener onPreferredGenderSelected = v -> {
            String preferredGender = "";
            if (v.getId() == R.id.all_button) {
                preferredGender = "Tất cả";
            } else if (v.getId() == R.id.male_button) {
                preferredGender = "Nam";
            } else if (v.getId() == R.id.female_button) {
                preferredGender = "Nữ";
            }

            // Cập nhật sở thích giới tính vào Realtime Database
            userRepository.updateUserField("preferredGender", preferredGender, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    // Chuyển đến màn hình nhập ngày sinh
                    Intent intent = new Intent(PreferGenderActivity.this, DateOfBirthActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(PreferGenderActivity.this, "Lỗi khi cập nhật sở thích giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        };

        btnAll.setOnClickListener(onPreferredGenderSelected);
        btnMale.setOnClickListener(onPreferredGenderSelected);
        btnFemale.setOnClickListener(onPreferredGenderSelected);
    }
}