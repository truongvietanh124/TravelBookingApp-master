package com.uilover.project1992.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uilover.project1992.Model.Flight;
import com.uilover.project1992.R;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final ArrayList<Flight> orderList;
    private final OnOrderClickListener listener;
    private final OnOrderDeleteListener deleteListener;


    public interface OnOrderClickListener {
        void onOrderClick(Flight flight);
    }

    // Interface cho xóa item
    public interface OnOrderDeleteListener {
        void onOrderDelete(int position);
    }

    public OrderAdapter(ArrayList<Flight> orderList, OnOrderClickListener listener, OnOrderDeleteListener deleteListener) {
        this.orderList = orderList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Flight flight = orderList.get(position);
        holder.airline.setText(flight.getAirlineName());
        holder.route.setText(flight.getFromShort() + " → " + flight.getToShort());
        holder.date.setText(flight.getDate());
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(flight));

        holder.deleteButton.setOnClickListener(v -> {
            deleteListener.onOrderDelete(position); // Gọi phương thức xóa
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView airline, route, date;
        ImageButton deleteButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            airline = itemView.findViewById(R.id.airlineName);
            route = itemView.findViewById(R.id.flightRoute);
            date = itemView.findViewById(R.id.flightDate);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}