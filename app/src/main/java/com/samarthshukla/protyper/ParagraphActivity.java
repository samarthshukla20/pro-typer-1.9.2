package com.samarthshukla.protyper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ParagraphActivity extends AppCompatActivity {
    private TextView wordDisplay, timerText, scoreText, tvDateTime;
    private EditText inputField;
    private List<String> words = new ArrayList<>();
    private List<String> paragraphs = new ArrayList<>();
    private Random random = new Random();
    private int accuracy = 0;
    private CountDownTimer timer;
    private static final int TIME_LIMIT = 2000; // 120 seconds per game
    private List<String> usedWords;
    private InterstitialAd interstitialAd;
    private long gameStartTime; // timestamp when game starts
    private long startTime; // Stores the game start time
    private String currentParagraph = ""; // to hold original paragraph for accurate comparison
    private boolean isGameOver = false;
    private boolean isParagraphFullyTyped = false;
    private boolean extraTimeGiven = false;
    private RewardedAd rewardedAd;
    private boolean hasShownRewardDialog = false;
    private boolean historySaved = false;
    private long totalPausedDuration = 0;     // total ad/dialog wait time
    private long pauseStartTime = 0;          // when ad/dialog started


    private String getCurrentDateTime() {
        String currentDateTime = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
                .format(new Date());
        if (tvDateTime != null) {
            tvDateTime.setText(currentDateTime);
        }
        return currentDateTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using adjustResize for better keyboard handling with KeyboardVisibilityEvent
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_paragraph_mode);

        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {});
        loadInterstitialAd();
        loadRewardedAd();

        // Initialize UI elements (IDs as per your XML)
        wordDisplay = findViewById(R.id.wordDisplay);
        timerText   = findViewById(R.id.timerText);
        scoreText   = findViewById(R.id.scoreText);
        inputField  = findViewById(R.id.inputField);
        tvDateTime  = findViewById(R.id.tvDateTime);
        startTime = System.currentTimeMillis();
        getCurrentDateTime();

        // Apply custom font to wordDisplay
        wordDisplay.setTypeface(ResourcesCompat.getFont(this, R.font.difficulty));

        loadWordsFromAssets();
        startNewGame();

        inputField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                checkWord();
            }
        });

        // Use KeyboardVisibilityEvent for a robust keyboard listener on lower APIs
        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    View wordCard = findViewById(R.id.wordCard);
                    View textInputLayout = findViewById(R.id.textInputLayout);
                    // Get the root view to measure visible area
                    View rootView = findViewById(android.R.id.content);
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = rootView.getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    if (isOpen && keypadHeight > screenHeight * 0.15) {
                        // Keyboard is open—calculate translation adjustments
                        int availableHeight = screenHeight - keypadHeight;
                        int[] inputLocation = new int[2];
                        textInputLayout.getLocationOnScreen(inputLocation);
                        int inputBottom = inputLocation[1] + textInputLayout.getHeight();
                        int overlap = inputBottom - availableHeight;
                        if (overlap < 0) overlap = 0;

                        int[] wordLocation = new int[2];
                        wordCard.getLocationOnScreen(wordLocation);
                        int wordBottom = wordLocation[1] + wordCard.getHeight();
                        int currentGap = inputLocation[1] - wordBottom;

                        int minGap = dpToPx(8);
                        int extraGapReduction = currentGap - minGap;
                        if (extraGapReduction < 0) extraGapReduction = 0;

                        int translationForInput = -overlap;
                        int translationForWord = -Math.min(overlap, extraGapReduction);

                        textInputLayout.animate().translationY(translationForInput).setDuration(100).start();
                        wordCard.animate().translationY(translationForWord).setDuration(100).start();
                    } else {
                        // Keyboard is closed—reset positions
                        textInputLayout.animate().translationY(0).setDuration(100).start();
                        wordCard.animate().translationY(0).setDuration(100).start();
                    }
                }
        );
    }

    // Helper: Convert dp to pixels.
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadParagraphsFromAssets(String filename) {
        try {
            InputStream is = getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            String fullText = builder.toString();

            String[] parts = fullText.split("(?m)^\\s*\\d+\\.\\s*");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    paragraphs.add(trimmed);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load words from assets.
    private void loadWordsFromAssets() {
        // Assuming your words are stored in "pg.txt" in the assets folder.
        loadParagraphsFromAssets("pg.txt");
        // Copy loaded paragraphs to the words list.
        words.clear();
        words.addAll(paragraphs);
    }

    private void startNewGame() {
        isGameOver = false;
        accuracy = 0;
        scoreText.setText("Accuracy: " + calculateAccuracy());
        inputField.setText("");
        inputField.setEnabled(true);
        usedWords = new ArrayList<>();
        generateNewWord();
        gameStartTime = System.currentTimeMillis();
        startTimer();
        hasShownRewardDialog = false;
        historySaved = false;
    }


    private void generateNewWord() {
        String newWord;
        do {
            newWord = words.get(random.nextInt(words.size()));
        } while (usedWords.contains(newWord));
        usedWords.add(newWord);
        wordDisplay.setText(newWord);
        currentParagraph = newWord; // store plain paragraph for logic
    }


    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(TIME_LIMIT, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                timerText.setText(String.format(Locale.getDefault(), "Time left: %d:%02d", minutes, seconds));
            }
            public void onFinish() {
                gameOver();
            }
        }.start();
    }

    private void checkWord() {
        String typedText = inputField.getText().toString();
        String paragraphText = wordDisplay.getText().toString();

        SpannableStringBuilder spannable = new SpannableStringBuilder();
        int minLength = Math.min(typedText.length(), paragraphText.length());

        int dullGreen = Color.parseColor("#228B22"); // Darker green for correct characters

        for (int i = 0; i < minLength; i++) {
            char typedChar = typedText.charAt(i);
            char correctChar = paragraphText.charAt(i);
            SpannableString spanChar = new SpannableString(String.valueOf(correctChar));

            if (typedChar == correctChar) {
                spanChar.setSpan(new ForegroundColorSpan(dullGreen), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spanChar.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            spannable.append(spanChar);
        }

        if (typedText.length() < paragraphText.length()) {
            spannable.append(paragraphText.substring(typedText.length()));
            isParagraphFullyTyped = false; // Not fully typed
        } else if (typedText.equals(paragraphText)) {
            isParagraphFullyTyped = true; // Exactly matches
        }

        wordDisplay.setText(spannable);

        // ✅ If paragraph is fully typed
        if (typedText.length() == paragraphText.length()) {
            inputField.setEnabled(false);

            if (timer != null) {
                timer.cancel(); // Stop timer if still running
            }

            accuracy = 100; // Set to 100% since fully correct
            String dateTime = getCurrentDateTime();
            int durationPlayed = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);

            showAdThenGameOver(); // Show interstitial ad and then result
        }
    }

    private void saveGameHistoryOnce() {
        if (!historySaved) {
            accuracy = calculateAccuracy();

            // ⏱️ Corrected duration: subtract paused time
            long rawDuration = System.currentTimeMillis() - gameStartTime;
            long actualDuration = rawDuration - totalPausedDuration;

            int durationPlayed = (int) (actualDuration / 1000);  // in seconds
            String dateTime = getCurrentDateTime();

            HistoryManager.addHistory(this, new GameHistory("Paragraph Mode", durationPlayed, 0, dateTime, accuracy));
            historySaved = true;

            Log.d("GameHistory", "Saved with accuracy: " + accuracy + ", duration: " + durationPlayed + "s");
        }
    }




    private int calculateAccuracy() {
        String typedText = inputField.getText().toString().trim();
        String paragraphText = currentParagraph.trim(); // use original paragraph here

        if (typedText.equals(paragraphText)) {
            return 100;
        }

        String[] typedWords = typedText.split("\\s+");
        String[] originalWords = paragraphText.split("\\s+");

        int correctWords = 0;
        int wordsToCompare = Math.min(typedWords.length, originalWords.length);

        for (int i = 0; i < wordsToCompare; i++) {
            if (typedWords[i].equals(originalWords[i])) {
                correctWords++;
            }
        }

        if (originalWords.length == 0) return 0;

        return (int) ((correctWords / (float) originalWords.length) * 100);
    }



    private void showGameOverCard(int accuracy) {

        saveGameHistoryOnce();

        LayoutInflater inflater = LayoutInflater.from(this);
        final View gameOverView = inflater.inflate(R.layout.game_over_card_para, null);

        TextView gameOverMessage = gameOverView.findViewById(R.id.gameOverMessageInside);
        gameOverMessage.setText("Game Over!");

        TextView accuracyInsideCard = gameOverView.findViewById(R.id.scoreTextInsideCard);
        accuracyInsideCard.setText("Accuracy: " + accuracy + "%");

        MaterialButton retryButton = gameOverView.findViewById(R.id.retryButton);
        MaterialButton menuButton = gameOverView.findViewById(R.id.menuButton);

        retryButton.setOnClickListener(v -> {
            removeGameOverView(gameOverView);
            startNewGame();
        });

        menuButton.setOnClickListener(v -> openMenu());

        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(gameOverView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        String typedText = inputField.getText().toString().trim();
        long endTime = System.currentTimeMillis();
        double timeTakenMinutes = (endTime - startTime) / 60000.0;

        // Count words typed
        int wordCount = 0;
        if (!typedText.trim().isEmpty()) {
            wordCount = typedText.trim().split("\\s+").length;
        }

        // Calculate WPM
        int wpm = timeTakenMinutes > 0 ? (int) (wordCount / timeTakenMinutes) : 0;

        // Show Game Over UI (Assuming there's a TextView for WPM)
        TextView wpmTextView = findViewById(R.id.wpmTextView);
        if (wpmTextView != null) {
            wpmTextView.setText("WPM: " + wpm);
        }

    }


    private void gameOver() {
        if (isGameOver) return;
        isGameOver = true;

        inputField.setEnabled(false);
        accuracy = calculateAccuracy();
        String dateTime = getCurrentDateTime();
        int durationPlayed = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);

        if (!isParagraphFullyTyped && !hasShownRewardDialog) {
            hasShownRewardDialog = true;
            showRewardedAdDialog();  // offer bonus time
        } else {
            showAdThenGameOver();   // go straight to results
        }
    }


    private void showAdThenGameOver() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    showGameOverCard(accuracy); // uses latest accuracy
                    loadInterstitialAd(); // load next ad
                }
            });
        } else {
            showGameOverCard(accuracy); // fallback if no ad
        }
    }


    private void removeGameOverView(View gameOverView) {
        ViewGroup parent = (ViewGroup) gameOverView.getParent();
        if (parent != null) {
            parent.removeView(gameOverView);
        }
    }


    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.Interstitial), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showRewardedAdDialog() {
        final AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rewarded_ad, null);

        TextView message = dialogView.findViewById(R.id.rewardMessage);
        MaterialButton watchAdButton = dialogView.findViewById(R.id.btnWatchAd);
        MaterialButton cancelButton = dialogView.findViewById(R.id.btnCancel);

        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // ⏱️ Start tracking pause time
        pauseStartTime = System.currentTimeMillis();

        dialog.getWindow().setDimAmount(0.9f); // 0 = no dim, 1 = full black
        dialog.show();  // ✅ Show before countdown begins so title updates work

        // Countdown timer logic
        final int[] secondsLeft = {500000000};
        dialog.setTitle("Add +15 sec? (" + secondsLeft[0] + "s)");

        final Handler handler = new Handler();
        final Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                secondsLeft[0]--;
                dialog.setTitle("Add +15 sec? (" + secondsLeft[0] + "s)");
                if (secondsLeft[0] > 0) {
                    handler.postDelayed(this, 1000);
                } else {
                    dialog.dismiss();
                    // ⏱️ Add dialog time to paused duration
                    totalPausedDuration += System.currentTimeMillis() - pauseStartTime;
                    showGameOverCard(accuracy);
                }
            }
        };
        handler.postDelayed(countdownRunnable, 1000);

        // ✅ Watch Ad button
        watchAdButton.setOnClickListener(v -> {
            handler.removeCallbacks(countdownRunnable);
            dialog.dismiss();
            showRewardedAd();  // pauseStartTime continues into ad time
        });

        // ✅ Cancel button
        cancelButton.setOnClickListener(v -> {
            handler.removeCallbacks(countdownRunnable);
            dialog.dismiss();
            totalPausedDuration += System.currentTimeMillis() - pauseStartTime;
            showAdThenGameOver();
        });
    }

    private void resumeGameWith15Seconds() {
        inputField.setEnabled(true);
        isGameOver = false;

        // ✅ Update totalPausedDuration to exclude ad/dialog time from duration calculation
        totalPausedDuration += System.currentTimeMillis() - pauseStartTime;
        pauseStartTime = 0;

        inputField.requestFocus();
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

        startTime = System.currentTimeMillis(); // Keep for WPM calculation only

        if (timer != null) timer.cancel();

        timer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timerText.setText(String.format(Locale.getDefault(), "Extra Time: %d sec", secondsRemaining));
            }

            public void onFinish() {
                gameOver();  // Ends game after extra 15s
            }
        }.start();
    }





    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.Rewarded), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                rewardedAd = null;
            }
        });
    }


    private void showRewardedAd() {
        if (rewardedAd != null) {
            pauseStartTime = System.currentTimeMillis(); // (Optional) If you're tracking paused time

            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;         // Clear reference
                    loadRewardedAd();          // Load next ad
                    resumeGameWith15Seconds(); // Resume after ad is closed
                }

                @Override
                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                    rewardedAd = null;
                    showAdThenGameOver();     // Fallback
                }
            });

            rewardedAd.show(this, rewardItem -> {
                // Reward granted, but we wait until user closes the ad screen
            });

        } else {
            showAdThenGameOver();  // fallback
        }
    }




    private void startTimerWithExtraTime() {
        timer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timerText.setText("Extra Time: " + secondsRemaining + "s");
            }

            public void onFinish() {
                gameOver();
            }
        }.start();
    }


    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Game?")
                .setMessage("Do you want to go back to the menu or continue playing?")
                .setPositiveButton("Go to Menu", (dialog, which) -> finish())
                .setNegativeButton("Continue Playing", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void saveGameHistory(Context context, String mode, int duration, int accuracy) {
        SharedPreferences preferences = context.getSharedPreferences("GameHistory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        String existingHistory = preferences.getString("history", "");
        String newEntry = mode + ", " + duration + "s, " + accuracy + " points, " + currentDateTime + "\n";
        editor.putString("history", existingHistory + newEntry);
        editor.apply();
    }

    private void openMenu() {
        finish();
    }
}