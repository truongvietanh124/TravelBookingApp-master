package com.uilover.project1992.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm import Log
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Thêm import Toast

import androidx.annotation.NonNull; // Thêm import NonNull

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Thêm import LayoutManager
import androidx.recyclerview.widget.RecyclerView; // Thêm import RecyclerView

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot; // Thêm import RTDB
import com.google.firebase.database.DatabaseError; // Thêm import RTDB
import com.google.firebase.database.DatabaseReference; // Thêm import RTDB
import com.google.firebase.database.FirebaseDatabase; // Thêm import RTDB
import com.google.firebase.database.Query; // Thêm import RTDB
import com.google.firebase.database.ValueEventListener; // Thêm import RTDB
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.uilover.project1992.Adapter.CollectionPreviewAdapter; // **IMPORT ADAPTER**
import com.uilover.project1992.Model.Experience; // **IMPORT MODEL**
import com.uilover.project1992.R;
// Bỏ import ActivityMainBinding không dùng đến
import com.uilover.project1992.databinding.ActivityProfileBinding;

import java.util.ArrayList; // Thêm import List
import java.util.Collections; // Thêm import Collections
import java.util.List; // Thêm import List

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding; // Sử dụng binding là chính
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore cho profile
    private FirebaseDatabase rtDb; // Realtime DB cho experiences
    private FirebaseUser currentUser;


    private LinearLayout myOrderLayout ;
    // Khai báo Adapter và Listener cho ảnh preview
    private CollectionPreviewAdapter previewAdapter;
    private ValueEventListener experiencePreviewListener;
    private Query userExperiencesQuery;

    private static final String TAG = "ProfileActivity"; // Tag cho Log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Ánh xạ các view từ layout
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvUserEmail = findViewById(R.id.tv_user_email); // ID của TextView
        backBtn = findViewById(R.id.backBtn); // Ánh xạ nút back
        myOrderLayout = findViewById(R.id.openOrder);
        // Khởi tạo Firebase
    

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        rtDb = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();


        myOrderLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyOrderActivity.class);
            startActivity(intent);
        });

        // --- Cài đặt RecyclerView cho ảnh Preview ---
        // Phải gọi trước khi load data để adapter sẵn sàng
        setupExperiencePreviewRecyclerView();

        // --- Listeners ---
        setupButtonClickListeners();

        // --- Tải dữ liệu ---
        if (currentUser != null) {
            loadUserProfile(); // Tải thông tin profile từ Firestore
            loadExperiencePreview(currentUser.getUid()); // Tải ảnh preview từ Realtime DB
        } else {
            // Xử lý trường hợp chưa đăng nhập
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            // Cân nhắc chuyển về màn hình Login
            goToLoginActivity(); // Gọi hàm chuyển màn hình Login
        }

        // --- Cài đặt Bottom Navigation ---
        setupBottomNav();
    }

    // Gom các listener vào đây cho gọn
    private void setupButtonClickListeners() {
        // Listener cho nút Back
        binding.backBtn.setOnClickListener(v -> onBackPressed()); // Cách tốt nhất để quay lại

        // Listener cho nút Edit Profile
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Listener cho nút Logout
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            goToLoginActivity(); // Gọi hàm chuyển màn hình Login
        });

        // Listener cho mục "Collection"
        binding.itemCollection.setOnClickListener(v -> {
            // TODO: Thay thế FullCollectionActivity.class bằng Activity/Fragment thực tế
            Intent intent = new Intent(ProfileActivity.this, FullCollectionActivity.class);
            if (currentUser != null) {
                // **THÊM DÒNG NÀY:** Đặt userId vào Intent extra
                intent.putExtra(FullCollectionActivity.EXTRA_USER_ID, currentUser.getUid());
                startActivity(intent);
            } else {
                // Có thể thông báo lỗi hoặc không làm gì nếu user không tồn tại
                Toast.makeText(this, "Lỗi thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExperiencePreviewRecyclerView() {
        // **QUAN TRỌNG:** Phải tạo lớp CollectionPreviewAdapter này
        // Khởi tạo adapter với danh sách rỗng ban đầu
        previewAdapter = new CollectionPreviewAdapter(new ArrayList<>(), this);

        // **QUAN TRỌNG:** Đảm bảo RecyclerView này có id "rv_collection_preview" trong XML
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvCollectionPreview.setLayoutManager(layoutManager);
        binding.rvCollectionPreview.setAdapter(previewAdapter);
        binding.rvCollectionPreview.setHasFixedSize(true); // Tối ưu hóa nếu item size không đổi
        binding.rvCollectionPreview.setNestedScrollingEnabled(false); // Tắt cuộn nested nếu RecyclerView nằm trong ScrollView

        // TODO: (Tùy chọn) Thêm ItemDecoration nếu muốn có khoảng cách giữa các ảnh
        // binding.rvCollectionPreview.addItemDecoration(...)
    }

    private void loadUserProfile() {
        if (currentUser == null) return; // Kiểm tra lại currentUser phòng trường hợp gọi từ nơi khác
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String profileImageUrl = document.getString("profileImageUrl");

                    binding.tvUsername.setText(name != null ? name : "N/A");
                    binding.tvUserEmail.setText(email != null ? email : "N/A");

                    Glide.with(this)
                            .load(profileImageUrl) // Glide xử lý được URL null/empty
                            .placeholder(R.drawable.ic_user_placeholder) // Ảnh mặc định khi tải
                            .error(R.drawable.ic_user_placeholder) // Ảnh mặc định khi lỗi
                            .into(binding.ivProfileImage);
                } else {
                    Log.d(TAG, "loadUserProfile: No such document for user " + userId);
                    // Set giá trị mặc định nếu không có thông tin
                    binding.tvUsername.setText("Người dùng mới");
                    binding.tvUserEmail.setText("");
                    binding.ivProfileImage.setImageResource(R.drawable.ic_user_placeholder);
                }
            } else {
                Log.e(TAG, "loadUserProfile: failed with ", task.getException());
                Toast.makeText(this, "Lỗi tải thông tin cá nhân.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExperiencePreview(String userId) {
        DatabaseReference dbRefExperiences = rtDb.getReference("experiences");
        // **QUAN TRỌNG:** Đảm bảo đã tạo Index "userId" trong Firebase Rules
        userExperiencesQuery = dbRefExperiences.orderByChild("userId")
                .equalTo(userId)
                .limitToLast(3); // Lấy 3 experiences gần nhất

        // Gỡ listener cũ (nếu có) trước khi thêm listener mới
        if (experiencePreviewListener != null) {
            userExperiencesQuery.removeEventListener(experiencePreviewListener);
            Log.d(TAG, "Removed previous experience listener.");
        }

        experiencePreviewListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> imageUrls = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "loadExperiencePreview: Found " + dataSnapshot.getChildrenCount() + " experiences");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // **QUAN TRỌNG:** Đảm bảo có model Experience và getter getImageUrl()
                        Experience experience = snapshot.getValue(Experience.class);
                        if (experience != null && experience.getImageUrl() != null && !experience.getImageUrl().isEmpty()) {
                            imageUrls.add(experience.getImageUrl());
                        } else if (experience == null) {
                            Log.w(TAG, "Experience object is null for snapshot: " + snapshot.getKey());
                        } else {
                            Log.w(TAG, "Experience image URL is null or empty for snapshot: " + snapshot.getKey());
                        }
                    }
                    // limitToLast trả về theo thứ tự key/priority. Để hiện mới nhất trước -> đảo ngược
                    Collections.reverse(imageUrls);
                } else {
                    Log.d(TAG, "loadExperiencePreview: No experiences found for user " + userId);
                }

                // Cập nhật adapter
                if (previewAdapter != null) {
                    previewAdapter.updateData(imageUrls);
                    Log.d(TAG, "Updated preview adapter with " + imageUrls.size() + " images.");
                } else {
                    Log.e(TAG, "previewAdapter is null, cannot update data.");
                }


                // Ẩn/Hiện RecyclerView
                binding.rvCollectionPreview.setVisibility(imageUrls.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadExperiencePreview:onCancelled", databaseError.toException());
                Toast.makeText(ProfileActivity.this, "Lỗi tải ảnh xem trước.", Toast.LENGTH_SHORT).show();
            }
        };
        // Thêm listener mới
        userExperiencesQuery.addValueEventListener(experiencePreviewListener);
        Log.d(TAG, "Added experience listener for user " + userId);
    }

    private void setupBottomNav() {
        // **QUAN TRỌNG:** Đảm bảo ChipNavigationBar có id "bottom_nav" trong XML
        ChipNavigationBar bottomNav = binding.bottomNav;
        bottomNav.setItemSelected(R.id.profile, true); // Đánh dấu mục Profile đang được chọn

        bottomNav.setOnItemSelectedListener(id -> {
            // Tối ưu: Nếu click vào mục đang chọn thì không làm gì cả
            if (id == R.id.profile) return;

            Intent intent = null;
            if (id == R.id.home) {
                intent = new Intent(ProfileActivity.this, MainActivity.class);
                // Mang MainActivity lên đầu nếu đã tồn tại, không tạo mới
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            } else if (id == R.id.explorer) {
                intent = new Intent(ProfileActivity.this, FeedActivity.class); // Hoặc Activity Explore thực tế
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            } else if (id == R.id.bookmark) {
                // intent = new Intent(ProfileActivity.this, BookmarkActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Toast.makeText(this, "Bookmark chưa triển khai", Toast.LENGTH_SHORT).show(); // Placeholder
                return; // Không chuyển màn hình nếu chưa có
            }
            // Trường hợp R.id.profile đã được xử lý ở trên

            if (intent != null) {
                startActivity(intent);
                // Không nên finish() ProfileActivity ở đây trừ khi bạn muốn nó bị xóa khỏi backstack
            }
        });
    }

    // Hàm tiện ích để chuyển về màn hình Login
    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Xóa hết các Activity trước đó khỏi stack, đảm bảo người dùng không quay lại được khi chưa đăng nhập
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Kết thúc Activity hiện tại
    }


    // **QUAN TRỌNG:** Gỡ bỏ listener khi Activity bị hủy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gỡ bỏ listener của Realtime Database
        if (userExperiencesQuery != null && experiencePreviewListener != null) {
            userExperiencesQuery.removeEventListener(experiencePreviewListener);
            Log.d(TAG, "onDestroy: Removed experiencePreviewListener");
        }
        // Listener của Firestore (get().addOnCompleteListener) tự động hủy sau khi hoàn thành
    }
}