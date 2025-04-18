package com.uilover.project1992.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uilover.project1992.Model.Experience;
import com.uilover.project1992.databinding.ActivityExploreBinding;

import java.util.UUID;

public class ExploreActivity extends AppCompatActivity {

    private ActivityExploreBinding binding;
    private static final int PICK_IMAGE_REQUEST = 71;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Toolbar setup
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Click listeners
        binding.btnSelectImage.setOnClickListener(v -> openImageChooser());
        binding.btnPost.setOnClickListener(v -> uploadExperience());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.imagePreview.setImageURI(imageUri);
            binding.btnSelectImage.setVisibility(View.GONE);
        }
    }

    private void uploadExperience() {
        String title = binding.etTitle.getText().toString().trim();
        String experienceText = binding.etExperience.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        if (title.isEmpty() || experienceText.isEmpty() || location.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPost.setEnabled(false);

        String experienceId = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child("experience_images/" + experienceId);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            Experience experience = new Experience(
                                    experienceId,
                                    title,
                                    experienceText,
                                    location,
                                    downloadUri.toString(),
                                    auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "",
                                    auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null
                                            ? auth.getCurrentUser().getEmail()
                                            : "Email not available",
                                    System.currentTimeMillis()
                            );

                            database.getReference().child("experiences").child(experienceId)
                                    .setValue(experience)
                                    .addOnSuccessListener(aVoid -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.btnPost.setEnabled(true);
                                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnPost.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
