package com.uilover.project1992.Nav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.uilover.project1992.Activity.FeedActivity;
import com.uilover.project1992.Activity.LoginActivity;
import com.uilover.project1992.Activity.MainActivity;
import com.uilover.project1992.Activity.ProfileActivity;
import com.uilover.project1992.R;
import com.uilover.project1992.databinding.ActivityUserGuideBinding;

public class UserGuideActivity extends AppCompatActivity {
    private ActivityUserGuideBinding binding; // Khai báo binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserGuideBinding.inflate(getLayoutInflater()); // Tạo binding
        setContentView(binding.getRoot()); // Set content view bằng binding

        setupQuestionClickListeners();
        setupBottomNav();// Gọi hàm xử lý sự kiện click
    }
    private void setupBottomNav() {
        // ChipNavigationBar bottomNav = findViewById(R.id.bottom_nav); // Nếu không có trong binding
        // Nên dùng binding nếu bottom_nav nằm trực tiếp trong activity_main.xml
        ChipNavigationBar bottomNav = binding.bottomNav; // Giả sử ID là bottomNav trong binding

        bottomNav.setOnItemSelectedListener(id -> { // Dùng lambda cho ngắn gọn
            if (id == R.id.home) {
                Intent intent;
                intent = new Intent(UserGuideActivity.this, MainActivity.class);
                startActivity(intent);
                // Xử lý Explorer (ví dụ: mở Activity/Fragment mới)
                // Toast.makeText(this, "Explorer Clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.explorer) {
                Intent intent;
                intent = new Intent(UserGuideActivity.this, FeedActivity.class);
                startActivity(intent);
            } else if (id == R.id.bookmark) {

            } else if (id == R.id.profile) {
                Intent intent; // Khai báo Intent trong khối lệnh
                if (isLoggedIn()) { // Kiểm tra trạng thái đăng nhập
                    intent = new Intent(UserGuideActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(UserGuideActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
        // Có thể đặt mục được chọn mặc định nếu muốn
        // bottomNav.setItemSelected(R.id.home, true);
    }
    private boolean isLoggedIn() {
        // Có thể dùng biến currentUser đã lấy ở onCreate cho hiệu quả hơn
        // return currentUser != null;
        return FirebaseAuth.getInstance().getCurrentUser() != null; // Giữ nguyên nếu logic cũ cần
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
