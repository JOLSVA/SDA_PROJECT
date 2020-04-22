package sda.oscail.edu.sda_project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class Welcome extends Fragment {

    public Welcome() {
        // Required empty public constructor
    }

    ImageView imageItem1, imageItem2, imageItem3;
    String imageUri1, imageUri2,imageUri3;

    int todayAllCounter, todayCompletedCounter, fortnightAllCounter, fortnightCompletedCounter,
                fortnightIncompleteCounter, typeCounter,
                completionFactor, goalCountFactor, typeDiversityFactor, performanceScore;

    TextView    todayText, scoreText;

    final ArrayList<String> typeList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        todayText=view.findViewById(R.id.todayText);
        scoreText=view.findViewById(R.id.scoreText);

        getTodaysPerformance();
        getPerformanceScore();

        //find ImageView items
        imageItem1 = view.findViewById(R.id.welcomeImage1);
        imageItem2 = view.findViewById(R.id.welcomeImage2);
        imageItem3 = view.findViewById(R.id.welcomeImage3);

        //find link to the Images saved in Firebase storage
        imageUri1 = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/Front%20Image%20opt1.png?alt=media&token=72423929-cb21-48ba-8261-6276fe1bec8d";
        imageUri2 = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/front%20page%20option%204.jpg?alt=media&token=99a4a1b9-4239-42dc-ab13-811ad58eb7b5";
        imageUri3 = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/Front%20Page%20option2.jpg?alt=media&token=9681f925-4e5c-4953-8271-65e1603d4d57";

        //display images
        try {
            Glide.with(imageItem1.getContext()).load(imageUri1).into(imageItem1);
            Glide.with(imageItem2.getContext()).load(imageUri2).into(imageItem2);
            Glide.with(imageItem3.getContext()).load(imageUri3).into(imageItem3);
        }
        catch (Exception e) {e.printStackTrace();}

        //listener on textview to navigate to the ScoreExplainer activity and to pass Score Metric values
        scoreText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent toScoreExplainer = new Intent(getContext(), ScoreExplainer.class);

                Bundle bundle = new Bundle();
                bundle.putString("COMPLETION_FACTOR", Integer.toString(completionFactor));
                bundle.putString("GOAL_COUNT_FACTOR", Integer.toString(goalCountFactor));
                bundle.putString("TYPE_DIVERSITY_FACTOR", Integer.toString(typeDiversityFactor));
                bundle.putString("PERFORMANCE_SCORE", Integer.toString(performanceScore));
                toScoreExplainer.putExtras(bundle);

                startActivity(toScoreExplainer);

            }
        });

        return view;

    }

    public void getTodaysPerformance (){

        //reset counter
        todayAllCounter = 0; todayCompletedCounter = 0;

        //setting current date
        Calendar mCurrentDateAndTime = Calendar.getInstance();
        CharSequence currentTime = DateUtils.formatDateTime(getContext(), mCurrentDateAndTime.getTimeInMillis(),DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Query with today's date filter to count complete and all goals
        db.collection("users")
                .whereEqualTo("date", currentTime)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {


                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d("getTodaysPerformance", document.getId() + " => " + document.getData());
                                boolean statusCheck = document.get("status").toString().equals("complete");
                                todayAllCounter++;
                                if(statusCheck){
                                    todayCompletedCounter++;
                                }
                            }
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }

                        todayText.setText("Today you set " + todayAllCounter + " goals and completed " + todayCompletedCounter + ".");

                    }
                });
    }

    //Calculation of the Score Metric
    public void getPerformanceScore () {

        //reset counter
        fortnightAllCounter = 0; fortnightCompletedCounter = 0; fortnightIncompleteCounter = 0;
        typeCounter = 0;
        typeList.clear();

        //setting current date
        Calendar mCurrentDateAndTime = Calendar.getInstance();
        CharSequence currentTime = DateUtils.formatDateTime(getContext(), mCurrentDateAndTime.getTimeInMillis(),DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d("getTodaysPerformance", document.getId() + " => " + document.getData());

                                try {

                                    String snapshotDateText = document.get("date").toString();
                                    String snapshotTypeText = document.get("type").toString();

                                    //convert database date from String to Date format
                                    Date snapshotDate = new SimpleDateFormat("dd/MM/yyyy").parse(snapshotDateText);

                                    //Calculate the fortnight period
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    calendar.add(Calendar.DAY_OF_YEAR, -13);
                                    Date fortnightAgo = calendar.getTime();

                                    boolean fortnightCheck = snapshotDate.after(fortnightAgo);
                                    boolean statusCheck = document.get("status").toString().equals("complete");
                                    boolean typeCheck = typeList.contains(snapshotTypeText);

                                    Log.d("getPerformanceScore", "=>" + snapshotDateText + document.get("status").toString() );

                                    if(fortnightCheck && !statusCheck) {
                                        fortnightIncompleteCounter++;
                                    }
                                    if(fortnightCheck && statusCheck){
                                        fortnightCompletedCounter++;
                                    }
                                    if(fortnightCheck && !typeCheck){
                                        typeList.add(snapshotTypeText);
                                    }
                                    else{
                                    }
                                }
                                catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }

                        fortnightAllCounter = fortnightCompletedCounter + fortnightIncompleteCounter;
                        typeCounter = typeList.size();

                        //Calculate the metric factors
                        completionFactor = 100 / 3 * fortnightCompletedCounter / fortnightAllCounter;
                        goalCountFactor = 100 / 3 * fortnightAllCounter / (14 * 5);
                        typeDiversityFactor = 100 / 3 * typeCounter / 8;

                        Log.w("Score Check", "type diversity factor: " + typeDiversityFactor + " type counter: " + typeCounter);

                        //Calculate and display the Score Metric
                        performanceScore = completionFactor + goalCountFactor + typeDiversityFactor;
                        String performanceScoreText = Integer.toString(performanceScore);
                        scoreText.setText(performanceScoreText);
                    }
                });
    }
}

