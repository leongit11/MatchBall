package de.hwr.matchball;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AllMatchesActivity extends AppCompatActivity {

    private RecyclerView rvMatches;
    private MatchAdapter adapter;
    private final List<MatchAdapter.MatchListItem> items = new ArrayList<>();
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_matches);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvMatches = findViewById(R.id.rvMatches);

        adapter = new MatchAdapter(items, matchId ->
                Toast.makeText(this, "Clicked Match: " + matchId, Toast.LENGTH_SHORT).show()
        );

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);

        listenForMatches();
    }

    private void listenForMatches() {
        Query query = FirebaseFirestore.getInstance()
                .collection("matches")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        registration = query.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null) return;

            items.clear();

            for (DocumentSnapshot doc : snap.getDocuments()) {
                Match match = doc.toObject(Match.class);
                if (match != null) {
                    items.add(new MatchAdapter.MatchListItem(doc.getId(), match));
                }
            }

            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}
