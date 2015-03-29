package com.example.viktorjankov.shuttletracker.singletons;

import com.firebase.client.Firebase;

public class FirebaseProvider {
    private static final String FIREBASE_URL = "https://shuttletracker.firebaseio.com";
    private static final Firebase FIREBASE = new Firebase(FIREBASE_URL);
    public static Firebase getInstance() {
        return FIREBASE;
    }
}
