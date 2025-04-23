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
import com.uilover.project1992.R; // Đảm bảo bạn đã import R đúng

import java.util.List;

public class CollectionPreviewAdapter extends RecyclerView.Adapter<CollectionPreviewAdapter.ViewHolder> {

    private List<String> imageUrls;
    private Context context;

    // Constructor để nhận dữ liệu và context
    public CollectionPreviewAdapter(List<String> imageUrls, Context context) {
        this.imageUrls = imageUrls;
        this.context = context;
    }

    // Phương thức để cập nhật dữ liệu cho adapter
    // Đây là cách đơn giản, sử dụng ListAdapter + DiffUtil sẽ hiệu quả hơn cho danh sách lớn/thay đổi thường xuyên
    public void updateData(List<String> newData) {
        this.imageUrls.clear();
        if (newData != null) {
            this.imageUrls.addAll(newData);
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại toàn bộ view
    }

    // Tạo ViewHolder mới (được gọi bởi LayoutManager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho một item ảnh preview
        View view = LayoutInflater.from(context).inflate(R.layout.item_collection_preview_image, parent, false);
        return new ViewHolder(view);
    }

    // Gắn dữ liệu vào ViewHolder (được gọi bởi LayoutManager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy URL ảnh tại vị trí hiện tại
        String imageUrl = imageUrls.get(position);

        // Sử dụng Glide để tải ảnh từ URL vào ImageView
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder) // **TODO:** Tạo drawable placeholder này
                .error(R.drawable.ic_image_error)       // **TODO:** Tạo drawable báo lỗi này
                .centerCrop() // Hoặc dùng .fitCenter() tùy theo thiết kế
                .transition(DrawableTransitionOptions.withCrossFade()) // Hiệu ứng mờ dần khi ảnh tải xong (tùy chọn)
                .into(holder.previewImageView); // Target ImageView
    }
    // Trả về tổng số lượng item trong danh sách
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // Lớp ViewHolder để giữ tham chiếu đến các View trong item layout (tránh findViewById nhiều lần)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ImageView từ layout item_collection_preview_image.xml
            // **QUAN TRỌNG:** Đảm bảo ID của ImageView trong XML là "iv_preview_image"
            previewImageView = itemView.findViewById(R.id.iv_preview_image);
        }
    }
}