package de.hwr.matchball;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

public class Match {

    public String title;
    public String date;
    public String time;
    public String location;
    public int minPlayers;
    public String notes;
    public String createdByUid;

    @ServerTimestamp
    public Timestamp createdAt;

    // Firestore braucht einen leeren Konstruktor um es zu speichern
    public Match() {}

    public Match(String title, String date, String time, String location,
                 int minPlayers, String notes, String createdByUid) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.minPlayers = minPlayers;
        this.notes = notes;
        this.createdByUid = createdByUid;
        this.createdAt = null; // Firestore setzt das serverseitig
    }
}
