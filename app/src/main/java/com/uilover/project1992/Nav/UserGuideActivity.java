package com.uilover.project1992.Nav;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uilover.project1992.databinding.ActivityUserGuideBinding;

public class UserGuideActivity extends AppCompatActivity {
    private ActivityUserGuideBinding binding; // Khai báo binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserGuideBinding.inflate(getLayoutInflater()); // Tạo binding
        setContentView(binding.getRoot()); // Set content view bằng binding

        setupQuestionClickListeners(); // Gọi hàm xử lý sự kiện click
    }

    private void setupQuestionClickListeners() {
        // Xử lý sự kiện click cho các câu hỏi
        binding.cardQuestion1.setOnClickListener(v -> {
            Intent intent = new Intent(UserGuideActivity.this, BookFlightActivity.class);
            startActivity(intent);
        });

        binding.cardQuestion2.setOnClickListener(v -> {
            Intent intent = new Intent(UserGuideActivity.this, ChangeAvatarActivity.class);
            startActivity(intent);
        });

        binding.cardQuestion8.setOnClickListener(v -> {
            Intent intent = new Intent(UserGuideActivity.this, CreateNewPostActivity.class);
            startActivity(intent);
        });

        // Bạn có thể thêm các sự kiện khác cho các câu hỏi còn lại
        // Ví dụ:
        // binding.cardQuestion2.setOnClickListener(v -> { ... });
    }
}
