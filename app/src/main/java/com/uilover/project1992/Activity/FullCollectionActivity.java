package com.uilover.project1992.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import ViewBinding cho layout này
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uilover.project1992.Adapter.FullCollectionAdapter;
import com.uilover.project1992.Model.Experience;
import com.uilover.project1992.R;
import com.uilover.project1992.databinding.ActivityFullCollectionBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullCollectionActivity extends AppCompatActivity {

    private static final String TAG = "FullCollectionActivity";
    public static final String EXTRA_USER_ID = "com.uilover.project1992.USER_ID";

    private ActivityFullCollectionBinding binding;
    private String targetUserId;

    // Views (khai báo lại cho rõ ràng)
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    // --- Biến cho Adapter và Firebase ---
    private FullCollectionAdapter collectionAdapter;
    private FirebaseDatabase rtDb;
    private DatabaseReference dbRefExperiences;
    private ValueEventListener collectionListener;
    private Query collectionQuery;
    private List<Experience> experienceList; // Danh sách để giữ dữ liệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase Realtime DB
        rtDb = FirebaseDatabase.getInstance();
        dbRefExperiences = rtDb.getReference("experiences"); // Tham chiếu đến nút experiences

        // Lấy userId được gửi từ Intent
        targetUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (targetUserId == null || targetUserId.isEmpty()) {
            Log.e(TAG, "User ID is missing!");
            Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Displaying collection for user ID: " + targetUserId);

        // Ánh xạ Views
        toolbar = binding.toolbarFullCollection;
        recyclerView = binding.rvFullCollection;
        progressBar = binding.progressBarFullCollection;
        emptyTextView = binding.textViewEmptyCollection;

        // Cài đặt Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bộ sưu tập");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Cài đặt RecyclerView ---
        setupRecyclerView();

        // --- Tải dữ liệu ---
        loadCollectionData(targetUserId);

    }

    // Xử lý sự kiện khi nhấn nút back trên Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        experienceList = new ArrayList<>(); // Khởi tạo danh sách rỗng
        collectionAdapter = new FullCollectionAdapter(experienceList, this);

        // Sử dụng GridLayoutManager để hiển thị dạng lưới (ví dụ: 3 cột)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(collectionAdapter);
        recyclerView.setHasFixedSize(true); // Tối ưu hóa

        // TODO: (Tùy chọn) Xử lý click vào item trong adapter
        // collectionAdapter.setOnItemClickListener(experience -> {
        //     // Mở màn hình chi tiết experience
        //     Intent detailIntent = new Intent(this, ExperienceDetailActivity.class);
        //     // detailIntent.putExtra("EXPERIENCE_ID", experience.getId()); // Truyền ID hoặc cả object
        //     startActivity(detailIntent);
        // });
    }

    private void loadCollectionData(String userId) {
        Log.d(TAG, "Loading collection data for user: " + userId);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // Gỡ listener cũ nếu có
        if (collectionQuery != null && collectionListener != null) {
            collectionQuery.removeEventListener(collectionListener);
            Log.d(TAG,"Removed previous collection listener.");
        }

        // **QUAN TRỌNG:** Đảm bảo có index "userId" và "timestamp" (nếu dùng) trong Rules
        collectionQuery = dbRefExperiences.orderByChild("userId").equalTo(userId);
        // Nếu muốn sắp xếp theo thời gian mới nhất -> cũ nhất, có thể cần query phức tạp hơn
        // hoặc sắp xếp lại list sau khi lấy về. Tạm thời lấy theo thứ tự Firebase trả về.

        collectionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                experienceList.clear(); // Xóa dữ liệu cũ trước khi thêm mới
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Data snapshot exists. Found " + dataSnapshot.getChildrenCount() + " items.");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Experience experience = snapshot.getValue(Experience.class);
                        if (experience != null) {
                            experienceList.add(experience);
                        } else {
                            Log.w(TAG, "Skipping null experience object for key: " + snapshot.getKey());
                        }
                    }
                    // Sắp xếp lại danh sách nếu cần (ví dụ: mới nhất lên đầu)
                    // Dùng timestamp để sắp xếp (giả sử timestamp là long milliseconds)
                    Collections.sort(experienceList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                    Log.d(TAG, "Loaded and sorted " + experienceList.size() + " experiences.");

                } else {
                    Log.d(TAG, "No experiences found for user: " + userId);
                }

                progressBar.setVisibility(View.GONE); // Ẩn progress bar

                if (experienceList.isEmpty()) {
                    // Hiển thị thông báo rỗng
                    recyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Displaying empty collection message.");
                } else {
                    // Hiển thị RecyclerView và cập nhật adapter
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    collectionAdapter.updateData(experienceList); // Cập nhật dữ liệu mới
                    Log.d(TAG, "Updating adapter. Displaying RecyclerView.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE); // Ẩn progress bar
                recyclerView.setVisibility(View.GONE); // Ẩn cả recycler view
                emptyTextView.setText("Lỗi tải dữ liệu"); // Hiển thị thông báo lỗi
                emptyTextView.setVisibility(View.VISIBLE);
                Log.e(TAG, "loadCollectionData:onCancelled", databaseError.toException());
                Toast.makeText(FullCollectionActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        collectionQuery.addValueEventListener(collectionListener); // Gắn listener
        Log.d(TAG,"Added collection listener.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gỡ bỏ listener khi activity bị hủy để tránh memory leak
        if (collectionQuery != null && collectionListener != null) {
            collectionQuery.removeEventListener(collectionListener);
            Log.d(TAG,"onDestroy: Removed collection listener.");
        }
    }
}