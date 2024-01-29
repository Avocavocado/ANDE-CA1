package com.example.ande_munch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.classes.Dish;
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> {

    private List<Dish> dishes;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LoginMethods loginMethods = new LoginMethods();
    String loggedInEmail = loginMethods.getUserEmail();
    Object DishesUnlocked;
    public DishAdapter(List<Dish> dishes, Object DishUnlocked) {
        this.dishes = dishes;
        this.DishesUnlocked = DishUnlocked;
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

        boolean isLocked = true;

        if (DishesUnlocked instanceof Map) {
            Map<String, Boolean> unlockedDishes = (Map<String, Boolean>) DishesUnlocked;
            Boolean unlockedStatus = unlockedDishes.get(dish.getName());
            if (unlockedStatus != null && unlockedStatus) {
                isLocked = false;
            }
        }

        if (isLocked) {
            // Load the image with Picasso and apply a gray scale color filter
            Picasso.get()
                    .load(dish.getUrl())
                    .fit()
                    .centerCrop()
                    .transform(new GrayscaleTransformation()) // Apply grayscale transformation
                    .into(holder.imageViewDish);
        } else {
            // Load the image without gray scale filter
            Picasso.get()
                    .load(dish.getUrl())
                    .fit()
                    .centerCrop()
                    .into(holder.imageViewDish);
        }

        holder.itemView.setOnClickListener(v -> onDishClicked(holder.itemView.getContext(), dish));
    }

    public static class GrayscaleTransformation implements com.squareup.picasso.Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
            Bitmap grayscaleBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(grayscaleBitmap);
            canvas.drawBitmap(source, 0, 0, paint);
            if (source != grayscaleBitmap) {
                source.recycle();
            }
            return grayscaleBitmap;
        }

        @Override
        public String key() {
            return "grayscale";
        }
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
