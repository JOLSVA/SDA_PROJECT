package sda.oscail.edu.sda_project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class Goal extends Fragment {

    public Goal() {
        // Required empty public constructor
    }

    private EditText mGoalName, mGoalDate, mGoalType, mGoalDescription;
    Button mGoalDates;
    String selectedGoalType;
    private static final String TAG = "DatabaseUpdate";
    private Calendar mDateAndTime = Calendar.getInstance();
    private Calendar mCurrentDateAndTime = Calendar.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal, container, false);

        //find the editText widgets
        mGoalName = view.findViewById(R.id.Goal);
        mGoalDates = view.findViewById(R.id.Date);
        mGoalDescription = view.findViewById(R.id.Description);

        //setting current date as default value
        CharSequence currentTime = DateUtils.formatDateTime(getContext(), mCurrentDateAndTime.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);
        mGoalDates.setText(currentTime);

        //SPINNER START

        final Spinner mGoalType = (Spinner) view.findViewById(R.id.Type);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.goal_type_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mGoalType.setAdapter(adapter);

        //listener for spinner item selection
        mGoalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedGoalType = (String) parentView.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //SPINNER END

        // find the button
        Button saveButton = view.findViewById(R.id.Save);

        //DATABASE UPDATE START

        // Access a Cloud Firestore instance
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //setting conditions for an incomplete document, where the Cloud Firestore is not to get updated
                boolean checkGoal = mGoalName.getText().toString().equals("");
                boolean checkDescription = mGoalDescription.getText().toString().equals("");
                boolean checkType = selectedGoalType.equals("SELECT TYPE!");

                if (checkGoal || checkDescription || checkType) {
                    Toast.makeText(getContext(), "Please fill out all fields!", Toast.LENGTH_LONG).show();
                }

                else{
                    // Create a new user document with complete data: date, goal, type, description and status
                    Map<String, Object> user = new HashMap<>();
                    user.put("date", mGoalDates.getText().toString());
                    user.put("goal", mGoalName.getText().toString());
                    user.put("type", selectedGoalType);
                    user.put("description", mGoalDescription.getText().toString());
                    user.put("status", "incomplete");

                    // Add a new document with an Auto-generated ID
                    db.collection("users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    mGoalName.setText("");
                                    mGoalDescription.setText("");
                                    Toast.makeText(getContext(), "Goal saved successfully!", Toast.LENGTH_LONG).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                }
            }
        });

        //DATABASE UPDATE END

        //date button and date picker
        mGoalDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        mDateAndTime.set(Calendar.YEAR, year);
                        mDateAndTime.set(Calendar.MONTH, monthOfYear);
                        mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateAndTimeDisplay();
                    }
                };

                //set up date picker
                DatePickerDialog myDatePickerDialog = new DatePickerDialog(getContext(), mDateListener,
                        mDateAndTime.get(Calendar.YEAR),
                        mDateAndTime.get(Calendar.MONTH),
                        mDateAndTime.get(Calendar.DAY_OF_MONTH));

                //restrict selection of past date and display
                myDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                myDatePickerDialog.show();
            }

            /**
             DATEPCKER END
             */

        });
        return view;
    }

    private void updateDateAndTimeDisplay() {

        //date time year
        CharSequence SelectedDate = DateUtils.formatDateTime(getContext(), mDateAndTime.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);
        mGoalDates.setText(SelectedDate);
    }
}
