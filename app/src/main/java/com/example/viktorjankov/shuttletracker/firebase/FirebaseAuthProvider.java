package com.example.viktorjankov.shuttletracker.firebase;

import com.firebase.client.AuthData;
import com.google.android.gms.common.api.GoogleApiClient;

public class FirebaseAuthProvider {

    private static AuthData mAuthData;
    private static GoogleApiClient mGoogleApiClient;

    public static AuthData getAuthData() {
        return mAuthData;
    }

    public static void setAuthData(AuthData mAuthData) {
        FirebaseAuthProvider.mAuthData = mAuthData;
    }

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        FirebaseAuthProvider.mGoogleApiClient = mGoogleApiClient;
    }
}
