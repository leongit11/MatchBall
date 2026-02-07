package de.hwr.matchball;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter für die Teilnehmerliste.
// Zeigt aktuell nur einfache Strings (UIDs), ohne User-Logik oder DB-Abfragen.
public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.VH> {

    // Liste der Teilnehmer (z.B. UIDs aus Firestore)
    private final List<String> items;

    // Übergabe der Teilnehmerliste von außen (Activity)
    public ParticipantsAdapter(List<String> items) {
        this.items = items;
    }

    // Erstellt eine einzelne Zeile für die RecyclerView
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // Android-Standardlayout mit einem TextView
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }

    // Befüllt eine Zeile mit dem passenden Teilnehmer
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.text1.setText(items.get(position));
    }

    // Anzahl der Einträge in der Liste
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder hält Referenzen auf die Views einer einzelnen Zeile
    static class VH extends RecyclerView.ViewHolder {
        TextView text1;

        VH(@NonNull View itemView) {
            super(itemView);
            // Das TextView aus simple_list_item_1
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }
}