package com.uilover.project1992.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.uilover.project1992.Model.Experience; // Import model Experience
import com.uilover.project1992.R; // Import R

import java.util.ArrayList;
import java.util.List;

public class FullCollectionAdapter extends RecyclerView.Adapter<FullCollectionAdapter.ViewHolder> {

    private List<Experience> experienceList;
    private Context context;
    // TODO: (Tùy chọn) Thêm Interface để xử lý click vào item
    // private OnItemClickListener listener;

    // Constructor
    public FullCollectionAdapter(List<Experience> experienceList, Context context) {
        this.experienceList = experienceList != null ? experienceList : new ArrayList<>(); // Đảm bảo list không null
        this.context = context;
    }

    // Phương thức cập nhật dữ liệu (cách đơn giản)
    public void updateData(List<Experience> newData) {
        this.experienceList.clear();
        if (newData != null) {
            this.experienceList.addAll(newData);
        }
        notifyDataSetChanged(); // Thông báo cập nhật toàn bộ
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_full_collection.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Experience experience = experienceList.get(position);

        // Load ảnh bằng Glide
        if (experience != null && experience.getImageUrl() != null) {
            Glide.with(context)
                    .load(experience.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder) // Placeholder đã tạo
                    .error(R.drawable.ic_image_error)       // Error drawable đã tạo (hoặc nên tạo)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.itemImageView);
        } else {
            // Xử lý trường hợp experience hoặc imageUrl là null (ví dụ: hiển thị ảnh lỗi)
            Glide.with(context)
                    .load(R.drawable.ic_image_error)
                    .centerCrop()
                    .into(holder.itemImageView);
        }

        // TODO: (Tùy chọn) Xử lý sự kiện click vào item
        // holder.itemView.setOnClickListener(v -> {
        //    if (listener != null) {
        //        listener.onItemClick(experience);
        //    }
        // });
    }

    @Override
    public int getItemCount() {
        return experienceList.size();
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // **QUAN TRỌNG:** Đảm bảo ID là "iv_collection_item_image" trong item_full_collection.xml
            itemImageView = itemView.findViewById(R.id.iv_collection_item_image);
        }
    }

    // TODO: (Tùy chọn) Interface cho sự kiện click
    // public interface OnItemClickListener {
    //     void onItemClick(Experience experience);
    // }
    //
    // public void setOnItemClickListener(OnItemClickListener listener) {
    //     this.listener = listener;
    // }
}