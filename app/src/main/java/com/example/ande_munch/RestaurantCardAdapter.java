package com.example.ande_munch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.ViewHolder> {

    private List<DocumentSnapshot> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView RestaurantName;
        private final TextView RestaurantDesc;
        private final ImageView RestaurantImage;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            RestaurantName = (TextView) view.findViewById(R.id.RestaurantName);
            RestaurantDesc = (TextView) view.findViewById(R.id.RestaurantDesc);
            RestaurantImage = (ImageView) view.findViewById(R.id.RestaurantImage);
        }

        public TextView getName() {
            return RestaurantName;
        }

        public TextView getDesc() {
            return RestaurantDesc;
        }

        public ImageView getImage() { return RestaurantImage; }
    }

    public RestaurantCardAdapter(List<DocumentSnapshot> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.restaurant_card, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getName().setText(localDataSet.get(position).getId());
        viewHolder.getDesc().setText(localDataSet.get(position).getString("Desc"));
        Picasso.get().load(localDataSet.get(position).getString("RestaurantImage")).into(viewHolder.getImage());
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}