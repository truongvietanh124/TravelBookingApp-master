package com.uilover.project1992.Nav;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uilover.project1992.databinding.ChangeAvatarBinding;

public class ChangeAvatarActivity extends AppCompatActivity {
    private ChangeAvatarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVariable();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }
}
