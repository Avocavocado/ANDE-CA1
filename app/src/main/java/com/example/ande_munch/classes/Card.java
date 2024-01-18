package com.example.ande_munch.classes;

import android.graphics.drawable.Drawable;

public class Card {
    String content;
    Drawable image;

    public Card (String content, Drawable image) {
        this.content = content;
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public Drawable getImage() {
        return image;
    }
}
