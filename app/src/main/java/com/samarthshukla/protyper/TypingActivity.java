package com.samarthshukla.protyper;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

import com.samarthshukla.protyper.R;

public class TypingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing);

        // Retrieve difficulty from intent
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "Not Selected";
        }

        // Show toast (for testing)
        Toast.makeText(this, "Difficulty: " + difficulty, Toast.LENGTH_SHORT).show();

        // Simulating a game session
        int duration = 10; // Example duration
        int score = 50;    // Example score

        // Save game history
        saveGameHistory(difficulty, duration, score);

        // Debug: Check if history is saved
        SharedPreferences preferences = getSharedPreferences("GameHistory", MODE_PRIVATE);
        String history = preferences.getString("history", "No history found");
        Toast.makeText(this, "Saved History: " + history, Toast.LENGTH_LONG).show();
    }
    
    private void saveGameHistory(String mode, int duration, int score) {
        SharedPreferences preferences = getSharedPreferences("GameHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Retrieve existing history
        String existingHistory = preferences.getString("history", "");

        // Add new entry
        String newEntry = mode + "," + duration + "s," + score + "\n";

        // Save updated history
        editor.putString("history", existingHistory + newEntry);
        editor.apply();

        // Debug: Confirm history is saved
        Toast.makeText(this, "History Saved!", Toast.LENGTH_SHORT).show();
    }
}