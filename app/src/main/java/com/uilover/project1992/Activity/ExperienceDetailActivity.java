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
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
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
    private Map<String, Float> userRatings; // Để lưu trữ đánh giá của người dùng
    private boolean isLiked = false;
    private ValueEventListener experienceListener;
    private RequestManager glideManager;
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExperienceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Glide RequestManager
        glideManager = Glide.with(this);

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

        // Khởi tạo Map lưu trữ đánh giá
        userRatings = new HashMap<>();

        // Khởi tạo RecyclerView cho comments
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentAdapter);

        // Đọc dữ liệu Experience từ Firebase
        setupExperienceListener();

        // Thiết lập các sự kiện click cho các nút trong layout mới
        setupClickListeners();

        // Kiểm tra nếu người dùng đã đăng nhập
        checkUserLoginStatus();
    }

    private void setupExperienceListener() {
        experienceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDestroyed) {
                    return; // Không làm gì nếu activity đã bị hủy
                }

                if (dataSnapshot.exists()) {
                    currentExperience = dataSnapshot.getValue(Experience.class);
                    if (currentExperience != null) {
                        updateUI(currentExperience);

                        // Thiết lập tiêu đề cho CollapsingToolbarLayout
                        binding.collapsingToolbar.setTitle(currentExperience.getTitle());

                        loadComments();

                        // Kiểm tra nếu có ratings map trong Firebase
                        if (dataSnapshot.hasChild("ratings")) {
                            DataSnapshot ratingsSnapshot = dataSnapshot.child("ratings");
                            userRatings.clear();

                            // Lấy tất cả đánh giá từ Firebase
                            for (DataSnapshot ratingSnapshot : ratingsSnapshot.getChildren()) {
                                String userId = ratingSnapshot.getKey();
                                Float rating = ratingSnapshot.getValue(Float.class);
                                if (userId != null && rating != null) {
                                    userRatings.put(userId, rating);
                                }
                            }

                            // Cập nhật hiển thị rating trung bình
                            updateRatingDisplay();

                            // Hiển thị rating của người dùng hiện tại (nếu có)
                            String currentUserId = firebaseAuth.getCurrentUser() != null ?
                                    firebaseAuth.getCurrentUser().getUid() : null;
                            if (currentUserId != null && userRatings.containsKey(currentUserId)) {
                                binding.ratingBarInput.setRating(userRatings.get(currentUserId));
                            }
                        } else {
                            // Nếu không có ratings, hiển thị rating từ trường rating của Experience
                            updateSimpleRating(currentExperience.getRating());
                        }
                    }
                } else {
                    if (!isDestroyed) {
                        Toast.makeText(ExperienceDetailActivity.this, "Bài viết không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isDestroyed) {
                    Toast.makeText(ExperienceDetailActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Đăng ký listener
        experienceRef.addValueEventListener(experienceListener);
    }

    private void setupClickListeners() {
        // Nút like - Sử dụng transaction để tránh race condition
        binding.btnLike.setOnClickListener(v -> handleLikeClick());

        // Nút chia sẻ trong toolbar
        binding.btnShare.setOnClickListener(v -> handleShareClick());

        // Nút lưu/bookmark trong toolbar
        binding.btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
            // Implement bookmark functionality here
        });

        // Nút gửi bình luận
        binding.btnSendComment.setOnClickListener(v -> handleSendComment());

        // Nút gửi đánh giá
        binding.btnSubmitRating.setOnClickListener(v -> handleSubmitRating());

        // Nút xem tất cả bình luận
        binding.btnViewAllComments.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải tất cả bình luận...", Toast.LENGTH_SHORT).show();
            // Implement view all comments functionality here
        });

        // Nút comment count
        binding.btnComment.setOnClickListener(v -> {
            // Scroll to comment section
            binding.nestedScrollView.smoothScrollTo(0, binding.cardComments.getTop());
        });
    }

    private void checkUserLoginStatus() {
        if (firebaseAuth.getCurrentUser() != null) {
            binding.cardRating.setVisibility(View.VISIBLE);
        } else {
            binding.cardRating.setVisibility(View.GONE);
        }
    }

    private void updateUI(Experience experience) {
        try {
            // Hiển thị hình ảnh - Kiểm tra xem activity có bị hủy không
            if (experience.getImageUrl() != null && !experience.getImageUrl().isEmpty() && !isDestroyed) {
                glideManager
                        .load(experience.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(binding.imageExperience);
            }

            // Hiển thị thông tin cơ bản
            binding.textTitle.setText(experience.getTitle());
            binding.textUserName.setText(experience.getUserName());
            binding.textDescription.setText(experience.getDescription());

            // Hiển thị vị trí trong chip
            binding.chipLocation.setText(experience.getLocation());
            binding.imageUserAvatar.setImageResource(R.drawable.default_avatar);

            // Định dạng thời gian
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(experience.getTimestamp()));
            binding.textTimestamp.setText(formattedDate);

            // Số lượt thích và bình luận
            int likeCount = experience.getLikes() != null ? experience.getLikes().size() : 0;
            binding.btnLike.setText(String.valueOf(likeCount));

            int commentCount = experience.getComments() != null ? experience.getComments().size() : 0;
            binding.btnComment.setText(String.valueOf(commentCount));
            binding.textCommentCount.setText("(" + commentCount + ")");

            // Kiểm tra xem người dùng hiện tại đã thích bài viết chưa
            updateLikeUI();
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi khi cập nhật UI
            e.printStackTrace();
        }
    }

    private void updateLikeUI() {
        String currentUserId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (currentUserId != null && currentExperience != null && currentExperience.getLikes() != null) {
            if (currentExperience.getLikes().containsKey(currentUserId)) {
                isLiked = true;
                binding.btnLike.setIconTint(getColorStateList(R.color.primary_color));
                binding.btnLike.setTextColor(getColor(R.color.primary_color));
            } else {
                isLiked = false;
                binding.btnLike.setIconTint(getColorStateList(R.color.text_secondary));
                binding.btnLike.setTextColor(getColor(R.color.text_secondary));
            }
        }
    }

    private void updateRatingDisplay() {
        if (userRatings.isEmpty()) {
            updateSimpleRating(0);
            return;
        }

        // Tính rating trung bình
        float totalRating = 0;
        for (Float rating : userRatings.values()) {
            totalRating += rating;
        }

        float averageRating = totalRating / userRatings.size();

        // Hiển thị rating trung bình và số lượng đánh giá
        updateSimpleRating(averageRating);
        binding.textRatingCount.setText("(" + userRatings.size() + ")");

        // Cập nhật rating trung bình vào Experience
        experienceRef.child("rating").setValue(averageRating);
    }

    private void updateSimpleRating(float rating) {
        binding.textRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
    }

    private void handleLikeClick() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thích bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tránh thao tác nhiều lần liên tiếp
        binding.btnLike.setEnabled(false);

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference likesRef = experienceRef.child("likes");

        // Sử dụng runTransaction để tránh race condition
        experienceRef.child("likes").runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(MutableData mutableData) {
                Map<String, Boolean> likes = new HashMap<>();

                // Chuyển data từ Firebase thành Map
                if (mutableData.getValue() != null) {
                    for (MutableData childData : mutableData.getChildren()) {
                        likes.put(childData.getKey(), true);
                    }
                }

                // Toggle like status
                if (likes.containsKey(userId)) {
                    likes.remove(userId);
                } else {
                    likes.put(userId, true);
                }

                // Set giá trị mới
                mutableData.setValue(likes);
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // Re-enable button
                if (!isDestroyed) {
                    binding.btnLike.setEnabled(true);
                }
            }
        });
    }

    private void handleSubmitRating() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        String userName = firebaseAuth.getCurrentUser().getDisplayName() != null ?
                firebaseAuth.getCurrentUser().getEmail() : "Người dùng ẩn danh";
        float userRating = binding.ratingBarInput.getRating();

        if (userRating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = binding.editTextRatingComment.getText().toString().trim();

        // Disable button to prevent multiple submissions
        binding.btnSubmitRating.setEnabled(false);

        // Lưu đánh giá vào Firebase
        experienceRef.child("ratings").child(userId).setValue(userRating)
                .addOnSuccessListener(aVoid -> {
                    if (!isDestroyed) {
                        Toast.makeText(ExperienceDetailActivity.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();

                        // Cập nhật Map userRatings cục bộ
                        userRatings.put(userId, userRating);

                        // Cập nhật hiển thị
                        updateRatingDisplay();

                        // Lưu comment của đánh giá nếu có
                        if (!comment.isEmpty()) {
                            String ratingCommentId = experienceRef.child("ratingComments").push().getKey();
                            if (ratingCommentId != null) {
                                HashMap<String, Object> ratingComment = new HashMap<>();
                                ratingComment.put("userId", userId);
                                ratingComment.put("rating", userRating);
                                ratingComment.put("comment", comment);
                                ratingComment.put("timestamp", System.currentTimeMillis());
                                ratingComment.put("userName", userName);

                                experienceRef.child("ratingComments").child(ratingCommentId).setValue(ratingComment);
                                binding.editTextRatingComment.setText("");
                            }
                        }

                        // Re-enable button
                        binding.btnSubmitRating.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isDestroyed) {
                        Toast.makeText(ExperienceDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnSubmitRating.setEnabled(true);
                    }
                });
    }

    private void handleShareClick() {
        // Xử lý chia sẻ bài viết
        // TODO: Implement share functionality with Intent.ACTION_SEND
        Toast.makeText(this, "Đang chia sẻ bài viết...", Toast.LENGTH_SHORT).show();
    }

    private void loadComments() {
        // Sử dụng phương thức getCommentList để chuyển đổi Map thành List
        if (currentExperience != null && currentExperience.getComments() != null) {
            commentList.clear();

            // Giới hạn hiển thị 3 comments mới nhất (vì nút Xem tất cả comments sẽ hiển thị phần còn lại)
            List<Comment> allComments = currentExperience.getCommentList();
            if (allComments.size() > 3) {
                commentList.addAll(allComments.subList(0, 3));
            } else {
                commentList.addAll(allComments);
            }

            commentAdapter.notifyDataSetChanged();

            // Hiển thị/ẩn nút xem tất cả tùy theo số lượng comments
            binding.btnViewAllComments.setVisibility(allComments.size() > 3 ? View.VISIBLE : View.GONE);
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

        // Disable button to prevent multiple submissions
        binding.btnSendComment.setEnabled(false);

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
                        if (!isDestroyed) {
                            binding.editTextComment.setText("");
                            Toast.makeText(ExperienceDetailActivity.this, "Đã đăng bình luận", Toast.LENGTH_SHORT).show();

                            // Cập nhật số lượng comment
                            int commentCount = currentExperience.getComments() != null ?
                                    currentExperience.getComments().size() + 1 : 1;
                            binding.btnComment.setText(String.valueOf(commentCount));
                            binding.textCommentCount.setText("(" + commentCount + ")");

                            // Thêm comment mới vào đầu danh sách
                            commentList.add(0, newComment);
                            if (commentList.size() > 3) {
                                commentList.remove(commentList.size() - 1);
                            }
                            commentAdapter.notifyDataSetChanged();

                            // Hiển thị nút xem tất cả
                            binding.btnViewAllComments.setVisibility(View.VISIBLE);

                            // Re-enable button
                            binding.btnSendComment.setEnabled(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isDestroyed) {
                            Toast.makeText(ExperienceDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.btnSendComment.setEnabled(true);
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        // Đánh dấu activity đã bị hủy
        isDestroyed = true;

        // Hủy đăng ký listener để tránh callback khi activity đã bị hủy
        if (experienceListener != null) {
            experienceRef.removeEventListener(experienceListener);
        }

        super.onDestroy();
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