package com.example.ande_munch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.classes.Card;
import com.example.ande_munch.databinding.CardBinding;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder>{
    List<Card> cardList;

    public CardAdapter(List<Card> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public CardAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardBinding binding = CardBinding.inflate(inflater);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardAdapter.CardViewHolder holder, int position) {
        Card cardItem = cardList.get(position);
        holder.binding.content.setText(cardItem.getContent());
        holder.binding.image.setImageDrawable(cardItem.getImage());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardBinding binding;
        public CardViewHolder(@NonNull CardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
