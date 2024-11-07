package com.example.gohiking_cs310;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupActivityAdapter extends RecyclerView.Adapter<GroupActivityAdapter.ViewHolder> {

    private List<GroupActivity> groupActivities;
    private OnGroupActivityClickListener clickListener;

    public GroupActivityAdapter(List<GroupActivity> groupActivities, OnGroupActivityClickListener clickListener) {
        this.groupActivities = groupActivities;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupActivity groupActivity = groupActivities.get(position);

        holder.titleTextView.setText(groupActivity.getTitle());
        holder.locationTextView.setText("Location: " + groupActivity.getLocation());
        holder.dateTextView.setText("Date: " + groupActivity.getTime().toString());
        holder.maxParticipantsTextView.setText("Max Participants: " + groupActivity.getMaxParticipants());

        holder.joinButton.setOnClickListener(v -> clickListener.onJoinClick(groupActivity));
        holder.leaveButton.setOnClickListener(v -> clickListener.onLeaveClick(groupActivity));
        holder.seeMembersButton.setOnClickListener(v -> clickListener.onSeeMembersClick(groupActivity));
        holder.addMemberButton.setOnClickListener(v -> clickListener.onAddMemberClick(groupActivity));
    }

    @Override
    public int getItemCount() {
        return groupActivities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, locationTextView, dateTextView, maxParticipantsTextView;
        Button joinButton, leaveButton, seeMembersButton, addMemberButton;

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