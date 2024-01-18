package com.example.ande_munch.methods;

import java.util.List;
import java.util.Map;

public interface Callback {
    void onUserChecked(boolean userExists);

    void onUserDataFetched(List<Map<String, Object>> usersList);

    void onFailure(Exception e);
}