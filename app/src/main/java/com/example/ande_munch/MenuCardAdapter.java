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

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.DoubleConsumer;

public class MenuCardAdapter extends RecyclerView.Adapter<MenuCardAdapter.ViewHolder> {

    private List<DocumentSnapshot> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView DishName;
        private final TextView DishPrice;
        private final ImageView DishImage;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            DishName = (TextView) view.findViewById(R.id.dishName);
            DishPrice = (TextView) view.findViewById(R.id.dishPrice);
            DishImage = (ImageView) view.findViewById(R.id.dishImage);
        }

        public TextView getName() {
            return DishName;
        }
        public TextView getPrice() { return DishPrice; }
        public ImageView getImage() { return DishImage; }
    }

    public MenuCardAdapter(List<DocumentSnapshot> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.menu_card, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        DocumentSnapshot item = localDataSet.get(position);
        viewHolder.getName().setText(item.getString("DishName"));
        Double price = (Double) item.getDouble("Price");
        viewHolder.getPrice().setText("$" + new DecimalFormat("0.00").format(price));
        String image = item.getString("DishImage");
        if (image != "") {
            Picasso.get().load(image).into(viewHolder.getImage());
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}