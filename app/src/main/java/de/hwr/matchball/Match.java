package de.hwr.matchball;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;


public class Match {

    public String title;
    public String date;
    public String time;
    public String location;

    public int minPlayers;
    public String notes;
    public String createdByUid;

    public List<String> participantUserIds;// UIDs
    public List<String> participantEmails; //Speichert zusätzlich Email in Match zum anwendungszweck
    public String status;                     // open, cancelled, completed
    //public Timestamp startAt;                 // startzeit prüfen



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
        //this.startAt = startAt;


        this.status = "open";
        this.participantUserIds = new ArrayList<>();
        this.participantEmails = new ArrayList<>(); //danke
        //this.participantUserIds.add(createdByUid);
        this.createdAt = null; // Firestore setzt das serverseitig
    }
}
