package com.example.gohiking_cs310;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

//Recycler view to display and allow users to interact with group activity page
public class GroupActivityAdapter extends RecyclerView.Adapter<GroupActivityAdapter.ViewHolder> {

    //list to hold group activity objects
    private List<GroupActivity> groupActivities;

    //listen for user clicks
    private OnGroupActivityClickListener clickListener;

    //constructor initializes the adapter with group activity list and click listener
    public GroupActivityAdapter(List<GroupActivity> groupActivities, OnGroupActivityClickListener clickListener) {
        this.groupActivities = groupActivities;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    //view holder to display the recycler view
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    //allows recycler view to display data at specific positions on page
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupActivity groupActivity = groupActivities.get(position);

        //set ui values
        holder.titleTextView.setText(groupActivity.getTitle());
        holder.locationTextView.setText("Location: " + groupActivity.getLocation());
        holder.dateTextView.setText("Date: " + groupActivity.getTime().toString());
        holder.maxParticipantsTextView.setText("Max Participants: " + groupActivity.getMaxParticipants());

        //set click listeners
        holder.joinButton.setOnClickListener(v -> clickListener.onJoinClick(groupActivity));
        holder.leaveButton.setOnClickListener(v -> clickListener.onLeaveClick(groupActivity));
        holder.seeMembersButton.setOnClickListener(v -> clickListener.onSeeMembersClick(groupActivity));
        holder.addMemberButton.setOnClickListener(v -> clickListener.onAddMemberClick(groupActivity));
    }

    @Override
    //returns number of group activities
    public int getItemCount() {
        return groupActivities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, locationTextView, dateTextView, maxParticipantsTextView;
        Button joinButton, leaveButton, seeMembersButton, addMemberButton;

        //initialize textview and buttons
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            maxParticipantsTextView = itemView.findViewById(R.id.maxParticipantsTextView);
            joinButton = itemView.findViewById(R.id.joinButton);
            leaveButton = itemView.findViewById(R.id.leaveButton);
            seeMembersButton = itemView.findViewById(R.id.seemembersButton);
            addMemberButton = itemView.findViewById(R.id.addmemberButton);
        }
    }

    // Interface for button clicks
    public interface OnGroupActivityClickListener {
        void onJoinClick(GroupActivity groupActivity);
        void onLeaveClick(GroupActivity groupActivity);
        void onSeeMembersClick(GroupActivity groupActivity);
        void onAddMemberClick(GroupActivity groupActivity);
    }
}