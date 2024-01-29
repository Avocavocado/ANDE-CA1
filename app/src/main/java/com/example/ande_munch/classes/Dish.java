package com.example.ande_munch.classes;

public class Dish {
    private String name;
    private String cuisine;
    private String url;
    private String description;
    public Dish(String name, String cuisine, String url, String description) {
        this.name = name;
        this.cuisine = cuisine;
        this.url = url;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() { return description; }

}
