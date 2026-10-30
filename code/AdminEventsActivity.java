package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.adapters.AdminEventAdapter;
import com.example.viragram.databinding.ActivityAdminEventsBinding;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends BaseActivity {

    private static final String TAG = "ViragramAdminEvent";

    private ActivityAdminEventsBinding binding;
    private AdminEventAdapter eventAdapter;
    private ListenerRegistration eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCreateEventActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        loadEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new AdminEventAdapter(new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(Event event) {
                Log.d(TAG, "Editing event ID: " + event.getEventId());
                Intent intent = new Intent(AdminEventsActivity.this, AdminCreateEventActivity.class);
                intent.putExtra(AdminCreateEventActivity.EXTRA_EVENT_ID, event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Event event) {
                new MaterialAlertDialogBuilder(AdminEventsActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (event.getEventId() != null) {
                                Log.d(TAG, "Deleting event ID: " + event.getEventId());
                                FirestoreManager.getInstance().deleteEvent(event.getEventId(), task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Event deleted successfully: " + event.getEventId());
                                        Toast.makeText(AdminEventsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e(TAG, "Error deleting event: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                                        Toast.makeText(AdminEventsActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        
        binding.rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminEvents.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        Log.d(TAG, "Loading events from Firestore...");
        
        // Use generic collection listener because admin needs to see Drafts as well (published=false)
        eventsListener = FirestoreManager.getInstance().getFirestore().collection("events").addSnapshotListener((querySnapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Firestore error loading events: " + error.getMessage());
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Event> events = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    if (e != null) {
                        events.add(e);
                    }
                }
                Log.d(TAG, "Loaded " + events.size() + " events.");
            } else {
                Log.d(TAG, "No events found.");
            }

            if (events.isEmpty()) {
                binding.rvAdminEvents.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvAdminEvents.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                eventAdapter.setEvents(events);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (eventsListener != null) {
            eventsListener.remove();
        }
        super.onDestroy();
    }
}