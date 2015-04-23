package com.example.viktorjankov.shuttletracker.splash_classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.firebase.FirebaseAuthProvider;
import com.example.viktorjankov.shuttletracker.firebase.RegisteredCompaniesProvider;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.facebook.Session;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class WelcomeActivity extends FragmentActivity {
    public final String kLOG_TAG = WelcomeActivity.this.getClass().getSimpleName();

    ProgressDialog mAuthProgressDialog;
    Firebase mFirebase = FirebaseProvider.getInstance();
    User mUser;
    Rider mRider;
    Company mCompany;
    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set content view and inject butterknife
        prepareActivity(savedInstanceState);

        // Change typeface of the two front button
        setTypeface();

        // Check phone SDK version and set the correct state selector
        setButtonsDrawables();

        // Download the registered companies from Firebase
        RegisteredCompaniesProvider.init();

        intent = new Intent(WelcomeActivity.this, MainActivity.class);

        AuthData authData = mFirebase.getAuth();
        if (authData != null) {
            mAuthProgressDialog.show();

            Log.i(kLOG_TAG, "Provider: " + authData.getProvider());
            Log.i(kLOG_TAG, "Uid: " + authData.getUid());

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            String uID = authData.getUid();
            mFirebase.child("users").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = getUserFromFirebase(dataSnapshot);
                    if (mUser == null) {
                        logout();
                        mAuthProgressDialog.hide();
                    } else {
                        UserProvider.setUser(mUser);
                        getRiderData();
                        getCompanyData();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void setTypeface() {
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        signInButton.setTypeface(type);
        registerButton.setTypeface(type);
    }

    private void setButtonsDrawables() {
        int resource_sign;
        int resource_reg;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            resource_sign = R.drawable.ripple_signin;
            resource_reg = R.drawable.ripple_register;
        } else {
            resource_sign = R.drawable.flat_pressed_signin;
            resource_reg = R.drawable.flat_pressed_register;
        }

        signInButton.setBackgroundResource(resource_sign);
        registerButton.setBackgroundResource(resource_reg);
    }


    private void checkLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setCancelable(false);
            builder.setMessage(R.string.enabled_location);
            builder.setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            builder.show();
        }
    }

    private void prepareActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.inject(this);
        initProgressDialog();
    }

    @InjectView(R.id.sign_in)
    TextView signInButton;
    @InjectView(R.id.register)
    TextView registerButton;

    @OnClick({R.id.sign_in, R.id.register})
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.sign_in:
                intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                break;
            case R.id.register:
                intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                break;
        }
        startActivity(intent);
    }

    private User getUserFromFirebase(DataSnapshot dataSnapshot) {
        String companyCode = "";
        String email = "";
        String firstName = "";
        String lastName = "";
        String uID = "";

        for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
            Log.i(kLOG_TAG, "Key: " + userInfo.getKey());
            Log.i(kLOG_TAG, "Value: " + userInfo.getValue());
            if (userInfo.getKey().equals("companyCode")) {
                companyCode = (String) userInfo.getValue();

            } else if (userInfo.getKey().equals("email")) {
                email = (String) userInfo.getValue();

            } else if (userInfo.getKey().equals("firstName")) {
                firstName = (String) userInfo.getValue();

            } else if (userInfo.getKey().equals("lastName")) {

                lastName = (String) userInfo.getValue();
            } else if (userInfo.getKey().equals("uID")) {
                uID = (String) userInfo.getValue();
            }
        }

        if (companyCode.equals("")) {
            return null;
        } else {
            return new User(uID, companyCode, email, firstName, lastName);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthProgressDialog == null) {
            initProgressDialog();
        }
        checkLocationServiceEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuthProgressDialog.dismiss();
    }

    private void initProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Signing in...");
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        AuthData mAuthData = mFirebase.getAuth();
        Log.i(kLOG_TAG, "Logging out!");

        if (mAuthData != null) {
            /* logout of Firebase */
            mFirebase.unauth();
            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
             * Facebook/Google+ after logging out of Firebase. */
            if (mAuthData.getProvider().equals("facebook")) {
                /* Logout from Facebook */
                Session session = Session.getActiveSession();
                if (session != null) {
                    if (!session.isClosed()) {
                        session.closeAndClearTokenInformation();
                    }
                } else {
                    session = new Session(getApplicationContext());
                    Session.setActiveSession(session);
                    session.closeAndClearTokenInformation();
                }
            } else if (mAuthData.getProvider().equals("google")) {
                /* Logout from Google+ */
                GoogleApiClient mGoogleApiClient = FirebaseAuthProvider.getGoogleApiClient();
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }

    private void getRiderData() {

        mFirebase.child("companyRiders/" + mUser.getCompanyCode() + "/" + mUser.getuID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRider = new Rider();
                for (DataSnapshot rider : dataSnapshot.getChildren()) {

                    if (rider.getKey().equals("uID")) {
                        String uID = (String) rider.getValue();
                        mRider.setuID(uID);
                    } else if (rider.getKey().equals("companyID")) {
                        String companyID = (String) rider.getValue();
                        mRider.setCompanyID(companyID);
                    } else if (rider.getKey().equals("firstName")) {
                        String firstName = (String) rider.getValue();
                        mRider.setFirstName(firstName);
                    } else if (rider.getKey().equals("proximity")) {
                        double proximity = (double) rider.getValue();
                        mRider.setProximity(proximity);
                    } else if (rider.getKey().equals("destinationTime")) {
                        String destinationTime = (String) rider.getValue();
                        mRider.setDestinationTime(destinationTime);
                    } else if (rider.getKey().equals("active")) {
                        boolean active = (boolean) rider.getValue();
                        mRider.setActive(active);
                    } else if (rider.getKey().equals("longitude")) {
                        double lng = (double) rider.getValue();
                        mRider.setLongitude(lng);
                    } else if (rider.getKey().equals("latitude")) {
                        double lat = (double) rider.getValue();
                        mRider.setLatitude(lat);
                    } else if (rider.getKey().equals("travelMode")) {
                        String travelMode = (String) rider.getValue();
                        mRider.setTravelMode(travelMode);
                    } else if (rider.getKey().equals("destinationLocation")) {
                        DestinationLocation destination = new DestinationLocation();
                        for (DataSnapshot dest : rider.getChildren()) {
                            if (dest.getKey().equals("destinationName")) {
                                String destName = (String) dest.getValue();
                                destination.setDestinationName(destName);
                            } else if (dest.getKey().equals("destinationAddress")) {
                                String destAddr = (String) dest.getValue();
                                destination.setDestinationAddress(destAddr);
                            } else if (dest.getKey().equals("latitude")) {
                                double lat = (double) dest.getValue();
                                destination.setLatitude(lat);
                            } else if (dest.getKey().equals("longitude")) {
                                double lng = (double) dest.getValue();
                                destination.setLongitude(lng);
                            }
                        }
                        mRider.setDestinationLocation(destination);
                    }
                }
                Log.i(kLOG_TAG, mRider.toString());
                Log.i(kLOG_TAG, "Rider in MainActivity: " + mRider.toString());
                RiderProvider.setRider(mRider);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getCompanyData() {
        mFirebase.child("companyData").child(mUser.getCompanyCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCompany = new Company();
                for (DataSnapshot companyData : dataSnapshot.getChildren()) {
                    if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
                        mCompany.setCompanyCode(companyData.getValue().toString());
                    } else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
                        mCompany.setCompanyName(companyData.getValue().toString());
                    } else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {

                        for (DataSnapshot destinations : companyData.getChildren()) {

                            String destinationName = "";
                            String destinationAddress = "";
                            double lat = 0;
                            double lng = 0;
                            for (DataSnapshot individualDestination : destinations.getChildren()) {

//                                Log.i(kLOG_TAG, "Destinations key: " + individualDestination.getKey());
//                                Log.i(kLOG_TAG, "Destinations value: " + individualDestination.getValue());

                                if (individualDestination.getKey().equals(FIREBASE_DESTINATION_NAME)) {
                                    destinationName = individualDestination.getValue().toString();
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LAT)) {
                                    lat = Double.parseDouble(individualDestination.getValue().toString());
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LNG)) {
                                    lng = Double.parseDouble(individualDestination.getValue().toString());
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_ADDRESS)) {
                                    destinationAddress = individualDestination.getValue().toString();
                                }

                            }
                            DestinationLocation destinationLocation = new DestinationLocation(destinationName, destinationAddress, lat, lng);
                            mCompany.addDestinationLocation(destinationLocation);
//                            Log.i(kLOG_TAG, "Destination: " + destinationLocation.toString());
                        }
                    }
                }
//                Log.i(kLOG_TAG, "Companies: " + mCompany.toString());
                CompanyProvider.setCompany(mCompany);

                intent.putExtra(MainActivity.USER_INFO, mUser);
                intent.putExtra(MainActivity.RIDER_INFO, mRider);
                intent.putExtra(MainActivity.COMPANY_INFO, mCompany);

                mAuthProgressDialog.hide();
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // firebase nodes
    public static final String FIREBASE_COMPANY_CODE = "companyCode";
    public static final String FIREBASE_COMPANY_NAME = "companyName";
    public static final String FIREBASE_DESTINATIONS = "destinations";
    public static final String FIREBASE_DESTINATION_NAME = "destinationName";
    public static final String FIREBASE_DESTINATION_ADDRESS = "destinationAddress";
    public static final String FIREBASE_DESTINATION_LAT = "latitude";
    public static final String FIREBASE_DESTINATION_LNG = "longitude";
}
