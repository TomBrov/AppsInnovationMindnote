package com.example.mindnote;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MaterialButton addEntryButton;
    private TextView viewAllButton;
    private LinearLayout recentEntriesContainer;
    private JournalDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataManager = JournalDataManager.getInstance(this);

        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
            dataManager.setAnalytics(analytics);
        } catch (SecurityException e) {
            android.util.Log.e("FirebaseInit", "Google Play Services call failed", e);
        }

        initViews();
        setupNavigation();
        setupButtonListeners();
        updateStats();
        updateRecentEntries();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        addEntryButton = findViewById(R.id.addEntryButton);
        viewAllButton = findViewById(R.id.viewAllButton);
        recentEntriesContainer = findViewById(R.id.recentEntriesContainer);

        if (recentEntriesContainer == null) {
            View scrollView = findViewById(R.id.scrollView);
            if (scrollView instanceof android.widget.ScrollView) {
                LinearLayout parent = (LinearLayout) ((android.widget.ScrollView) scrollView).getChildAt(0);
                recentEntriesContainer = parent;
            }
        }
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_journal) {
                startActivity(new Intent(MainActivity.this, JournalActivity.class));
            } else if (itemId == R.id.navigation_notes) {
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            } else if (itemId == R.id.navigation_calendar) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            } else if (itemId == R.id.navigation_profile) {
                Snackbar.make(findViewById(android.R.id.content), "Profile feature coming soon", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void setupButtonListeners() {
        addEntryButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, JournalActivity.class)));
        viewAllButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
        updateRecentEntries();
    }

    private void updateStats() {
        int entryCount = dataManager.getEntryCount();

        TextView streakTextView = findViewById(R.id.streakTextView);
        TextView streakFlameIcon = findViewById(R.id.streakFlameIcon);
        TextView entriesTextView = findViewById(R.id.entriesTextView);

        int streak = calculateStreak();
        if (streakTextView != null) {
            streakTextView.setText(streak + " day streak");
        }

        if (streakFlameIcon != null) {
            streakFlameIcon.setVisibility(streak >= 3 ? View.VISIBLE : View.GONE);
            streakFlameIcon.setText("🔥"); // just to be sure, though already in XML
        }

        if (entriesTextView != null) {
            entriesTextView.setText(entryCount + " total entries");
        }
    }

    private int calculateStreak() {
        List<JournalEntry> entries = dataManager.getAllEntries();

        entries.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));

        int streak = 0;
        long currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24);

        for (JournalEntry entry : entries) {
            long entryDay = entry.getDate().getTime() / (1000 * 60 * 60 * 24);
            if (entryDay == currentDay - streak) {
                streak++;
            } else if (entryDay < currentDay - streak) {
                break;
            }
        }

        return streak;
    }

    private void updateRecentEntries() {
        if (recentEntriesContainer == null) return;
        recentEntriesContainer.removeAllViews();

        dataManager.loadEntriesFromFirestore(entries -> {
            if (entries.isEmpty()) {
                showEmptyStateIfNeeded();
            } else {
                displayRecentEntries(entries);
            }
        });
    }

    private void showEmptyStateIfNeeded() {
        if (recentEntriesContainer.getChildCount() == 0) {
            View emptyView = LayoutInflater.from(this)
                    .inflate(R.layout.empty_recent_entries, recentEntriesContainer, false);
            recentEntriesContainer.addView(emptyView);
        }
    }

    private void displayRecentEntries(List<JournalEntry> entries) {
        int entryLimit = Math.min(entries.size(), 3);

        for (int i = 0; i < entryLimit; i++) {
            JournalEntry entry = entries.get(i);
            View entryView = createEntryView(entry);
            recentEntriesContainer.addView(entryView);
        }
    }

    private View createEntryView(JournalEntry entry) {
        View entryView = LayoutInflater.from(this)
                .inflate(R.layout.item_recent_entry, recentEntriesContainer, false);

        TextView dateText = entryView.findViewById(R.id.dateText);
        TextView contentText = entryView.findViewById(R.id.contentText);
        ImageView entryImage = entryView.findViewById(R.id.entryImage);

        dateText.setText(checkDate(entry));
        contentText.setText(entry.getNote());
        loadEntryImage(entry, entryImage);

        entryView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JournalActivity.class);
            intent.putExtra("entry_id", entry.getId());
            startActivity(intent);
        });

        return entryView;
    }

    private void loadEntryImage(JournalEntry entry, ImageView entryImage) {
        String imagePath = entry.getImagePath();

        if (JournalDataManager.isDemoImage(imagePath)) {
            switch (imagePath) {
                case JournalDataManager.DEMO_IMAGE_FAMILY:
                    entryImage.setImageResource(R.drawable.family_sunset);
                    break;
                case JournalDataManager.DEMO_IMAGE_MEDITATION:
                    entryImage.setImageResource(R.drawable.meditation_sunrise);
                    break;
                case JournalDataManager.DEMO_IMAGE_LIGHTBULB:
                    entryImage.setImageResource(R.drawable.lightbulb);
                    break;
            }
            entryImage.setVisibility(View.VISIBLE);
        } else if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .into(entryImage);
            entryImage.setVisibility(View.VISIBLE);
        } else {
            entryImage.setVisibility(View.GONE);
        }
    }


    private String checkDate(JournalEntry entry) {
        Date entryDate = entry.getDate();
        Calendar entryCal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        String dateDisplay;

        entryCal.setTime(entryDate);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (isSameDay(entryCal, today)) {
            dateDisplay = "Today, " + entry.getFormattedTime();
        } else if (isSameDay(entryCal, yesterday)) {
            dateDisplay = "Yesterday, " + entry.getFormattedTime();
        } else {
            dateDisplay = entry.getShortDate();
        }

        return dateDisplay;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}