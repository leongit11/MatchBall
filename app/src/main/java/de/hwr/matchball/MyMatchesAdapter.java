package de.hwr.matchball;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


 // Adapter für "My Matches" (Created & Joined). Zeigt Match-Infos + zwei Buttons: Cancel/Leave und Details.

public class MyMatchesAdapter extends RecyclerView.Adapter<MyMatchesAdapter.VH> {

    public interface OnActionClick {
        void onDetails(String matchId);
        void onPrimaryAction(String matchId);// Cancel oder Leave (je nach Liste)
    }

    public static class MyMatchItem {
        public final String matchId;
        public final Match match;
        public final boolean isCreatedByMe;

        public MyMatchItem(String matchId, Match match, boolean isCreatedByMe) {
            this.matchId = matchId;
            this.match = match;
            this.isCreatedByMe = isCreatedByMe;
        }
    }

    private final List<MyMatchItem> items;
    private final OnActionClick listener;

    public MyMatchesAdapter(List<MyMatchItem> items, OnActionClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_match, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MyMatchItem item = items.get(position);
        Match m = item.match;

        // Textfelder
        h.tvTitle.setText(m.title != null ? m.title : "-");
        h.tvLocation.setText(m.location != null ? m.location : "-");

        String date = m.date != null ? m.date : "-";
        String time = m.time != null ? m.time : "-";
        h.tvDateTime.setText(date + " • " + time);

        // Buttons
        h.btnDetails.setOnClickListener(v -> listener.onDetails(item.matchId));

        // Primary Action: Created => Cancel, Joined => Leave
        h.btnPrimary.setText(item.isCreatedByMe ? "Cancel" : "Leave");
        h.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(item.matchId));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvDateTime;
        Button btnPrimary, btnDetails;

        VH(@NonNull View itemView) {
            super(itemView);

            // Diese IDs müssen in item_my_match.xml existieren:
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvLocation = itemView.findViewById(R.id.tvItemLocation);
            tvDateTime = itemView.findViewById(R.id.tvItemDateTime);

            btnPrimary = itemView.findViewById(R.id.btnItemPrimary);
            btnDetails = itemView.findViewById(R.id.btnItemDetails);
        }
    }

}