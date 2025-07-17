package com.samarthshukla.protyper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.samarthshukla.protyper.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<GameHistory> historyList;

    public HistoryAdapter(List<GameHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        com.samarthshukla.protyper.GameHistory history = historyList.get(position);
        holder.txtMode.setText("Mode: " + history.getMode());
        holder.txtDuration.setText("Duration: " + history.getDuration() + " s");
        holder.txtDateTime.setText("Date: " + history.getDateTime());
        if (history.getMode().equals("Paragraph Mode")){
        holder.txtScore.setText("Accuracy: " + history.getAccuracy() + "%");
        }
        else {
            holder.txtScore.setText("Score: " + history.getScore());
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtMode, txtDuration, txtScore, txtDateTime;
        MaterialCardView cardView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            txtMode = itemView.findViewById(R.id.tvMode);
            txtDuration = itemView.findViewById(R.id.tvDuration);
            txtScore = itemView.findViewById(R.id.tvScore);
            txtDateTime = itemView.findViewById(R.id.tvDateTime);
        }
    }
}