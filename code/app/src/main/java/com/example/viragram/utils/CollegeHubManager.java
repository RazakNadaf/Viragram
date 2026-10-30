package com.example.viragram.utils;

import com.example.viragram.R;
import com.example.viragram.models.Announcement;
import com.example.viragram.models.Club;
import com.example.viragram.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository manager for College Hub campus community features.
 */
public class CollegeHubManager {

    private static CollegeHubManager instance;

    private final List<Event> trendingEvents = new ArrayList<>();
    private final List<Event> upcomingEvents = new ArrayList<>();
    private final List<Club> campusClubs = new ArrayList<>();
    private final List<Announcement> announcements = new ArrayList<>();

    private CollegeHubManager() {
        initDemoData();
    }

    public static synchronized CollegeHubManager getInstance() {
        if (instance == null) {
            instance = new CollegeHubManager();
        }
        return instance;
    }

    private void initDemoData() {
        // 1. Trending Items
        trendingEvents.clear();
        trendingEvents.add(new Event("t1", "Hackathon 2026", "Sat, 22 March", "9:00 AM", "CS Dept Lab", "24-hour coding sprint with prize pool", "CS Club", R.drawable.goa_university_lab));
        trendingEvents.add(new Event("t2", "College Fest 2026", "Fri, 15 March", "4:00 PM", "Main Auditorium", "Annual cultural evening & music battle", "Cultural Club", R.drawable.goa_university_event));
        trendingEvents.add(new Event("t3", "Inter-College Sports", "Fri, 28 March", "8:00 AM", "Athletics Ground", "Football & track championship", "Sports Club GU", R.drawable.goa_university_sports));
        trendingEvents.add(new Event("t4", "Android & AI Workshop", "Wed, 2 April", "2:00 PM", "Seminar Hall B", "Hands-on mobile AI app development", "Coding Club", R.drawable.goa_university_library));

        // 2. Upcoming Events
        upcomingEvents.clear();
        upcomingEvents.add(new Event("e1", "Goa University Cultural Fest 2026", "Fri, 15 March 2026", "4:00 PM onwards", "Main Auditorium", "Annual campus cultural extravaganza featuring live music, drama, and dance battles.", "Cultural Club", R.drawable.goa_university_event));
        upcomingEvents.add(new Event("e2", "Hackathon 2026", "Sat, 22 March 2026", "9:00 AM onwards", "CS Department Lab", "24-hour coding sprint building real-world campus & civic solutions.", "Computer Science Club", R.drawable.goa_university_lab));
        upcomingEvents.add(new Event("e3", "Inter-College Sports Meet", "Fri, 28 March 2026", "8:00 AM onwards", "Campus Athletics Ground", "Track, field, and football championship match day.", "Sports Club GU", R.drawable.goa_university_sports));
        upcomingEvents.add(new Event("e4", "Android & AI Coding Workshop", "Wed, 2 April 2026", "2:00 PM onwards", "Seminar Hall B", "Hands-on workshop building native mobile applications with AI APIs.", "Coding Club", R.drawable.goa_university_admin));
        upcomingEvents.add(new Event("e5", "Campus Photography Exhibition", "Mon, 7 April 2026", "11:00 AM onwards", "Art Gallery Corridor", "Displaying winning photographs captured across Goa University campus.", "Photography Club", R.drawable.goa_university_campus));

        // 3. Campus Clubs
        campusClubs.clear();
        campusClubs.add(new Club("c1", "Computer Science Club", "Official student technology & coding community at Goa University.", 128, R.drawable.goa_university_lab));
        campusClubs.add(new Club("c2", "Sports Club GU", "Campus athletics, football, and inter-department sports tournaments.", 215, R.drawable.goa_university_sports));
        campusClubs.add(new Club("c3", "Cultural Club", "Music, drama, and traditional arts performance hub.", 180, R.drawable.goa_university_auditorium));
        campusClubs.add(new Club("c4", "Photography Club", "Campus shutterbugs, photo walks, and visual storytelling.", 94, R.drawable.goa_university_campus));
        campusClubs.add(new Club("c5", "Robotics Club", "AI, IoT hardware, and automation student projects.", 76, R.drawable.goa_university_students));
        campusClubs.add(new Club("c6", "Coding Club", "Competitive programming, DSA, and open-source development.", 150, R.drawable.goa_university_library));

        // 4. Announcements
        announcements.clear();
        announcements.add(new Announcement("a1", "Semester Registration Deadline Extended", "Goa University academic section has extended the course registration deadline to Friday, 10th March without late fee.", "ACADEMIC", "University Registrar", "Today, 10:15 AM", R.drawable.goa_university_admin));
        announcements.add(new Announcement("a2", "University Cultural Fest Registrations Open", "Registrations for solo singing, group dance, and drama competitions are now open on Viragram.", "EVENT", "Cultural Committee", "Yesterday", R.drawable.goa_university_event));
        announcements.add(new Announcement("a3", "Inter-Department Football Team Trials", "Trials for Goa University inter-department football squad will take place at the Athletics Ground this Thursday at 4 PM.", "SPORTS", "Sports Department", "2 days ago", R.drawable.goa_university_sports));
        announcements.add(new Announcement("a4", "Central Library Extended Reading Hours", "The central library reading hall will remain open until midnight during mid-term examination week.", "CAMPUS", "Library Committee", "3 days ago", R.drawable.goa_university_library));
    }

    public List<Event> getTrendingEvents() {
        return trendingEvents;
    }

    public List<Event> getUpcomingEvents() {
        return upcomingEvents;
    }

    public List<Club> getCampusClubs() {
        return campusClubs;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }
}