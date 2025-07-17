package com.samarthshukla.protyper;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.widget.ImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.samarthshukla.protyper.R;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private List<GameHistory> historyList;
    private ImageView btnBack, btnDeleteHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerViewHistory);
        btnBack = findViewById(R.id.btnBack);
        btnDeleteHistory = findViewById(R.id.btnDeleteHistory);

        // Load history from SharedPreferences or database
        historyList = com.samarthshukla.protyper.HistoryManager.getHistory(this);

        // Ensure history contains valid date-time
        for (GameHistory history : historyList) {
            if (history.getDateTime() == null || history.getDateTime().isEmpty()) {
                history.setDateTime("Unknown Date"); // Fallback value
            }
        }

        // Setup RecyclerView
        historyAdapter = new HistoryAdapter(historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyAdapter);

        // Back Button Click
        btnBack.setOnClickListener(v -> finish());

        // Delete History Button Click
        btnDeleteHistory.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Delete History")
                .setMessage("Are you sure you want to clear all history?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    HistoryManager.clearHistory(this);
                    historyList.clear();
                    historyAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .show();
        });
    }
}