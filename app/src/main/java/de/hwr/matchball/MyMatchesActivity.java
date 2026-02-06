package de.hwr.matchball;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyMatchesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);


        findViewById(R.id.btnBackAllMatches).setOnClickListener(v -> finish());

        RecyclerView rvCreated = findViewById(R.id.rvCreatedMatches);
        RecyclerView rvJoined  = findViewById(R.id.rvJoinedMatches);

        rvCreated.setLayoutManager(new LinearLayoutManager(this));
        rvJoined.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data
        List<Match> created = new ArrayList<>();
        created.add(new Match("Created Game 1", "2026-02-06", "18:00", "Berlin", 10, "", "me"));
        created.add(new Match("Created Game 2", "2026-02-07", "20:00", "Hamburg", 8, "", "me"));

        List<Match> joined = new ArrayList<>();
        joined.add(new Match("Joined Game 1", "2026-02-08", "19:30", "Köln", 12, "", "other"));

        // Noch kein Adapter, Liste bleibt leer

    }
}