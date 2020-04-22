package sda.oscail.edu.sda_project;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;


public class Day extends Fragment {

    public Day() {
        // Required empty public constructor
    }

    private static final String TAG = "DatabaseQuery";
    private RecyclerView recyclerView;
    private DayViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Calendar mDateAndTime = Calendar.getInstance();
    private Calendar mCurrentDateAndTime = Calendar.getInstance();

    final ArrayList<String> goalDataset = new ArrayList<String>();
    final ArrayList<String> typeDataset = new ArrayList<String>();
    final ArrayList<String> descriptionDataset = new ArrayList<String>();
    final ArrayList<String> docReferenceDataset = new ArrayList<String>();
    final ArrayList<String> completionDataset = new ArrayList<String>();
    final ArrayList<String> dateDataset = new ArrayList<String>();

    Button mDayDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        recyclerView = view.findViewById(R.id.day_recycler_view);

        //find the date button
        mDayDate = view.findViewById(R.id.Date);

        //setting current date as default value
        CharSequence currentTime = DateUtils.formatDateTime(getContext(), mCurrentDateAndTime.getTimeInMillis(),DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);
        mDayDate.setText(currentTime);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify the adapter and attach it to the recyclerview
        mAdapter = new DayViewAdapter(getContext(), docReferenceDataset, goalDataset, typeDataset, descriptionDataset, completionDataset, dateDataset);
        recyclerView.setAdapter(mAdapter);

        mDayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        mDateAndTime.set(Calendar.YEAR, year);
                        mDateAndTime.set(Calendar.MONTH, monthOfYear);
                        mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        clearArrayListContent();
                        updateDateAndTimeDisplay();
                        databaseQueryForDay();
                        clearArrayListContent();

                    }
                };

                //set up date picker
                DatePickerDialog myDatePickerDialog = new DatePickerDialog(getContext(), mDateListener,
                        mDateAndTime.get(Calendar.YEAR),
                        mDateAndTime.get(Calendar.MONTH),
                        mDateAndTime.get(Calendar.DAY_OF_MONTH));

                //display date picker
                myDatePickerDialog.show();

            }

            /**
             DATEPCKER END
             */

        });

        Log.d(TAG, "Date for check" + mDayDate.getText().toString());

        //DATABASE QUERY START

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //run Cloud Firestore query for the selected date as filter and listen database changes
        db.collection("users")
                .whereEqualTo("date", mDayDate.getText().toString())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        Log.w(TAG, "Listen elkapva", e);
                        //clearArrayListContent();
                        //databaseQueryForDay();
                        //clearArrayListContent();
                    }
                });

        //DATABASE QUERY END

        return view;
    }

    private void updateDateAndTimeDisplay() {

        //date time year
        CharSequence SelectedDate = DateUtils.formatDateTime(getContext(), mDateAndTime.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);

        //display summary
        mDayDate.setText(SelectedDate);
    }

    public void databaseQueryForDay (){
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    clearArrayListContent();

    db.collection("users")
            .whereEqualTo("date", mDayDate.getText().toString())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Log.d(TAG, document.getId() + " => " + document.getData());

                            docReferenceDataset.add(document.getId());
                            goalDataset.add(document.get("goal").toString());
                            typeDataset.add(document.get("type").toString());
                            descriptionDataset.add(document.get("description").toString());
                            completionDataset.add(document.get("status").toString());
                            dateDataset.add(document.get("date").toString());

                            mAdapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                }
            });
    }

    //clear all arrays that feed data in the recyclerview
    public void clearArrayListContent() {

        goalDataset.clear();
        typeDataset.clear();
        descriptionDataset.clear();
        completionDataset.clear();
        dateDataset.clear();
        docReferenceDataset.clear();

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume(){
        super.onResume();

        clearArrayListContent();
        databaseQueryForDay();
        clearArrayListContent();
    }
}
