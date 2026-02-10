package de.hwr.matchball;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Einfache Notes-Seite: speichert einen Text pro User in Firestore (user_notes/{uid}).
public class NotesActivity extends AppCompatActivity {

    private Button btnHome, btnSave;
    private EditText etNotes;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        btnHome = findViewById(R.id.btnHome);
        btnSave = findViewById(R.id.btnSave);
        etNotes = findViewById(R.id.etNotes);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Zurück
        btnHome.setOnClickListener(v -> finish());

        // Beim Start vorhandene Notes laden
        loadNotes();

        // Speichern
        btnSave.setOnClickListener(v -> saveNotes());
    }

    private void loadNotes() {
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("user_notes")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String text = doc.getString("text");
                        etNotes.setText(text != null ? text : "");
                    } else {
                        etNotes.setText("");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void saveNotes() {
        String uid = user.getUid();
        String text = etNotes.getText().toString(); // absichtlich nicht trim() – user will evtl. Format/Zeilen behalten

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("user_notes")
                .document(uid)
                .set(data) // überschreibt einfach den Text, genau was du willst
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Gespeichert.", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}