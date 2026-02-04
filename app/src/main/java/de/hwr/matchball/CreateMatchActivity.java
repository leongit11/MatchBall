package de.hwr.matchball;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class CreateMatchActivity extends AppCompatActivity {

    private EditText Title, Date, Time, Location, MinPlayers, Notes;
    private Button btnCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_match);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Title = findViewById(R.id.Title);
        Date = findViewById(R.id.Date);
        Time = findViewById(R.id.Time);
        Location = findViewById(R.id.Location);
        MinPlayers = findViewById(R.id.MinPlayers);
        Notes = findViewById(R.id.Notes);

        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> saveMatch());
    }

    private void saveMatch() {
        String title = Title.getText().toString().trim();
        String date = Date.getText().toString().trim();
        String time = Time.getText().toString().trim();
        String location = Location.getText().toString().trim();
        String minPlayersStr = MinPlayers.getText().toString().trim();
        String notes = Notes.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty() || minPlayersStr.isEmpty()) {
            Toast.makeText(this, "Bitte alle Pflichtfelder ausfüllen.", Toast.LENGTH_SHORT).show();
            return;
        }

        int minPlayers;
        try {
            minPlayers = Integer.parseInt(minPlayersStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "MinPlayers muss eine Zahl sein.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bitte zuerst einloggen.", Toast.LENGTH_SHORT).show();
            return;
        }

        Match match = new Match(title, date, time, location, minPlayers, notes, user.getUid());

        FirebaseFirestore.getInstance()
                .collection("matches")
                .add(match)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Match erstellt!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}