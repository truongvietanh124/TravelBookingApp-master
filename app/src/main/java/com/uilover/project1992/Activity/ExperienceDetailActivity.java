package com.uilover.project1992.Activity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uilover.project1992.Model.Comment;
import com.uilover.project1992.Model.Experience;
import com.uilover.project1992.Adapter.CommentAdapter;
import com.uilover.project1992.R;
import com.uilover.project1992.databinding.ActivityExperienceDetailBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExperienceDetailActivity extends AppCompatActivity {

    private ActivityExperienceDetailBinding binding;
    private String experienceId;
    private DatabaseReference experienceRef;
    private FirebaseAuth firebaseAuth;
    private Experience currentExperience;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExperienceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Lấy ID của Experience từ Intent
        experienceId = getIntent().getStringExtra("EXPERIENCE_ID");
        if (experienceId == null) {
            Toast.makeText(this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        experienceRef = FirebaseDatabase.getInstance().getReference("experiences").child(experienceId);

        // Khởi tạo RecyclerView cho comments
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentAdapter);

        // Đọc dữ liệu Experience từ Firebase
        loadExperienceData();

        // Thiết lập sự kiện click cho nút like
        binding.layoutLikes.setOnClickListener(v -> handleLikeClick());

        // Thiết lập sự kiện click cho nút share
        binding.layoutShare.setOnClickListener(v -> handleShareClick());

        // Thiết lập sự kiện click cho nút gửi comment
        binding.buttonSendComment.setOnClickListener(v -> handleSendComment());
    }

    private void loadExperienceData() {
        experienceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentExperience = dataSnapshot.getValue(Experience.class);
                    if (currentExperience != null) {
                        updateUI(currentExperience);
                        loadComments();
                    }
                } else {
                    Toast.makeText(ExperienceDetailActivity.this, "Bài viết không tồn tại", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ExperienceDetailActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Experience experience) {
        // Hiển thị hình ảnh
        if (experience.getImageUrl() != null && !experience.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(experience.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.imageExperience);
        }

        // Hiển thị thông tin
        binding.textTitle.setText(experience.getTitle());
        binding.textUserName.setText(experience.getUserName());
        binding.textLocation.setText(experience.getLocation());
        binding.textDescription.setText(experience.getDescription());

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(experience.getTimestamp()));
        binding.textTimestamp.setText(formattedDate);

        // Số lượt thích
        int likeCount = experience.getLikes() != null ? experience.getLikes().size() : 0;
        binding.textLikeCount.setText(String.valueOf(likeCount));

        // Kiểm tra xem người dùng hiện tại đã thích bài viết chưa
        updateLikeUI();
    }

    private void updateLikeUI() {
        String currentUserId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;

        if (currentUserId != null && currentExperience != null && currentExperience.getLikes() != null) {
            if (currentExperience.getLikes().containsKey(currentUserId)) {
                binding.imageLike.setImageResource(R.drawable.ic_like);
            } else {
                binding.imageLike.setImageResource(R.drawable.ic_like);
            }
        }
    }

    private void handleLikeClick() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thích bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference likesRef = experienceRef.child("likes");

        if (currentExperience.getLikes() != null && currentExperience.getLikes().containsKey(userId)) {
            // Người dùng đã thích, giờ bỏ thích
            likesRef.child(userId).removeValue();
        } else {
            // Người dùng chưa thích, thêm like
            likesRef.child(userId).setValue(true);
        }
    }

    private void handleShareClick() {
        // Xử lý chia sẻ bài viết
        // Có thể sử dụng Intent.ACTION_SEND
        Toast.makeText(this, "Tính năng chia sẻ đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void loadComments() {
        // Sử dụng phương thức getCommentList để chuyển đổi Map thành List
        if (currentExperience != null) {
            commentList.clear();
            commentList.addAll(currentExperience.getCommentList());
            commentAdapter.notifyDataSetChanged();
        }
    }

    private void handleSendComment() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentText = binding.editTextComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo comment mới
        String commentId = experienceRef.child("comments").push().getKey();
        String userId = firebaseAuth.getCurrentUser().getUid();
        String userName = firebaseAuth.getCurrentUser().getDisplayName() != null ?
                firebaseAuth.getCurrentUser().getEmail() : "Người dùng ẩn danh";
        long timestamp = System.currentTimeMillis();

        Comment newComment = new Comment(commentId, userId, userName, commentText, timestamp);

        // Thêm comment vào danh sách
        if (commentId != null) {
            experienceRef.child("comments").child(commentId).setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        binding.editTextComment.setText("");
                        Toast.makeText(ExperienceDetailActivity.this, "Đã đăng bình luận", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ExperienceDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}