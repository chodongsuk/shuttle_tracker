package com.example.viktorjankov.shuttletracker.firebase;

import com.firebase.client.AuthData;

public class FirebaseAuthProvider {

    private static AuthData mAuthData;

    public static AuthData getmAuthData() {
        return mAuthData;
    }

    public static void setmAuthData(AuthData mAuthData) {
        FirebaseAuthProvider.mAuthData = mAuthData;
    }
}
