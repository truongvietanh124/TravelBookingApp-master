package com.uilover.project1992.Activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;
import com.uilover.project1992.databinding.ActivityTicketDetailBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class TicketDetailActivity extends BaseActivity {
    private ActivityTicketDetailBinding binding;
    private Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        setVariable();


        binding.confirmButton.setOnClickListener(v -> {
            Log.d("TicketDetailActivity", "Confirm button clicked");

            // Lưu chuyến bay vào Firebase
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications");
            String id = ref.push().getKey();
            ref.child(id).setValue(flight);

            // Tạo intent mở TicketDetail khi nhấn thông báo
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("flight", flight);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Tạo thông báo
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Đặt vé thành công")
                    .setContentText("Bạn đã đặt vé: " + flight.getAirlineName())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                } else {
                    manager.notify(1001, builder.build());
                }
            } else {
                manager.notify(1001, builder.build());
            }

            // Dù gì cũng quay lại MainActivity
            Log.d("TicketDetailActivity", "Going back to MainActivity");
            startActivity(new Intent(TicketDetailActivity.this, MainActivity.class));
            finish();
            Log.d("TicketDetailActivity", "finish() called");
        });
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.fromTxt.setText(flight.getFromShort());
        binding.fromSmallTxt.setText(flight.getFrom());
        binding.toTxt.setText(flight.getTo());
        binding.toShortTxt.setText(flight.getToShort());
        binding.toSmallTxt.setText(flight.getTo());
        binding.dateTxt.setText(flight.getDate());
        binding.timeTxt.setText(flight.getTime());
        binding.arrivalTxt.setText(flight.getArriveTime());
        binding.classTxt.setText(flight.getClassSeat());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN")); // Định dạng theo chuẩn VND
        String priceFormatted = formatter.format(flight.getPrice()) + " đ"; // Thêm ký hiệu "đ" vào

        binding.priceTxt.setText(priceFormatted); // Hiển thị giá đã được định dạng

        binding.airlines.setText(flight.getAirlineName());
        binding.seatsTxt.setText(flight.getPassenger());

        Glide.with(TicketDetailActivity.this)
                .load(flight.getAirlineLogo())
                .into(binding.logo);
    }
    private void getIntentExtra() {
        flight = (Flight) getIntent().getSerializableExtra("flight");
    }

}