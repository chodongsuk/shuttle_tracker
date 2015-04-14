package com.example.viktorjankov.shuttletracker.firebase;

import com.firebase.client.AuthData;
import com.google.android.gms.common.api.GoogleApiClient;

public class FirebaseAuthProvider {

    private static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        FirebaseAuthProvider.mGoogleApiClient = mGoogleApiClient;
    }
}
