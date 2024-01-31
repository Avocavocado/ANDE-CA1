package com.example.ande_munch;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.classes.Restaurant;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.ViewHolder> {

    private List<Restaurant> localDataSet;
    private Context context;
    private static RestaurantCardAdapter.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView RestaurantName;
        private final TextView RestaurantDesc;
        private final TextView OpenInfo;
        private final ImageView RestaurantImage;
        private final TextView Rating;
        private final LinearLayout Cuisines;
        private Restaurant restaurant;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            RestaurantName = (TextView) view.findViewById(R.id.RestaurantName);
            RestaurantDesc = (TextView) view.findViewById(R.id.RestaurantDesc);
            RestaurantImage = (ImageView) view.findViewById(R.id.RestaurantImage);
            OpenInfo = (TextView) view.findViewById(R.id.OpenInfo);
            Cuisines = (LinearLayout) view.findViewById(R.id.Cuisines);
            Rating = (TextView) view.findViewById(R.id.restaurantRating);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(restaurant);
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
        public TextView getOpenInfo() {
            return OpenInfo;
        }
        public TextView getRating() {
            return Rating;
        }
        public ImageView getImage() { return RestaurantImage; }
        public LinearLayout getCuisines() { return Cuisines; }
        public void setData(Restaurant restaurant) {
            this.restaurant = restaurant;
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
        viewHolder.setData(item);

        viewHolder.getName().setText(item.data.getId() );
        viewHolder.getDesc().setText(item.data.getString("Desc"));
        //viewHolder.getAddress().setText("• " + item.data.getString("Address") + " (" + (item.distance) + "km)");

        //Average Rating
        SpannableString avgRating = new SpannableString(item.avgRating + "★");
        avgRating.setSpan(new ForegroundColorSpan(Color.rgb(255,199,44)), 3, 4, 0);
        viewHolder.getRating().setText(avgRating);
        Picasso.get().load(item.data.getString("RestaurantImage")).into(viewHolder.getImage());

        //Cuisine Boxes
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
            textView.setBackgroundColor(Color.parseColor("#D7DAFF"));
            textView.setTextColor(Color.parseColor("#53555C"));
            textView.setLayoutParams(layoutParams);

            viewHolder.getCuisines().addView(textView);

        }

        //Opening Hours
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue() - 1;
        List<Object> openingHours = (List<Object>) item.data.get("OpeningHours");
        //Log.i("OPEN", item.data.getId() + " isNull" + (openingHours == null));
        Map<String,String> todayOH = (Map<String, String>) openingHours.get(dayOfWeek);
        LocalTime now = LocalTime.now();
        LocalTime openingTime = LocalTime.parse(todayOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
        LocalTime closingTime = LocalTime.parse(todayOH.get("Close"), DateTimeFormatter.ofPattern("H:mm"));

        boolean hasOpened = !now.isBefore(openingTime);
        boolean hasClosed = !now.isBefore(closingTime);

        if (hasOpened && !hasClosed) {
            viewHolder.getOpenInfo().setBackgroundColor(Color.parseColor("#00FF66"));
            viewHolder.getOpenInfo().setText("Open til " + closingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        }
        else if (!hasOpened && !hasClosed){
            viewHolder.getOpenInfo().setBackgroundColor(Color.parseColor("#30303A"));
            viewHolder.getOpenInfo().setText("Opens at " + openingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        }
        else {
            viewHolder.getOpenInfo().setBackgroundColor(Color.parseColor("#30303A"));
            Map<String,String> tomorrowOH = (Map<String, String>) openingHours.get(dayOfWeek != 6 ? dayOfWeek+1 : 0);
            LocalTime tomorrowOpeningTime = LocalTime.parse(tomorrowOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
            viewHolder.getOpenInfo().setText("Closed til " + tomorrowOpeningTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        }
        //Log.i("OpenInfo", openingTime + "-" + closingTime);
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