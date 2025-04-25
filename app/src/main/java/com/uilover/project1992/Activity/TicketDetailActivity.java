package com.uilover.project1992.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.uilover.project1992.Model.Flight;
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
            SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Chuyển flight sang JSON
            Gson gson = new Gson();
            String flightJson = gson.toJson(flight);

            // Lưu dữ liệu
            editor.putString("last_notification", "Bạn đã đặt thành công vé máy bay!");
            editor.putString("flight_data", flightJson);
            editor.apply();

            // Quay lại MainActivity
            Intent intent = new Intent(TicketDetailActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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