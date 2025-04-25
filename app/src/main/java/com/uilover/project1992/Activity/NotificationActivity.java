package com.uilover.project1992.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;

public class NotificationActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private Button notificationButton;
    private TextView noNotificationText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Ánh xạ các view
        noNotificationText = findViewById(R.id.noNotificationText);
        notificationButton = findViewById(R.id.notificationButton);
        imageButton = findViewById(R.id.buttonBack);


        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        String json = prefs.getString("flight_data", null);

        if (json != null) {
            Gson gson = new Gson();
            Flight flight = gson.fromJson(json, Flight.class);

            String message = "Bạn đã đặt thành công vé máy bay: " +
                    flight.getAirlineName() +
                    ", Ghế: " + flight.getPassenger() +
                    ", Ngày: " + flight.getDate();

            notificationButton.setText(message);
            notificationButton.setVisibility(View.VISIBLE);
            noNotificationText.setVisibility(View.GONE);

            notificationButton.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationActivity.this, TicketDetailActivity.class);
                intent.putExtra("flight", flight); // Gửi lại toàn bộ đối tượng flight
                startActivity(intent);
            });
        } else {
            // Không có dữ liệu
            notificationButton.setVisibility(View.GONE);
            noNotificationText.setVisibility(View.VISIBLE);
        }

//        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
//        String message = prefs.getString("last_notification", null);
//
//        if (message != null) {
//            noNotificationText.setVisibility(View.GONE);
//            notificationButton.setVisibility(View.VISIBLE);
//            notificationButton.setText(message);
//
//            notificationButton.setOnClickListener(v -> {
//                // Mở TicketDetailActivity khi bấm vào thông báo
//                Intent intent = new Intent(NotificationActivity.this, TicketDetailActivity.class);
//                intent.putExtra("flightName", prefs.getString("flightName", ""));
//                intent.putExtra("seat", prefs.getString("seat", ""));
//                intent.putExtra("date", prefs.getString("date", ""));
//                startActivity(intent);
//            });
//        } else {
//            noNotificationText.setVisibility(View.VISIBLE);
//            notificationButton.setVisibility(View.GONE);
//        }

        // Xử lý nút quay lại
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
