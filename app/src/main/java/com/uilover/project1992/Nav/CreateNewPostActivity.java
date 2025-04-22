package com.uilover.project1992.Nav;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uilover.project1992.databinding.CreatePostBinding;

public class CreateNewPostActivity extends AppCompatActivity {
    private CreatePostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVariable();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }
}
