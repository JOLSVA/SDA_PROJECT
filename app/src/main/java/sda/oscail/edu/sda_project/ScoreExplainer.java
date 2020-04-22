package sda.oscail.edu.sda_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class ScoreExplainer extends AppCompatActivity {

    private TextView completionFactorText, goalCountFactorText, typeDiversityFactorText,
            perfomanceScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_explainer);

        //set the toolbar we have overridden
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //adds a home button to the toolbar.
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //to receive score details from the Welcome Fragment
        final String completionFactor = Objects.requireNonNull(getIntent().getExtras().getString("COMPLETION_FACTOR"));
        final String goalCountFactor = Objects.requireNonNull(getIntent().getExtras().getString("GOAL_COUNT_FACTOR"));
        final String typeDiversityFactor = Objects.requireNonNull(getIntent().getExtras().getString("TYPE_DIVERSITY_FACTOR"));
        final String performanceScore = Objects.requireNonNull(getIntent().getExtras().getString("PERFORMANCE_SCORE"));

        completionFactorText = findViewById(R.id.completionFactor);
        goalCountFactorText = findViewById(R.id.goalCountFactor);
        typeDiversityFactorText = findViewById(R.id.typeDiversityFactor);
        perfomanceScoreText = findViewById(R.id.scoreSummary);

        completionFactorText.setText("Your current completion factor is: " + completionFactor);
        goalCountFactorText.setText("Your current goal count factor is: " + goalCountFactor);
        typeDiversityFactorText.setText("Your current type diversity factor is:" + typeDiversityFactor);
        perfomanceScoreText.setText("YOUR PERFORMANCE SCORE IS: " + "\n" + performanceScore);
    }
}
