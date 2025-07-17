package com.samarthshukla.protyper;

public class GameHistory {
    private String mode;
    private int duration;
    private int score;
    private String dateTime; // This replaces timestamp

    private int accuracy;

    public GameHistory(String mode, int duration, int score, String dateTime, int accuracy) {
        this.mode = mode;
        this.duration = duration;
        this.score = score;
        this.dateTime = dateTime;
        this.accuracy = accuracy;
    }

    public String getMode() {
        return mode;
    }

    public int getDuration() {
        return duration;
    }

    public int getScore() {
        return score;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getAccuracy(){return accuracy;}

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}