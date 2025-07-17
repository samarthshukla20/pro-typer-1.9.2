package com.samarthshukla.protyper;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryManager {
    private static final String PREFS_NAME = "GameHistoryPrefs";
    private static final String KEY_HISTORY_LIST = "history_list";
    private static final int MAX_HISTORY_SIZE = 20;
    private static final Gson gson = new Gson();

    // Retrieve the saved list of histories (or an empty list if none)
    public static List<GameHistory> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY_LIST, "");
        if (!json.isEmpty()) {
            Type listType = new TypeToken<List<GameHistory>>() {}.getType();
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    // Save the entire history list to SharedPreferences
    public static void saveHistory(Context context, List<GameHistory> historyList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(historyList);
        editor.putString(KEY_HISTORY_LIST, json);
        editor.apply();
    }

    // Add a new history record with timestamp
    public static void addHistory(Context context, GameHistory newHistory) {
        List<GameHistory> historyList = getHistory(context);

        // Set timestamp if not already set
        if (newHistory.getDateTime() == null) {
            newHistory.setDateTime(getCurrentTimestamp());
        }

        historyList.add(0, newHistory); // Add new entry at the top

        // Keep history size within the limit
        if (historyList.size() > MAX_HISTORY_SIZE) {
            historyList.remove(historyList.size() - 1);
        }

        saveHistory(context, historyList);
    }

    // Delete all history records
    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY_LIST).apply();
    }

    // Get the current date and time
    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }
}