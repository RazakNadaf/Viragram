package com.example.viragram.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.R;
import com.example.viragram.adapters.SearchClubAdapter;
import com.example.viragram.adapters.SearchEventAdapter;
import com.example.viragram.adapters.SearchStudentAdapter;
import com.example.viragram.databinding.FragmentSearchBinding;
import com.example.viragram.models.Club;
import com.example.viragram.models.Event;
import com.example.viragram.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment responsible for discovery, user search, clubs, events, and trending campus posts.
 */
public class SearchFragment extends Fragment {

    private static final String PREF_SEARCH_HISTORY = "search_history_prefs";
    private static final String KEY_RECENT_QUERIES = "recent_queries";

    private FragmentSearchBinding binding;

    private SearchStudentAdapter suggestedStudentAdapter;
    private SearchClubAdapter campusClubAdapter;
    private SearchEventAdapter upcomingEventAdapter;

    private SearchStudentAdapter activeSearchStudentAdapter;

    private final List<User> allStudents = new ArrayList<>();
    private final List<Club> allClubs = new ArrayList<>();
    private final List<Event> allEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDatasets();
        setupExploreRecyclerViews();
        setupSearchListeners();
        setupChips();
        updateRecentSearchesUi();
    }

    private void initDatasets() {
        // Temporary local demo data — Firebase will replace this later.

        // Students
        allStudents.clear();
        allStudents.add(new User("u1", "noor.f", "Noor Fernandes", "noor@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_2), "MSc Computer Science • 2nd Year", "Goa University"));
        allStudents.add(new User("u2", "rahul.patil", "Rahul Patil", "rahul@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_1), "MCA • 1st Year", "Goa University"));
        allStudents.add(new User("u3", "aarav.naik", "Aarav Naik", "aarav@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_3), "BSc Computer Science • 3rd Year", "Goa University"));
        allStudents.add(new User("u4", "priya.naik", "Priya Naik", "priya@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_4), "MSc Data Science • 2nd Year", "Goa University"));
        allStudents.add(new User("u5", "ananya.sawant", "Ananya Sawant", "ananya@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_6), "BCA • 2nd Year", "Goa University"));
        allStudents.add(new User("u6", "rohan.dessai", "Rohan Dessai", "rohan@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_5), "B.Tech CSE • Final Year", "Goa University"));

        // Clubs
        allClubs.clear();
        allClubs.add(new Club("c1", "Computer Science Club", "Official student technology & coding community at Goa University.", 128, R.drawable.ic_avatar_4));
        allClubs.add(new Club("c2", "Sports Club GU", "Campus athletics, football, and inter-department sports tournaments.", 215, R.drawable.ic_avatar_5));
        allClubs.add(new Club("c3", "Cultural Club", "Music, drama, and traditional arts performance hub.", 180, R.drawable.ic_avatar_6));
        allClubs.add(new Club("c4", "Photography Club", "Campus shutterbugs, photo walks, and visual storytelling.", 94, R.drawable.ic_avatar_2));
        allClubs.add(new Club("c5", "Robotics Club", "AI, IoT hardware, and automation student projects.", 76, R.drawable.ic_avatar_3));
        allClubs.add(new Club("c6", "Coding Club", "Competitive programming, DSA, and open-source development.", 150, R.drawable.ic_avatar_1));

        // Events
        allEvents.clear();
        allEvents.add(new Event("e1", "Goa University Cultural Fest 2026", "Fri, 15 March 2026", "4:00 PM", "Main Auditorium", "Annual campus cultural extravaganza featuring live music, drama, and dance battles.", "Cultural Club", R.drawable.bg_post_fest));
        allEvents.add(new Event("e2", "Hackathon 2026", "Sat, 22 March 2026", "9:00 AM", "CS Department Lab", "24-hour coding sprint building real-world campus & civic solutions.", "Computer Science Club", R.drawable.bg_post_coding));
        allEvents.add(new Event("e3", "Inter-College Sports Meet", "Fri, 28 March 2026", "8:00 AM", "Campus Athletics Ground", "Track, field, and football championship match day.", "Sports Club GU", R.drawable.bg_post_sports));
        allEvents.add(new Event("e4", "Android & AI Coding Workshop", "Wed, 2 April 2026", "2:00 PM", "Seminar Hall B", "Hands-on workshop building native mobile applications with AI APIs.", "Coding Club", R.drawable.bg_post_campus));
        allEvents.add(new Event("e5", "Campus Photography Exhibition", "Mon, 7 April 2026", "11:00 AM", "Art Gallery Corridor", "Displaying winning photographs captured across Goa University campus.", "Photography Club", R.drawable.bg_post_cultural));
    }

    private void setupExploreRecyclerViews() {
        suggestedStudentAdapter = new SearchStudentAdapter();
        binding.rvSuggestedStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSuggestedStudents.setAdapter(suggestedStudentAdapter);
        suggestedStudentAdapter.setStudents(allStudents);

        campusClubAdapter = new SearchClubAdapter();
        binding.rvCampusClubs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCampusClubs.setAdapter(campusClubAdapter);
        campusClubAdapter.setClubs(allClubs);

        upcomingEventAdapter = new SearchEventAdapter();
        binding.rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUpcomingEvents.setAdapter(upcomingEventAdapter);
        upcomingEventAdapter.setEvents(allEvents);

        activeSearchStudentAdapter = new SearchStudentAdapter();
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchResults.setAdapter(activeSearchStudentAdapter);
    }

    private void setupSearchListeners() {
        binding.etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (query.length() > 0) {
                    binding.btnClearSearch.setVisibility(View.VISIBLE);
                    performFilterSearch(query);
                } else {
                    binding.btnClearSearch.setVisibility(View.GONE);
                    restoreExploreState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchInput.setText("");
            restoreExploreState();
        });

        binding.btnClearAllHistory.setOnClickListener(v -> {
            clearSearchHistory();
            updateRecentSearchesUi();
        });
    }

    private void setupChips() {
        binding.chipGroupSearch.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String query = binding.etSearchInput.getText() != null ? binding.etSearchInput.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                performFilterSearch(query);
            }
        });
    }

    private void performFilterSearch(String query) {
        saveRecentQuery(query);
        String lowerQuery = query.toLowerCase();

        List<User> matchedStudents = new ArrayList<>();
        for (User u : allStudents) {
            if (u.getFullName().toLowerCase().contains(lowerQuery)
                    || u.getUsername().toLowerCase().contains(lowerQuery)
                    || u.getBio().toLowerCase().contains(lowerQuery)) {
                matchedStudents.add(u);
            }
        }

        binding.scrollExploreDefault.setVisibility(View.GONE);

        if (matchedStudents.isEmpty()) {
            binding.rvSearchResults.setVisibility(View.GONE);
            binding.layoutSearchEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutSearchEmptyState.setVisibility(View.GONE);
            binding.rvSearchResults.setVisibility(View.VISIBLE);
            activeSearchStudentAdapter.setStudents(matchedStudents);
        }
    }

    private void restoreExploreState() {
        binding.layoutSearchEmptyState.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.scrollExploreDefault.setVisibility(View.VISIBLE);
        updateRecentSearchesUi();
    }

    private void saveRecentQuery(String query) {
        if (query == null || query.trim().isEmpty() || getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_SEARCH_HISTORY, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_RECENT_QUERIES, "");
        List<String> list = new ArrayList<>(Arrays.asList(existing.split(",")));
        list.remove(query);
        list.add(0, query);
        if (list.size() > 5) {
            list = list.subList(0, 5);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).trim().isEmpty()) {
                sb.append(list.get(i)).append(",");
            }
        }
        prefs.edit().putString(KEY_RECENT_QUERIES, sb.toString()).apply();
    }

    private void clearSearchHistory() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_SEARCH_HISTORY, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_RECENT_QUERIES).apply();
    }

    private void updateRecentSearchesUi() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_SEARCH_HISTORY, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_RECENT_QUERIES, "");
        if (existing.trim().isEmpty()) {
            binding.layoutRecentSearchesContainer.setVisibility(View.GONE);
        } else {
            binding.layoutRecentSearchesContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}