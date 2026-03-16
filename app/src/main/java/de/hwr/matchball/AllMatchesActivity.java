package de.hwr.matchball;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllMatchesActivity extends AppCompatActivity {

    private RecyclerView rvMatches;
    private MatchAdapter adapter;

    // alle Matches aus Firestore
    private final List<MatchAdapter.MatchListItem> allItems = new ArrayList<>();
    // das, was gerade angezeigt wird
    private final List<MatchAdapter.MatchListItem> shownItems = new ArrayList<>();

    private ListenerRegistration registration;

    private enum FilterMode { ALL, TODAY, SEEKING }
    private FilterMode filterMode = FilterMode.ALL;

    // Prototyp-Definition "noch suchend": minPlayers ab 10
    private static final int SEEKING_MINPLAYERS_THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_matches);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvMatches = findViewById(R.id.rvMatches);

        // Adapter MUSS shownItems benutzen, sonst sieht man Filter nicht
        adapter = new MatchAdapter(shownItems, matchId -> {
            Intent i = new Intent(AllMatchesActivity.this, MatchDetailActivity.class);
            i.putExtra("matchId", matchId);
            startActivity(i);
        });

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);

        // Buttons
        Button btnToday = findViewById(R.id.btnToday);
        Button btnFreePlayer = findViewById(R.id.btnFreePlayer);

        btnToday.setOnClickListener(v -> {
            // Toggle: wenn schon TODAY, dann wieder ALL
            filterMode = (filterMode == FilterMode.TODAY) ? FilterMode.ALL : FilterMode.TODAY;
            applyFilter();
        });

        btnFreePlayer.setOnClickListener(v -> {
            // Toggle: wenn schon SEEKING, dann wieder ALL
            filterMode = (filterMode == FilterMode.SEEKING) ? FilterMode.ALL : FilterMode.SEEKING;
            applyFilter();
        });

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

            allItems.clear();

            for (DocumentSnapshot doc : snap.getDocuments()) {
                Match match = doc.toObject(Match.class);
                if (match != null) {
                    allItems.add(new MatchAdapter.MatchListItem(doc.getId(), match));
                }
            }

            applyFilter(); // wichtig: nach neuem Laden wieder filtern
        });
    }

    private void applyFilter() {
        shownItems.clear();

        for (MatchAdapter.MatchListItem item : allItems) {
            if (item.match == null) continue;

            if (filterMode == FilterMode.ALL) {
                shownItems.add(item);
            } else if (filterMode == FilterMode.TODAY) {
                Date d = parseGermanDate(item.match.date);
                if (isToday(d)) shownItems.add(item);
            } else if (filterMode == FilterMode.SEEKING) {
                // Prototyp: "noch suchend" = minPlayers ist hoch (z.B. 10+)
                if (item.match.minPlayers >= SEEKING_MINPLAYERS_THRESHOLD) {
                    shownItems.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // akzeptiert "14.02.2026" und auch "14.2.2026"
    private Date parseGermanDate(String input) {
        if (input == null) return null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("d.M.yyyy", Locale.GERMANY);
            parser.setLenient(false);
            return parser.parse(input.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isToday(Date d) {
        if (d == null) return false;

        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        return now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}