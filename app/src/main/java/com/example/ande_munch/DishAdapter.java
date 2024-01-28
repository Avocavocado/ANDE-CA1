package com.example.ande_munch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.classes.Dish;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> {

    private List<Dish> dishes;

    public DishAdapter(List<Dish> dishes) {
        this.dishes = dishes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        Picasso.get()
                .load(dish.getUrl())
                .fit()
                .centerCrop()
                .into(holder.imageViewDish);
        holder.itemView.setOnClickListener(v -> onDishClicked(holder.itemView.getContext(), dish));
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewDish;

        public ViewHolder(View view) {
            super(view);
                imageViewDish = view.findViewById(R.id.imageViewDish);
        }
    }

    private void onDishClicked(Context context, Dish dish) {
        // Create an intent to start the DishDescriptionActivity
        Intent intent = new Intent(context, DishDescriptionActivity.class);

        // Pass data to the DishDescriptionActivity
        intent.putExtra("dishName", dish.getName());
        intent.putExtra("dishImageUrl", dish.getUrl());
        intent.putExtra("dishDescription", dish.getDescription());

        // Start the activity
        context.startActivity(intent);
    }

}
