package com.uilover.project1992.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uilover.project1992.Adapter.FlightAdapter;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.databinding.ActivitySearchBinding;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {
    private ActivitySearchBinding binding;
    private String from, to, date;
    private int numPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
        setVariable();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
        finish();
        });
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Flights");
        ArrayList<Flight> list = new ArrayList<>();

        // Chuẩn hóa biến 'from' (xóa khoảng trắng, chữ thường hết)
        String fromNormalized = from.trim().toLowerCase();

        Query query = myRef.orderByChild("from").equalTo(from);
        Log.d("FIREBASE_QUERY", "Querying for flights from: " + fromNormalized);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE_QUERY", "DataSnapshot exists: " + snapshot.exists());

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Flight flight = issue.getValue(Flight.class);
                        if (flight == null) continue;

                        // Chuẩn hóa dữ liệu lấy từ Firebase để so sánh
                        String flightTo = flight.getTo().trim().toLowerCase();
                        String expectedTo = to.trim().toLowerCase();

                        Log.d("FIREBASE_QUERY", "Comparing flight.to = '" + flightTo + "' with to = '" + expectedTo + "'");

                        if (flightTo.equals(expectedTo)) {
                            list.add(flight);
                            Log.d("FIREBASE_QUERY", "✅ Flight matched and added: " + flight.getFrom() + " -> " + flight.getTo());
                        }
                    }

                    Log.d("FIREBASE_QUERY", "Total flights found: " + list.size());

                    if (!list.isEmpty()) {
                        binding.searchView.setLayoutManager(new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false));
                        binding.searchView.setAdapter(new FlightAdapter(list));
                    } else {
                        Log.d("FIREBASE_QUERY", "❌ No matching flights found.");
                    }

                    binding.progressBarSearch.setVisibility(View.GONE);
                } else {
                    Log.d("FIREBASE_QUERY", "❌ No snapshot found for from = " + fromNormalized);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_QUERY", "Firebase query cancelled: " + error.getMessage());
            }
        });
    }

    private void getIntentExtra() {
        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date = getIntent().getStringExtra("date");
    }
}