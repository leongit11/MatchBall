package de.hwr.matchball;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnItemClickListener {
        void onClick(String matchId);
    }

    public static class MatchListItem {
    public String id;
    public Match match;

    public  MatchListItem(String id, Match match){
        this.id = id;
        this.match = match;
    }
    }

    private final List<MatchListItem> items;
    private final OnItemClickListener listener;

    public MatchAdapter(List<MatchListItem> items, OnItemClickListener listener){
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchListItem item = items.get(position);
        Match m = item.match;

        holder.tvTitle.setText(m.title != null ? m.title : "");
        holder.tvLocation.setText("Ort: " + (m.location != null ? m.location : ""));
        holder.tvDateTime.setText("Wann: " + (m.date != null ? m.date : "") + "  " + (m.time != null ? m.time : ""));
        holder.tvMinPlayers.setText("Min Spieler: " + m.minPlayers);

        holder.itemView.setOnClickListener(v -> listener.onClick(item.id));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvDateTime, tvMinPlayers;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitel);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvMinPlayers = itemView.findViewById(R.id.tvMinPlayers);
        }
    }
}

