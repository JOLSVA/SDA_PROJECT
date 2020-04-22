package sda.oscail.edu.sda_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.ViewHolder> {

    private Context mNewContext;

    //add array for each item
    final ArrayList<String> goalDataset;
    final ArrayList<String> typeDataset;
    final ArrayList<String> descriptionDataset;
    final ArrayList<String> docReferenceDataset;
    final ArrayList<String> completionDataset;
    final ArrayList<String> dateDataset;

    //adding URI to Firebase Storage for the images to be displayed
    String completionImageUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/blue_complete_task.jpg?alt=media&token=92a04940-4a02-4957-80c3-4b46b54172d0";
    String educationalUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/educational.png?alt=media&token=72844dbe-9669-4728-b73f-856dfcff74ea";
    String healthUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/health.jpg?alt=media&token=76c7b62e-6795-4525-b099-834c9c42293b";
    String relationshipUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/relationship2.jpg?alt=media&token=4654024a-9670-4d47-a509-dda4b5a8f484";
    String personalDevelopmentUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/personal%20development.jpg?alt=media&token=aaf977dd-16f0-4735-9944-4a287f2bfc02";
    String careerUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/career.png?alt=media&token=642b8db6-ff59-4575-9245-13ca66e6f19d";
    String financialUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/financial.png?alt=media&token=3c413527-6c59-44aa-93f9-069ca3824d96";
    String experientialUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/educational.png?alt=media&token=72844dbe-9669-4728-b73f-856dfcff74ea";
    String otherUri = "https://firebasestorage.googleapis.com/v0/b/sda-project-goal.appspot.com/o/other.png?alt=media&token=32a9ec04-249b-496e-abd0-1731622de38b";

    private static final String TAG = "Recycler View Adapter";

    DayViewAdapter(Context mNewContext, ArrayList<String> docReferenceDataset, ArrayList<String> goalDataset, ArrayList<String> typeDataset,ArrayList<String> descriptionDataset, ArrayList<String> completionDataset, ArrayList<String> dateDataset) {

        this.mNewContext = mNewContext;

        this.goalDataset = goalDataset;
        this.typeDataset = typeDataset;
        this.descriptionDataset = descriptionDataset;
        this.docReferenceDataset = docReferenceDataset;
        this.completionDataset = completionDataset;
        this.dateDataset = dateDataset;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {

        Log.d(TAG, "onBindViewHolder: was called");


        int counter = position+1;
        String counterText = Integer.toString(counter);

        //assigned the query values to the recyclerview
        viewHolder.goalText.setText(goalDataset.get(position));
        viewHolder.typeText.setText(typeDataset.get(position));
        viewHolder.descriptionText.setText(descriptionDataset.get(position));
        viewHolder.goalCounter.setText(counterText);

        //display the type descriptive images
        try {
            switch (typeDataset.get(position).toString()) {
                case "educational" : Glide.with(viewHolder.typeImage.getContext()).load(educationalUri).into(viewHolder.typeImage);break;
                case "health" : Glide.with(viewHolder.typeImage.getContext()).load(healthUri).into(viewHolder.typeImage);break;
                case "relationship" : Glide.with(viewHolder.typeImage.getContext()).load(relationshipUri).into(viewHolder.typeImage);break;
                case "personal development" : Glide.with(viewHolder.typeImage.getContext()).load(personalDevelopmentUri).into(viewHolder.typeImage);break;
                case "career" : Glide.with(viewHolder.typeImage.getContext()).load(careerUri).into(viewHolder.typeImage);break;
                case "financial" : Glide.with(viewHolder.typeImage.getContext()).load(financialUri).into(viewHolder.typeImage);break;
                case "experiential" : Glide.with(viewHolder.typeImage.getContext()).load(experientialUri).into(viewHolder.typeImage);break;
                case "other" : Glide.with(viewHolder.typeImage.getContext()).load(otherUri).into(viewHolder.typeImage);break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //add completion check
        boolean checkCompletionStatus = completionDataset.get(position).equals("complete");
        if (checkCompletionStatus) {
            try {
                Glide.with(viewHolder.completionImage.getContext()).load(completionImageUri).into(viewHolder.completionImage);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            viewHolder.completeToggleButton.setChecked(false);
        }
        else {
            viewHolder.completeToggleButton.setChecked(true);
        }

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        //listener on the remove button to delete a document at the give recyclerview position
        viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Alert Dialog to ask for confirmation before a document is deleted
                AlertDialog alertDialog = new AlertDialog.Builder(mNewContext).create();
                alertDialog.setTitle("Confirmation request");
                alertDialog.setMessage("Please confirm if you want to delete this goal record permanently.");

                //delete after confirmation
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Sure", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                    // Remove a new document with a generated ID
                    db.collection("users").document(docReferenceDataset.get(position))
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Document updated successfully");

                                    //remove the relevant goal from the arraylists and update the recyclerview
                                    goalDataset.remove(position);
                                    typeDataset.remove(position);
                                    descriptionDataset.remove(position);
                                    docReferenceDataset.remove(position);
                                    completionDataset.remove(position);
                                    dateDataset.remove(position);
                                    notifyDataSetChanged();

                                }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            }
                        });
                            }

                });

                //dismiss the deletion after rejection
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Not really", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                });
                alertDialog.show();
            }
        });

        //listener on the complete button to delete a document at the give recyclerview position
        viewHolder.completeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    // update the satus of the user collection document in Cloud Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("status", "incomplete");

                    // update the database for status only for the determined document
                    db.collection("users").document(docReferenceDataset.get(position))
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Document updated successfully");
                                    //Toast.makeText(getApplicationContext(), "Goal saved successfully!", Toast.LENGTH_LONG).show();
                                    //completionText.setText("incomplete");
                                    viewHolder.completionImage.setImageResource(0);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                }

                else {

                    // update the satus of the user collection document in Cloud Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("status", "complete");

                    // update the database for status only for the determined document
                    db.collection("users").document(docReferenceDataset.get(position))
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Document updated successfully");
                                    //Toast.makeText(getApplicationContext(), "Goal saved successfully!", Toast.LENGTH_LONG).show();
                                    try {
                                        Glide.with(viewHolder.completionImage.getContext()).load(completionImageUri).into(viewHolder.completionImage);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
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

    }
    @Override
    public int getItemCount() {
        return goalDataset.size();
    }

    //view holder class for recycler_list_item.xml
    class ViewHolder extends RecyclerView.ViewHolder{

        //ImageView imageItem;
        TextView goalText, typeText, descriptionText, goalCounter;
        Button removeButton;
        ToggleButton completeToggleButton;
        ConstraintLayout itemParentLayout;
        ImageView completionImage, typeImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //grab the image, the text and the layout id's
            goalText = itemView.findViewById(R.id.goal_recycler);
            typeText = itemView.findViewById(R.id.type_recycler);
            descriptionText = itemView.findViewById(R.id.description_recycler);
            goalCounter = itemView.findViewById(R.id.goalCounter);
            completionImage = itemView.findViewById(R.id.completionImageView);
            typeImage = itemView.findViewById(R.id.typeImageView);

            removeButton = itemView.findViewById(R.id.button_recycler);
            completeToggleButton = itemView.findViewById(R.id.toggleButton_recycler);
            itemParentLayout = itemView.findViewById(R.id.listItemLayout);
        }
    }
}