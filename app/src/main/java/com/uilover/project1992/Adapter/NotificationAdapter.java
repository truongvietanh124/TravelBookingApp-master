package com.uilover.project1992.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uilover.project1992.Activity.TicketDetailActivity;
import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Flight> flightList;
    private Context context;

    public NotificationAdapter(List<Flight> flightList, Context context) {
        this.flightList = flightList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight flight = flightList.get(position);
        holder.btnNotification.setText("Bạn đã đặt vé: " + flight.getAirlineName());

        holder.btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra("flight", flight);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return flightList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button btnNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnNotification = itemView.findViewById(R.id.btnNotification);
        }
    }
}