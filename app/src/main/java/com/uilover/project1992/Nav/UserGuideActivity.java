package com.uilover.project1992.Nav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
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

    public void onHotlineClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gọi điện thoại");
        builder.setMessage("Bạn có muốn gọi tới số 1900 123 456 không?");
        builder.setPositiveButton("Gọi", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:1900123456"));
            startActivity(intent);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    public void onBotChatClick(View view) {
        if (binding.chatBoxView.getVisibility() == View.GONE) {
            binding.chatBoxView.setVisibility(View.VISIBLE);
        } else {
            binding.chatBoxView.setVisibility(View.GONE);
        }
    }

    public void onSendMessageClick(View view) {
        String userMessage = binding.userInput.getText().toString().trim();

        if (!userMessage.isEmpty()) {
            // Gán nội dung vào TextView chatbotResponse (tạm thời)
            binding.chatbotResponse.setText("Bạn: " + userMessage + "\nChatbot: Cảm ơn bạn đã gửi câu hỏi!");

            // Xoá nội dung EditText
            binding.userInput.setText("");
        }
    }


}
