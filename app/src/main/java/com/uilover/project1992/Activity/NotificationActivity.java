package com.uilover.project1992.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.uilover.project1992.Adapter.NotificationAdapter;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Flight> flightList = new ArrayList<>();

    ImageButton buttonClear ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        imageButton = findViewById(R.id.buttonBack);
        buttonClear = findViewById(R.id.buttonClear);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(flightList, this);
        recyclerView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        flightList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Flight flight = data.getValue(Flight.class);
                            flightList.add(flight);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Xử lý nút quay lại
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        buttonClear.setOnClickListener(v -> {
            // Xóa toàn bộ dữ liệu trong node "notifications"
            FirebaseDatabase.getInstance().getReference("notifications").removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã xóa tất cả thông báo", Toast.LENGTH_SHORT).show();
                            flightList.clear();            // Xóa trong danh sách local
                            adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                        } else {
                            Toast.makeText(this, "Lỗi khi xóa thông báo", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
