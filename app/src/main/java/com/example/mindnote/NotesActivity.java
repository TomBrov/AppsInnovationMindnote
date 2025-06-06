package com.example.mindnote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout emptyStateContainer;
    private Button addNoteButton;
    private JournalDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Initialize views
        initViews();
        dataManager = JournalDataManager.getInstance(this);
        recyclerView();

        // Set up add note button in empty state
        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, JournalActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
        updateUI();
    }

    private void initViews(){
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        addNoteButton = findViewById(R.id.addNoteButton);
    }

    private void recyclerView(){
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(new ArrayList<>(), this);
        notesRecyclerView.setAdapter(notesAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        dataManager.loadEntriesFromFirestore(entries -> {
            notesAdapter.updateEntries(entries);

            if (entries.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                notesRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
                notesRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupBottomNavigation() {
        // Set Notes as selected item
        bottomNavigationView.setSelectedItemId(R.id.navigation_notes);

        // Set up bottom navigation listener
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_journal) {
                            Intent intent = new Intent(NotesActivity.this, JournalActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (itemId == R.id.navigation_notes) {
                            // Already on notes
                            return true;
                        } else if (itemId == R.id.navigation_home) {
                            Intent intent = new Intent(NotesActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        } else
                            if (itemId == R.id.navigation_calendar) {
                            Intent intent = new Intent(NotesActivity.this, CalendarActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        } else return itemId == R.id.navigation_profile;
                    }
                });
    }

    @Override
    public void onNoteClick(JournalEntry entry) {
        Intent intent = new Intent(NotesActivity.this, JournalActivity.class);
        intent.putExtra("entry_id", entry.getId());
        startActivity(intent);
    }
}