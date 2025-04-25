package com.uilover.project1992.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uilover.project1992.Adapter.OrderAdapter;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MyOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private ArrayList<Flight> orderList;

    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        recyclerView = findViewById(R.id.orderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        buttonBack =findViewById(R.id.buttonBack);

        SharedPreferences prefs = getSharedPreferences("MyOrders", MODE_PRIVATE);
        String json = prefs.getString("orderList", "[]");

        Type type = new TypeToken<ArrayList<Flight>>() {}.getType();
        orderList = new Gson().fromJson(json, type);

        adapter = new OrderAdapter(orderList, this::onOrderClick, this::onOrderDelete);
        recyclerView.setAdapter(adapter);


        buttonBack.setOnClickListener(v->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onOrderDelete(int position) {
        // Xóa phần tử khỏi danh sách
        orderList.remove(position);

        // Cập nhật lại SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyOrders", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String updatedJson = new Gson().toJson(orderList);
        editor.putString("orderList", updatedJson);
        editor.apply();
        // Thông báo cho adapter biết dữ liệu đã thay đổi
        adapter.notifyItemRemoved(position);
    }

    private void onOrderClick(Flight flight) {
        Intent intent = new Intent(this, TicketDetailActivity.class);
        intent.putExtra("flight", flight);
        startActivity(intent);
    }
}
