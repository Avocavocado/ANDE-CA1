package com.example.ande_munch.classes;

import java.util.List;
import java.util.Map;

public class RestaurantInfo {
    private String name;
    private String description;
    private String restaurantImage;
    private List<String> cuisines;
    private List<Map<String, String>> openingHours;
    private double averageRating;
    private boolean isOpen;

    public RestaurantInfo(String name, String description, String restaurantImage, List<String> cuisines, List<Map<String, String>> openingHours, boolean isOpen, double averageRating) {
        this.name = name;
        this.description = description;
        this.restaurantImage = restaurantImage;
        this.cuisines = cuisines;
        this.openingHours = openingHours;
        this.isOpen = isOpen;
        this.averageRating = averageRating;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public List<Map<String, String>> getOpeningHours() {
        return openingHours;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
