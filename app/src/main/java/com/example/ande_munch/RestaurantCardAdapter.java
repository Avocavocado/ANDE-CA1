package com.example.ande_munch;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.ViewHolder> {

    private List<Restaurant> localDataSet;
    private Context context;
    private static RestaurantCardAdapter.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String rid, double avgPrice, double avgRating);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView RestaurantName;
        private final TextView RestaurantDesc;
        private final ImageView RestaurantImage;
        private final LinearLayout Cuisines;
        private double avgPrice;
        private double avgRating;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            RestaurantName = (TextView) view.findViewById(R.id.RestaurantName);
            RestaurantDesc = (TextView) view.findViewById(R.id.RestaurantDesc);
            RestaurantImage = (ImageView) view.findViewById(R.id.RestaurantImage);
            Cuisines = (LinearLayout) view.findViewById(R.id.Cuisines);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(RestaurantName.getText().toString(),avgPrice,avgRating);
                    }
                }
            });
        }

        public TextView getName() {
            return RestaurantName;
        }
        public TextView getDesc() {
            return RestaurantDesc;
        }
        public ImageView getImage() { return RestaurantImage; }
        public LinearLayout getCuisines() { return Cuisines; }
        public void setAvgPrice(double avgPrice) {
            this.avgPrice = avgPrice;
        }

        public void setAvgRating(double avgRating) {
            this.avgRating = avgRating;
        }
    }

    public RestaurantCardAdapter(Context context, List<Restaurant> dataSet) {
        this.context = context;
        localDataSet = dataSet;
    }

    public void setOnItemClickListener(RestaurantCardAdapter.OnItemClickListener listener) {
        this.onItemClickListener = (RestaurantCardAdapter.OnItemClickListener) listener;
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
        Restaurant item = localDataSet.get(position);
        viewHolder.getName().setText(item.data.getId());
        viewHolder.getDesc().setText(item.data.getString("Desc"));
        Picasso.get().load(item.data.getString("RestaurantImage")).into(viewHolder.getImage());
        viewHolder.setAvgPrice(item.avgPrice);
        viewHolder.setAvgRating(item.avgRating);

        viewHolder.getCuisines().removeAllViews();
        for (String cuisine: (List<String>) item.data.get("Cuisine")) {
            TextView textView = new TextView(context);
            textView.setText(cuisine);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            int dp = (int) (0.5f * context.getResources().getDisplayMetrics().density);
            layoutParams.setMargins(0, 0, 24*dp, 0);
            textView.setPadding(26*dp, 8*dp, 26*dp, 8*dp);
            textView.setBackgroundColor(Color.parseColor("#BBBBBB"));
            textView.setLayoutParams(layoutParams);

            viewHolder.getCuisines().addView(textView);

        }
    }

    public void updateData(List<Restaurant> newDataSet) {
        localDataSet = newDataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}