package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.adapters.SearchClubAdapter;
import com.example.viragram.databinding.ActivityAdminEventsBinding; // Reusing event layout for simplicity
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Club;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminClubsActivity extends BaseActivity {

    private ActivityAdminEventsBinding binding; // Reusing XML layout
    private SearchClubAdapter clubAdapter;
    private ListenerRegistration clubsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Manage Clubs");
        binding.fabCreateEvent.setText("Create Club");
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCreateClubActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        loadClubs();
    }

    private void setupRecyclerView() {
        clubAdapter = new SearchClubAdapter();
        // Overriding the click listener internally in the real version, but here we can just use the adapter
        binding.rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminEvents.setAdapter(clubAdapter);
    }

    private void loadClubs() {
        clubsListener = FirestoreManager.getInstance().getFirestore().collection("clubs").addSnapshotListener((querySnapshots, error) -> {
            if (error != null) {
                Toast.makeText(this, "Failed to load clubs", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Club> clubs = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Club c = doc.toObject(Club.class);
                    if (c != null) {
                        clubs.add(c);
                    }
                }
            }

            if (clubs.isEmpty()) {
                binding.rvAdminEvents.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvAdminEvents.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                clubAdapter.setClubs(clubs);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (clubsListener != null) {
            clubsListener.remove();
        }
        super.onDestroy();
    }
}