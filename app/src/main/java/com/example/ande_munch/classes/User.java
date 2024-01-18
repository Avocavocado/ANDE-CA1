package com.example.ande_munch.classes;

import java.util.Map;

public class User {
    private Map<String, Object> details;

    public User( Map<String, Object> details) {
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}

