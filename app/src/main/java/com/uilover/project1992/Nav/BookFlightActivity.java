package com.uilover.project1992.Nav;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uilover.project1992.databinding.BookFlightBinding;

public class BookFlightActivity extends AppCompatActivity {
    private BookFlightBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BookFlightBinding.inflate(getLayoutInflater());  // Tạo binding
        setContentView(binding.getRoot());  // Sử dụng binding để set content view
        setVariable();  // Gọi hàm thiết lập sự kiện
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }
}
