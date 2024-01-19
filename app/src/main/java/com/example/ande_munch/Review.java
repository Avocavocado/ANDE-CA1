package com.example.ande_munch;

public class Review {
    public String username;
    public String feedback;
    public Double rating;
    public String profilePic;

    public Review(String username, String feedback, String profilePic, Double rating) {
        this.username = username;
        this.feedback = feedback;
        this.rating = rating;
        this.profilePic = profilePic;
    }
}
