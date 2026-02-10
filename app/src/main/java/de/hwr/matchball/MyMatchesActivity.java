package de.hwr.matchball;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

// Zeigt "Meine Matches" getrennt nach Created (von mir erstellt) und Joined (ich bin Teilnehmer).
public class MyMatchesActivity extends AppCompatActivity {

    private RecyclerView rvCreated, rvJoined;
    private Button btnBackAll;

    private MyMatchesAdapter createdAdapter, joinedAdapter;

    // Wichtig: MyMatchesAdapter erwartet List<MyMatchesAdapter.MyMatchItem>
    private final List<MyMatchesAdapter.MyMatchItem> createdItems = new ArrayList<>();
    private final List<MyMatchesAdapter.MyMatchItem> joinedItems = new ArrayList<>();

    // Firestore Listener für Live-Updates
    private ListenerRegistration regCreated;
    private ListenerRegistration regJoined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_matches);

        // Login-Check (ohne User keine "My Matches")
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // In DB steht UID, nicht Email
        String myUid = user.getUid();

        // Views
        rvCreated = findViewById(R.id.rvCreatedMatches);
        rvJoined = findViewById(R.id.rvJoinedMatches);
        btnBackAll = findViewById(R.id.btnBackAllMatches);

        btnBackAll.setOnClickListener(v -> finish());

        // Adapter: Created Matches (Primary = Cancel/Delete)
        createdAdapter = new MyMatchesAdapter(createdItems, new MyMatchesAdapter.OnActionClick() {
            @Override
            public void onDetails(String matchId) {
                Intent i = new Intent(MyMatchesActivity.this, MatchDetailActivity.class);
                i.putExtra("matchId", matchId);
                startActivity(i);
            }

            @Override
            public void onPrimaryAction(String matchId) {
                // Cancel = Match komplett löschen
                FirebaseFirestore.getInstance()
                        .collection("matches")
                        .document(matchId)
                        .delete()
                        .addOnFailureListener(e ->
                                Toast.makeText(MyMatchesActivity.this,
                                        "Cancel fehlgeschlagen: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
            }
        });

        rvCreated.setLayoutManager(new LinearLayoutManager(this));
        rvCreated.setAdapter(createdAdapter);

        // Adapter: Joined Matches (Primary = Leave)
        joinedAdapter = new MyMatchesAdapter(joinedItems, new MyMatchesAdapter.OnActionClick() {
            @Override
            public void onDetails(String matchId) {
                Intent i = new Intent(MyMatchesActivity.this, MatchDetailActivity.class);
                i.putExtra("matchId", matchId);
                startActivity(i);
            }

            @Override
            public void onPrimaryAction(String matchId) {
                // Leave = eigene UID aus participantUserIds entfernen
                FirebaseFirestore.getInstance()
                        .collection("matches")
                        .document(matchId)
                        .update("participantUserIds", FieldValue.arrayRemove(myUid))
                        .addOnFailureListener(e ->
                                Toast.makeText(MyMatchesActivity.this,
                                        "Leave fehlgeschlagen: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
            }
        });

        rvJoined.setLayoutManager(new LinearLayoutManager(this));
        rvJoined.setAdapter(joinedAdapter);

        // Live-Listen starten
        listenCreated(myUid);
        listenJoined(myUid);
    }

    // Holt alle Matches, die erstellt wurden (createdByUid == UID)
    private void listenCreated(String myUid) {
        Query q = FirebaseFirestore.getInstance()
                .collection("matches")
                .whereEqualTo("createdByUid", myUid);
                //.orderBy("createdAt", Query.Direction.DESCENDING);

        regCreated = q.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Toast.makeText(this, "Fehler (Created): " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null) return;

            createdItems.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Match m = doc.toObject(Match.class);
                if (m != null) {
                    createdItems.add(new MyMatchesAdapter.MyMatchItem(doc.getId(), m, true));
                }
            }
            createdAdapter.notifyDataSetChanged();
        });
    }

    // Holt alle Matches, bei denen ich Teilnehmer bin (participantUserIds enthält meine UID)
    private void listenJoined(String myUid) {
        Query q = FirebaseFirestore.getInstance()
                .collection("matches")
                .whereArrayContains("participantUserIds", myUid);
                //.orderBy("createdAt", Query.Direction.DESCENDING);

        regJoined = q.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Toast.makeText(this, "Fehler (Joined): " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null) return;

            joinedItems.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Match m = doc.toObject(Match.class);
                if (m != null) {
                    joinedItems.add(new MyMatchesAdapter.MyMatchItem(doc.getId(), m, false));
                }
            }
            joinedAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (regCreated != null) regCreated.remove();
        if (regJoined != null) regJoined.remove();
    }
}