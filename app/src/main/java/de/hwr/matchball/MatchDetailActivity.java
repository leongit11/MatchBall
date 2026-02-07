package de.hwr.matchball;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;


 // Detailansicht eines einzelnen Matches.Zeigt Match-Infos und ermöglicht Join / Leave.

public class MatchDetailActivity extends AppCompatActivity {

    // UI-Elemente aus dem XML
    private TextView tvMatchTitle, tvMatchDateTime, tvMatchLocation, tvCreatedBy;
    private Button btnJoin, tvBackAllMatches;
    private RecyclerView rvParticipants;

    // Teilnehmerliste (Strings, aktuell UID / später Mail)
    private final List<String> participantItems = new ArrayList<>();
    private ParticipantsAdapter participantsAdapter;

    // Aktuell eingeloggter User und Match-ID
    private FirebaseUser user;
    private String matchId;

    // Firestore Listener für Live-Updates
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_match_detail);

        // Insets, damit UI nicht unter Status-/Navigation-Bar liegt
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Aktuellen User holen (ohne Login kein Zugriff)
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Match-ID kommt aus AllMatchesActivity per Intent
        matchId = getIntent().getStringExtra("matchId");
        if (matchId == null || matchId.trim().isEmpty()) {
            Toast.makeText(this, "matchId fehlt.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Views initialisieren
        bindViews();
        setupParticipantsList();

        // Zurück zur Match-Übersicht
        tvBackAllMatches.setOnClickListener(v -> finish());

        // Match-Daten aus Firestore laden (inkl. Live-Updates)
        listenToMatch();
    }

    // Verbindet alle Views mit dem XML
    private void bindViews() {
        tvBackAllMatches = findViewById(R.id.tvBackAllMatches);
        tvMatchTitle = findViewById(R.id.tvMatchTitle);
        tvMatchDateTime = findViewById(R.id.tvMatchDateTime);
        tvMatchLocation = findViewById(R.id.tvMatchLocation);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        rvParticipants = findViewById(R.id.rvParticipants);
        btnJoin = findViewById(R.id.btnJoin);
    }

    // Initialisiert RecyclerView für Teilnehmer
    private void setupParticipantsList() {
        participantsAdapter = new ParticipantsAdapter(participantItems);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        rvParticipants.setAdapter(participantsAdapter);
    }

    // Lauscht auf Änderungen am Match-Dokument in Firestore
    private void listenToMatch() {
        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("matches")
                .document(matchId);

        registration = ref.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null || !snap.exists()) {
                Toast.makeText(this, "Match nicht gefunden.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Match match = snap.toObject(Match.class);
            if (match == null) return;

            // UI mit aktuellen Match-Daten füllen
            renderMatch(match);

            // Join / Leave Button passend setzen
            wireJoinLeave(ref, match);
        });
    }

    // Setzt alle Match-Daten in die UI
    private void renderMatch(Match match) {
        tvMatchTitle.setText(match.title != null ? match.title : "-");

        String date = match.date != null ? match.date : "-";
        String time = match.time != null ? match.time : "-";
        tvMatchDateTime.setText(date + " • " + time);

        tvMatchLocation.setText(match.location != null ? match.location : "-");

        // Host anzeigen (UID oder eigene Mail)
        String hostLabel;
        if (match.createdByUid != null && match.createdByUid.equals(user.getUid())) {
            hostLabel = user.getEmail() != null ? user.getEmail() : user.getUid();
        } else {
            hostLabel = match.createdByUid != null ? match.createdByUid : "-";
        }
        tvCreatedBy.setText("Created by: " + hostLabel);

        participantItems.clear();
        if (match.participantUserIds != null) {
            participantItems.addAll(match.participantUserIds);
        }
        participantsAdapter.notifyDataSetChanged();
    }

    // Join- oder Leave-Logik für den aktuellen User
    private void wireJoinLeave(DocumentReference ref, Match match) {
        String uid = user.getUid();

        boolean isCancelled = match.status != null && match.status.equalsIgnoreCase("cancelled");
        boolean isParticipant = match.participantUserIds != null && match.participantUserIds.contains(uid);

        if (isCancelled) {
            btnJoin.setEnabled(false);
            btnJoin.setText("Cancelled");
            return;
        }

        btnJoin.setEnabled(true);
        btnJoin.setText(isParticipant ? "Leave" : "Join");

        btnJoin.setOnClickListener(v -> {
            if (isParticipant) {
                // User verlässt das Match
                ref.update("participantUserIds", FieldValue.arrayRemove(uid))
                        .addOnFailureListener(err ->
                                Toast.makeText(this, "Leave fehlgeschlagen: " + err.getMessage(), Toast.LENGTH_LONG).show()
                        );
            } else {
                // User tritt dem Match bei
                ref.update("participantUserIds", FieldValue.arrayUnion(uid))
                        .addOnFailureListener(err ->
                                Toast.makeText(this, "Join fehlgeschlagen: " + err.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}