package com.viktorjankov.shuttletracker.fragments;

import java.util.HashMap;
import java.util.List;

public interface AsyncTaskCompletionListener<T> {
    public void onTaskComplete(List<List<HashMap<String, String>>> result);
}
