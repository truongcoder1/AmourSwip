package vn.edu.tlu.cse.amourswip.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import vn.edu.tlu.cse.amourswip.Datalayer.Repository.UserRepository;
import vn.edu.tlu.cse.amourswip.R;

public class SelectGenderActivity extends AppCompatActivity {

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectedgender);

        userRepository = new UserRepository();

        Button btnMale = findViewById(R.id.male_button);
        Button btnFemale = findViewById(R.id.female_button);
        Button btnOther = findViewById(R.id.other_button);

        View.OnClickListener onGenderSelected = v -> {
            String gender = "";
            if (v.getId() == R.id.male_button) {
                gender = "Nam";
            } else if (v.getId() == R.id.female_button) {
                gender = "Nữ";
            } else if (v.getId() == R.id.other_button) {
                gender = "Khác";
            }

            // Cập nhật giới tính vào Realtime Database
            userRepository.updateUserField("gender", gender, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    // Chuyển đến màn hình chọn sở thích giới tính
                    Intent intent = new Intent(SelectGenderActivity.this, PreferGenderActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(SelectGenderActivity.this, "Lỗi khi cập nhật giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        };

        btnMale.setOnClickListener(onGenderSelected);
        btnFemale.setOnClickListener(onGenderSelected);
        btnOther.setOnClickListener(onGenderSelected);
    }
}