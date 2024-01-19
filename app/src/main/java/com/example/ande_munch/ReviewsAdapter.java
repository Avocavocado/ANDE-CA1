package com.example.ande_munch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private List<Review> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView rating;
        private final TextView username;
        private final TextView feedback;
        private final ImageView profilePic;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            rating = (TextView) view.findViewById(R.id.rating);
            username = (TextView) view.findViewById(R.id.username);
            feedback = (TextView) view.findViewById(R.id.feedback);
            profilePic = (ImageView) view.findViewById(R.id.profilePic);
        }

        public TextView getRating() {
            return rating;
        }
        public TextView getUsername() { return username; }
        public TextView getFeedback() { return feedback; }
        public ImageView getProfilePic() { return profilePic; }
    }

    public ReviewsAdapter(List<Review> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReviewsAdapter.ViewHolder viewHolder, final int position) {
        Review item = localDataSet.get(position);
        viewHolder.getUsername().setText(item.username);
        viewHolder.getFeedback().setText(item.feedback);
        viewHolder.getRating().setText(item.rating.toString());
        String image = item.profilePic;
        if (image != "") {
            Picasso.get().load(image).into(viewHolder.getProfilePic());
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
