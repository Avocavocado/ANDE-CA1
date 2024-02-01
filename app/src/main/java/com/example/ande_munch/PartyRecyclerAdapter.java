package com.example.ande_munch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class PartyRecyclerAdapter extends RecyclerView.Adapter<PartyRecyclerAdapter.ViewHolder> {
    private ArrayList<HashMap<String, Object>> dataList; // Replace with your data model

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView partyMemberName;
        private final ImageView crownImageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            partyMemberName = (TextView) view.findViewById(R.id.tvPartyMemberName);
            crownImageView = view.findViewById(R.id.partyLeaderIcon);
        }

        public TextView getPartyMemberName() {
            return partyMemberName;
        }

        public ImageView getCrownImageView() {
            return crownImageView;
        }
    }

    public PartyRecyclerAdapter(ArrayList<HashMap<String, Object>> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_party_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> currentItem = dataList.get(position);
        holder.getPartyMemberName().setText(currentItem.get("Username").toString());
        if ((boolean) currentItem.get("IsLeader")) {
            holder.getCrownImageView().setVisibility(View.VISIBLE);
        } else {
            holder.getCrownImageView().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Method to update the data in the adapter
    public void updateData(ArrayList<HashMap<String, Object>> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}
