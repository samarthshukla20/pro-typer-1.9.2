package com.samarthshukla.protyper;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WordLoader {

    public static List<String> loadWords(Context context) {
        List<String> words = new ArrayList<>();
        try {
            for (char letter = 'A'; letter <= 'Z'; letter++) {
                String fileName = letter + " Words.txt";  // File name is 'A Words.txt', 'B Words.txt', etc.
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
                String line;
                while ((line = reader.readLine()) != null) {
                    words.add(line);  // Add each word from the file
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions (file read errors)
        }
        return words;
    }
}