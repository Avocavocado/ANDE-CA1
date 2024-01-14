package com.example.ande_munch;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class CuisineButtonAdapter extends RecyclerView.Adapter<CuisineButtonAdapter.ViewHolder> {

    private List<String> urls;
    private List<String> cuisines;
    private Context context;
    private static OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String cuisineName, View v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView CuisineImage;
        private final TextView CuisineName;

        public ViewHolder(View view) {
            super(view);
            CuisineImage = view.findViewById(R.id.cuisineImage);
            CuisineName = view.findViewById(R.id.cuisineName);

            // Set click listener directly in the ViewHolder
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(CuisineName.getText().toString(), v);
                    }
                }
            });
        }

        public TextView getName() {
            return CuisineName;
        }

        public ImageView getImage() {
            return CuisineImage;
        }
    }

    public CuisineButtonAdapter(Context context, List<String> cuisines, List<String> urls) {
        this.context = context;
        this.cuisines = cuisines;
        this.urls = urls;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.cuisine_button, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getName().setText(cuisines.get(position));

        Picasso.get().load(context.getResources().getIdentifier(urls.get(position), "drawable", context.getPackageName()))
                .into(viewHolder.getImage());
        viewHolder.getImage().setClipToOutline(true);
        viewHolder.getImage().setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int diameter = Math.min(view.getWidth(), view.getHeight());
                float radius = diameter / 2.0f;
                outline.setRoundRect(0, 0, diameter, diameter, radius);
            }
        });
    }

    @Override
    public int getItemCount() { return cuisines.size(); }
}
