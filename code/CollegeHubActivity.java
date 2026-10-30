package com.example.viragram.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.adapters.AnnouncementAdapter;
import com.example.viragram.adapters.SearchClubAdapter;
import com.example.viragram.adapters.SearchEventAdapter;
import com.example.viragram.adapters.TrendingCampusAdapter;
import com.example.viragram.databinding.ActivityCollegeHubBinding;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Announcement;
import com.example.viragram.models.Club;
import com.example.viragram.models.Event;
import com.example.viragram.utils.CollegeHubManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for Goa University College Hub campus discovery dashboard with real-time Firestore synchronization.
 */
public class CollegeHubActivity extends BaseActivity {

    private static final String TAG = "ViragramFirestore";

    private ActivityCollegeHubBinding binding;

    private TrendingCampusAdapter trendingAdapter;
    private SearchEventAdapter eventsAdapter;
    private SearchClubAdapter clubsAdapter;
    private AnnouncementAdapter announcementsAdapter;

    private ListenerRegistration eventsListener;
    private ListenerRegistration announcementsListener;
    private ListenerRegistration clubsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollegeHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerViews();
        setupChips();
        loadHubData();
    }

    private void setupRecyclerViews() {
        // 1. Trending
        trendingAdapter = new TrendingCampusAdapter();
        binding.rvTrendingCampus.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrendingCampus.setAdapter(trendingAdapter);

        // 2. Events
        eventsAdapter = new SearchEventAdapter();
        binding.rvHubEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHubEvents.setAdapter(eventsAdapter);

        // 3. Clubs
        clubsAdapter = new SearchClubAdapter();
        binding.rvHubClubs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHubClubs.setAdapter(clubsAdapter);

        // 4. Announcements
        announcementsAdapter = new AnnouncementAdapter();
        binding.rvHubAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHubAnnouncements.setAdapter(announcementsAdapter);
    }

    private void setupChips() {
        binding.chipGroupHub.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int selectedId = group.getCheckedChipId();
            if (selectedId == binding.chipHubTrending.getId()) {
                showSections(true, false, false, false);
            } else if (selectedId == binding.chipHubEvents.getId()) {
                showSections(false, true, false, false);
            } else if (selectedId == binding.chipHubClubs.getId()) {
                showSections(false, false, true, false);
            } else if (selectedId == binding.chipHubAnnouncements.getId()) {
                showSections(false, false, false, true);
            } else {
                showSections(true, true, true, true);
            }
        });
    }

    private void showSections(boolean showTrending, boolean showEvents, boolean showClubs, boolean showAnnouncements) {
        binding.layoutSectionTrending.setVisibility(showTrending ? View.VISIBLE : View.GONE);
        binding.layoutSectionEvents.setVisibility(showEvents ? View.VISIBLE : View.GONE);
        binding.layoutSectionClubs.setVisibility(showClubs ? View.VISIBLE : View.GONE);
        binding.layoutSectionAnnouncements.setVisibility(showAnnouncements ? View.VISIBLE : View.GONE);

        if (!showTrending && !showEvents && !showClubs && !showAnnouncements) {
            binding.layoutHubEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHubEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadHubData() {
        CollegeHubManager manager = CollegeHubManager.getInstance();
        trendingAdapter.setTrendingItems(manager.getTrendingEvents());

        // 1. Listen for Firestore Events
        eventsListener = FirestoreManager.getInstance().getPublishedEventsListener((querySnapshots, error) -> {
            List<Event> firestoreEvents = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    if (e != null) {
                        firestoreEvents.add(e);
                    }
                }
            }
            List<Event> allEvents = new ArrayList<>(firestoreEvents);
            allEvents.addAll(manager.getUpcomingEvents());
            eventsAdapter.setEvents(allEvents);
        });

        // 2. Listen for Firestore Announcements
        announcementsListener = FirestoreManager.getInstance().getPublishedAnnouncementsListener((querySnapshots, error) -> {
            List<Announcement> firestoreAnnouncements = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Announcement a = doc.toObject(Announcement.class);
                    if (a != null) {
                        firestoreAnnouncements.add(a);
                    }
                }
            }
            List<Announcement> allAnnouncements = new ArrayList<>(firestoreAnnouncements);
            allAnnouncements.addAll(manager.getAnnouncements());
            announcementsAdapter.setAnnouncements(allAnnouncements);
        });

        // 3. Listen for Firestore Clubs
        clubsListener = FirestoreManager.getInstance().getClubsListener((querySnapshots, error) -> {
            List<Club> firestoreClubs = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Club c = doc.toObject(Club.class);
                    if (c != null) {
                        firestoreClubs.add(c);
                    }
                }
            }
            List<Club> allClubs = new ArrayList<>(firestoreClubs);
            allClubs.addAll(manager.getCampusClubs());
            clubsAdapter.setClubs(allClubs);
        });
    }

    @Override
    protected void onDestroy() {
        if (eventsListener != null) eventsListener.remove();
        if (announcementsListener != null) announcementsListener.remove();
        if (clubsListener != null) clubsListener.remove();
        super.onDestroy();
    }
}