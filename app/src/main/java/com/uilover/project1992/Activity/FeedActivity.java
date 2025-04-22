package com.uilover.project1992.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uilover.project1992.Adapter.ExperienceAdapter;
import com.uilover.project1992.Model.Experience;
import com.uilover.project1992.databinding.ActivityFeedBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FeedActivity extends AppCompatActivity implements ExperienceAdapter.OnExperienceClickListener {

    private ActivityFeedBinding binding;
    private ExperienceAdapter adapter;
    private final ArrayList<Experience> experienceList = new ArrayList<>();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize RecyclerView
        adapter = new ExperienceAdapter(this, experienceList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Set up floating action button for posting new experience
        binding.fabAddExperience.setOnClickListener(v ->
                startActivity(new Intent(FeedActivity.this, ExploreActivity.class)));

        // Load data from Firebase
        loadExperiences();
    }

    private void loadExperiences() {
        binding.progressBar.setVisibility(View.VISIBLE);

        database.getReference().child("experiences")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        experienceList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Experience experience = dataSnapshot.getValue(Experience.class);
                            if (experience != null) {
                                experienceList.add(experience);
                            }
                        }

                        // Sort by timestamp (newest first)
                        Collections.sort(experienceList, new Comparator<Experience>() {
                            @Override
                            public int compare(Experience o1, Experience o2) {
                                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                            }
                        });

                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvEmptyState.setVisibility(experienceList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onExperienceClick(Experience experience) {
      Intent intent = new Intent(this, ExperienceDetailActivity.class);
       intent.putExtra("EXPERIENCE_ID", experience.getId());
        startActivity(intent);
    }

    @Override
    public void onLikeClick(Experience experience) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        com.google.firebase.database.DatabaseReference experienceLikeRef = database.getReference()
                .child("experiences")
                .child(experience.getId())
                .child("likes")
                .child(userId);

        experienceLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    experienceLikeRef.removeValue();
                } else {
                    experienceLikeRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @Override
    public void onCommentClick(Experience experience) {
    //    Intent intent = new Intent(this, CommentActivity.class);
      //  intent.putExtra("EXPERIENCE_ID", experience.getId());
     //   startActivity(intent);
    }

}
