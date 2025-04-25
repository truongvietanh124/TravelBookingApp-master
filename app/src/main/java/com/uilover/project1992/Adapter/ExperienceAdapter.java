package com.uilover.project1992.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.uilover.project1992.Model.Experience;
import com.uilover.project1992.R;
import com.uilover.project1992.databinding.ItemExperienceBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> {

    private Context context;
    private ArrayList<Experience> experienceList;
    private OnExperienceClickListener listener;

    public interface OnExperienceClickListener {
        void onExperienceClick(Experience experience);
        void onLikeClick(Experience experience);
        void onCommentClick(Experience experience);
    }

    public ExperienceAdapter(Context context, ArrayList<Experience> experienceList, OnExperienceClickListener listener) {
        this.context = context;
        this.experienceList = experienceList;
        this.listener = listener;
    }

    public class ExperienceViewHolder extends RecyclerView.ViewHolder {
        ItemExperienceBinding binding;

        public ExperienceViewHolder(ItemExperienceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemExperienceBinding binding = ItemExperienceBinding.inflate(inflater, parent, false);
        return new ExperienceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience experience = experienceList.get(position);

        holder.binding.tvTitle.setText(experience.getTitle());
        holder.binding.tvDescription.setText(experience.getDescription());
        holder.binding.tvLocation.setText(experience.getLocation());
        holder.binding.tvUsername.setText(experience.getUserName());

        // Format timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date date = new Date(experience.getTimestamp());
        holder.binding.tvTimestamp.setText(dateFormat.format(date));

        // Like count
        int likeCount = experience.getLikes() != null ? experience.getLikes().size() : 0;
        holder.binding.btnLike.setText(String.valueOf(likeCount));

        // Comment count
        int commentCount = experience.getComments() != null ? experience.getComments().size() : 0;
        holder.binding.btnComment.setText(String.valueOf(commentCount));

        // Rating
        float rating = experience.getRating();
        holder.binding.ratingBar.setRating(rating);
        holder.binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));

        // Load image
        Glide.with(context)
                .load(experience.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.binding.ivExperience);

        // Click listeners
        holder.itemView.setOnClickListener(v -> listener.onExperienceClick(experience));
        holder.binding.btnLike.setOnClickListener(v -> listener.onLikeClick(experience));
        holder.binding.btnComment.setOnClickListener(v -> listener.onCommentClick(experience));
    }

    @Override
    public int getItemCount() {
        return experienceList.size();
    }

    public void updateList(List<Experience> newList) {
        experienceList.clear();
        experienceList.addAll(newList);
        notifyDataSetChanged();
    }
}