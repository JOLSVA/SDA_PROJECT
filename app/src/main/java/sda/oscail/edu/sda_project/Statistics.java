package sda.oscail.edu.sda_project;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Statistics extends Fragment {

    public Statistics() {
        // Required empty public constructor
    }

    private DecimalFormat myFormat = new DecimalFormat("#,##0");
    private Calendar mDateAndTime = Calendar.getInstance();
    int periodInDays = -13;
    Date setCustomDate;

    //HORIZONTAL BARCHART START
    HorizontalBarChart horizontalChart;
    int completeCounter = 0;
    int incompleteCounter = 0;
    int[] counterDataForBar = {completeCounter, incompleteCounter};
    //HORIZONTAL BARCHART END

    //PIECHART START
    private PieChart pieChart;
    private PieData pieData;
    private PieDataSet pieDataSet;
    ArrayList pieEntries;
    private int numberEducational = 0;
    private int numberHealth = 0;
    private int numberRelationship = 0;
    private int numberDevelopment = 0;
    private int numberCareer = 0;
    private int numberFinancial = 0;
    private int numberExperiential = 0;
    private int numberPersonalDevelopment = 0;
    private int numberOther = 0;

    int[] typeDataForPie = {numberEducational, numberHealth, numberRelationship, numberDevelopment, numberCareer, numberFinancial,
            numberExperiential, numberPersonalDevelopment, numberOther};
    String[] typeLabelsForPie = {"educational", "health", "relationship", "development", "career",
            "financial", "experiential", "personal development", "other"};
    //PIECHART END

    //LINECHART START
    private LineChart lineChart;
    final int allGoalsArray[] = new int[360];
    int maxXAxisValue;
    //LINECHART END

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        //find the chart views
        horizontalChart = view.findViewById(R.id.complete_incomplete_chart);
        lineChart = view.findViewById(R.id.lineChart);
        pieChart = view.findViewById(R.id.pieChart);

        //set fortnight as default period
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -13);
        setCustomDate = calendar.getTime();

        //run the charts for the default fortnight period when the view is inflated, then clear values
        setAxisMaximumValue();
        getDataForBar();
        executeQueryForPieChart();
        getDataPointsForDataLine();
        clearData();

        //find the button
        Button mCustomDate;
        mCustomDate = view.findViewById(R.id.customDate);

        //listen custom button and update the charts
        mCustomDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {

                        mDateAndTime.set(Calendar.YEAR, year);
                        mDateAndTime.set(Calendar.MONTH, monthOfYear);
                        mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        //customise the period of interest
                        setCustomDate = mDateAndTime.getTime();

                        //rerun the charts for the customised perdiod then clear values
                        setAxisMaximumValue();
                        getDataForBar();
                        executeQueryForPieChart();
                        getDataPointsForDataLine();
                        clearData();
                    }
                };

                //set up date picker
                DatePickerDialog myDatePickerDialog = new DatePickerDialog(getContext(), mDateListener,
                        mDateAndTime.get(Calendar.YEAR),
                        mDateAndTime.get(Calendar.MONTH),
                        mDateAndTime.get(Calendar.DAY_OF_MONTH));

                //display
                myDatePickerDialog.show();
            }
        });

        //find the button
        Button mFortNightDate;
        mFortNightDate = view.findViewById(R.id.fornightDate);

        //listen for the fortnight button and update the charts
        mFortNightDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the date of two weeks ago
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -13);
                setCustomDate = calendar.getTime();

                //rerun the charts for the fortnight period then clear values
                setAxisMaximumValue();
                getDataForBar();
                executeQueryForPieChart();
                getDataPointsForDataLine();
                clearData();
            }
        });

        return view;
    }

    //adjust line chart demonstration to the set time period
    public void setAxisMaximumValue(){
        Date todaysDate = new Date();

        //calculate the days between today's date and
        long timeBetween = todaysDate.getTime() - setCustomDate.getTime();

        //long daysBetweenLong = TimeUnit.DAYS.convert(timeBetween, TimeUnit.DAYS);
        long daysBetweenLong = timeBetween / (1000 * 3600 * 24);
        int daysBetween = (int) daysBetweenLong;

        maxXAxisValue = daysBetween;
    }

    //Pie1 - get the data for the pie chart
    public void executeQueryForPieChart() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String typeData;
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                try {
                                    String snapshotDateText = document.get("date").toString();
                                    String snapshotTypeText = document.get("type").toString();

                                    Date snapshotDate = new SimpleDateFormat("dd/MM/yyyy").parse(snapshotDateText);

                                    //get the date of two weeks ago
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    calendar.add(Calendar.DAY_OF_YEAR, periodInDays);
                                    Date fortnightAgo = calendar.getTime();

                                    Date todaysDate = new Date();

                                    //calculate the days between today's date and
                                    long timeBetween = todaysDate.getTime() - snapshotDate.getTime();
                                    //long daysBetweenLong = TimeUnit.DAYS.convert(timeBetween, TimeUnit.DAYS);
                                    long daysBetweenLong = timeBetween / (1000 * 3600 * 24);
                                    int daysBetween = (int) daysBetweenLong;

                                    boolean fortnightCheck = snapshotDate.after(setCustomDate);

                                    if (snapshotTypeText.equals("educational") && fortnightCheck) {
                                        typeDataForPie[0]++;
                                    }
                                    if (snapshotTypeText.equals("health") && fortnightCheck) {
                                        typeDataForPie[1]++;
                                    }
                                    if (snapshotTypeText.equals("relationship") && fortnightCheck) {
                                        typeDataForPie[2]++;
                                    }
                                    if (snapshotTypeText.equals("development") && fortnightCheck) {
                                        typeDataForPie[3]++;
                                    }
                                    if (snapshotTypeText.equals("career") && fortnightCheck) {
                                        typeDataForPie[4]++;
                                    }
                                    if (snapshotTypeText.equals("financial") && fortnightCheck) {
                                        typeDataForPie[5]++;
                                    }
                                    if (snapshotTypeText.equals("experiential") && fortnightCheck) {
                                        typeDataForPie[6]++;
                                    }
                                    if (snapshotTypeText.equals("personal development") && fortnightCheck) {
                                        typeDataForPie[7]++;
                                    }
                                    if (snapshotTypeText.equals("other") && fortnightCheck) {
                                        typeDataForPie[8]++;
                                    }

                                    //Log.d("sikerult", document.getId() + " => " + document.getData());

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        //update the chart with the data from the query
                        getPieChart();
                        }

                        else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    //Pie2 - draw the pie chart
    private void getPieChart(){

        pieEntries = new ArrayList<>();

        for (int counter = 0; counter < typeLabelsForPie.length; counter++) {

            Log.d("TAG", "see how it works");
            if (typeDataForPie[counter] > 0) {
                pieEntries.add(new PieEntry(typeDataForPie[counter], typeLabelsForPie[counter]));
            }
        }

        pieDataSet = new PieDataSet(pieEntries, "Goals per Type");
        pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setSliceSpace(5f);
        pieDataSet.setValueFormatter(new MyValueFormatter());
        pieChart.invalidate();

    }

    //Line1 - The method runs the query required for the getDataline method (Line Chart)
    public void getDataPointsForDataLine() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("sikerult", document.getId() + " => " + document.getData());

                                try {
                                    String snapshotDateText = document.get("date").toString();
                                    String snapshotStatusText = document.get("status").toString();

                                    Date snapshotDate = new SimpleDateFormat("dd/MM/yyyy").parse(snapshotDateText);

                                    //get the date of two weeks ago
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    calendar.add(Calendar.DAY_OF_YEAR, periodInDays);
                                    Date fortnightAgo = calendar.getTime();

                                    Date todaysDate = new Date();

                                    //calculate the days between today's date and
                                    long timeBetween = todaysDate.getTime() - snapshotDate.getTime();
                                    //long daysBetweenLong = TimeUnit.DAYS.convert(timeBetween, TimeUnit.DAYS);
                                    long daysBetweenLong = timeBetween / (1000 * 3600 * 24);
                                    int daysBetweenLine = (int) daysBetweenLong;

                                    boolean fortnightCheck = snapshotDate.after(setCustomDate);
                                    boolean statusCheck = snapshotStatusText.equals("complete");

                                    Log.d("daysBetween", "daysBetween" + daysBetweenLine);
                                    if (fortnightCheck) {
                                        allGoalsArray[daysBetweenLine]++;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        getLineChart();
                        }
                    }
                });
    }

    //Line2 - draw the line chart
    private void getLineChart (){

        final ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < allGoalsArray.length; i++) {
            //Log.d("Check Goal array", "Goal arrayB index " + i + " value "+ allGoalsArray[i]);
            entries.add(new Entry(i, allGoalsArray[i]));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Goals per day");

        lineDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        lineDataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        lineDataSet.setLineWidth(3);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new MyValueFormatter());

        XAxis xLineAxis = lineChart.getXAxis();
        xLineAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLineAxis.setGranularity(1f);
        xLineAxis.setDrawGridLines(false);
        xLineAxis.setAxisMaximum(maxXAxisValue);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setDrawGridLines(false);
        yAxisRight.setDrawGridLines(false);

        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
        lineChart.animateX(2500);
        lineChart.invalidate();
    }

    //Bar1 - get the data for the bar chart
    public void getDataForBar() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                try {
                                    String snapshotDateText = document.get("date").toString();
                                    String snapshotStatusText = document.get("status").toString();

                                    Date snapshotDate = new SimpleDateFormat("dd/MM/yyyy").parse(snapshotDateText);

                                    //get the date of two weeks ago
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    calendar.add(Calendar.DAY_OF_YEAR, periodInDays);
                                    Date fortnightAgo = calendar.getTime();

                                    Date todaysDate = new Date();

                                    //calculate the days between today's date and
                                    long timeBetween = todaysDate.getTime() - snapshotDate.getTime();
                                    //long daysBetweenLong = TimeUnit.DAYS.convert(timeBetween, TimeUnit.DAYS);
                                    long daysBetweenLong = timeBetween / (1000 * 3600 * 24);
                                    int daysBetween = (int) daysBetweenLong;

                                    boolean fortnightCheck = snapshotDate.after(setCustomDate);
                                    boolean statusCheck = snapshotStatusText.equals("complete");

                                    Log.d("sikerult", document.getId() + " => " + document.getData());

                                    if (fortnightCheck && statusCheck) {
                                        completeCounter++;
                                        Log.d("PrepData", "complete" + completeCounter);

                                    }
                                    if (fortnightCheck && !statusCheck) {
                                        incompleteCounter++;
                                        Log.d("PrepData", "incomplete" + incompleteCounter);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        //upadte the chart with the query data
                        getHorizontalChart();
                    }
                });

    }

    //Bar2 - draw the horizontal bar chart
    private void getHorizontalChart(){

        horizontalChart.setFitBars(true);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, completeCounter));
        //Log.d("PrepDataPres", "complete" + completeCounter);
        entries.add(new BarEntry(1f, incompleteCounter));
        //Log.d("PrepDataPres", "incomplete" + completeCounter);

        BarDataSet barDataSet = new BarDataSet(entries, "Completion status");

        barDataSet.setBarBorderWidth(0.9f);
        barDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        barDataSet.setValueTextSize(10f);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.7f);
        barDataSet.setValueFormatter(new MyValueFormatter());

        XAxis xAxis = horizontalChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);

        YAxis yAxis1 = horizontalChart.getAxisLeft();
        YAxis yAxis2 = horizontalChart.getAxisRight();
        yAxis1.setAxisMinimum(0);
        yAxis2.setAxisMinimum(0);

        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawLabels(false);
        yAxis1.setDrawAxisLine(false);
        yAxis2.setDrawGridLines(false);
        yAxis2.setDrawLabels(false);
        yAxis2.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);

        final String[] statusGoal = new String[]{"Complete", "Incomplete"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(statusGoal);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        horizontalChart.setData(barData);
        horizontalChart.setFitBars(true);
        horizontalChart.animateXY(2000, 2000);
        horizontalChart.invalidate();
        horizontalChart.setTouchEnabled(false);
    }

    //clear data to avoid duplications
    private void clearData(){
        incompleteCounter = 0; completeCounter = 0;

        for (int i=0; i < typeDataForPie.length; i++){
            typeDataForPie[i] = 0;
        }
        for (int i=0; i < allGoalsArray.length; i++){
            allGoalsArray[i] = 0;
        }
    }

    //to remove decimal places from the chart data
    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {

            // use no decimal
            mFormat = new DecimalFormat("###,###,##0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            return mFormat.format(value);
        }
    }

}

