package com.example.viktorjankov.shuttletracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.events.TravelModeEvent;
import com.example.viktorjankov.shuttletracker.firebase.FirebaseAuthProvider;
import com.example.viktorjankov.shuttletracker.fragments.MapViewFragment;
import com.example.viktorjankov.shuttletracker.fragments.PickupLocationFragment;
import com.example.viktorjankov.shuttletracker.fragments.TravelModeFragment;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.example.viktorjankov.shuttletracker.splash_classes.WelcomeActivity;
import com.facebook.Session;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity {
    FragmentManager manager;


    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    MapViewFragment mapViewFragment;
    TravelModeFragment travelModeFragment;

    Bus bus = BusProvider.getInstance();
    Firebase mFirebase = FirebaseProvider.getInstance();
    Rider mRider;

    User mUser;
    Company mCompany;

    protected void onCreate(Bundle savedInstanceState) {
        // content view, toolbar and title
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRider = (Rider) savedInstanceState.get(RIDER_KEY);
            RiderProvider.setRider(mRider);
        }

        if (mRider == null) {
            mRider = RiderProvider.getRider();
            RiderProvider.setRider(mRider);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText("FLOW");
        title.setTextColor(Color.WHITE);
        title.setVisibility(View.VISIBLE);

        // Get user and company data
        mUser = UserProvider.getUser();
        getCompanyData();

        // Set Ride info from company data
        mRider = RiderProvider.getRider();
        mRider.setFirstName(mUser.getFirstName());
        mRider.setuID(mFirebase.getAuth().getUid());
        mRider.setCompanyID(mUser.getCompanyCode());

        // Set and upload the rider to firebase
        setFirebaseEndpoints();

        // init the fragments
        mapViewFragment = MapViewFragment.newInstance(mRider);
        travelModeFragment = TravelModeFragment.newInstance();
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e) {
        mDestinationLocation = e.getPickupLocation();
        mRider.setDestinationLocation(mDestinationLocation);

        Log.i(kLOG_TAG, "HandlePickup Rider:" + mRider.toString());

        mFirebase.child(FIREBASE_RIDER_DESTINATION_LOCATION).setValue(mDestinationLocation);

        manager.beginTransaction()
                .replace(R.id.fragmentContainer, travelModeFragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void handleTravelModeEvent(TravelModeEvent e) {
        mTravelMode = e.getTravelSource();
        mRider.setTravelMode(mTravelMode);

        mFirebase.child(FIREBASE_RIDER_TRAVEL_MODE).setValue(mTravelMode);

        Log.i(kLOG_TAG, "HandleTravel Rider:" + mRider.toString());
        manager.beginTransaction()
                .replace(R.id.fragmentContainer, mapViewFragment)
                .addToBackStack(null)
                .commit();
    }

    private AlertDialog.Builder buildAlertDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.dialog_sign_out))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.sign_out), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        logout();
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();

                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        AuthData mAuthData = mFirebase.getAuth();

        if (mAuthData != null) {
            /* logout of Firebase */
            mFirebase.unauth();

            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
            /* Facebook/Google+ after logging out of Firebase. */
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
                GoogleApiClient googleApiClient = FirebaseAuthProvider.getGoogleApiClient();
                if (googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    FirebaseAuthProvider.setGoogleApiClient(null);
                }
            }
            Log.i(kLOG_TAG, "Provider: " + mAuthData.getProvider());
            Log.i(kLOG_TAG, "Uid: " + mAuthData.getUid());
        }
    }

    /**************************************
     *       Firebase stuff               *
     **************************************/

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
                            double lat = 0;
                            double lng = 0;
                            for (DataSnapshot individualDestination : destinations.getChildren()) {

                                Log.i(kLOG_TAG, "Destinations key: " + individualDestination.getKey());
                                Log.i(kLOG_TAG, "Destinations value: " + individualDestination.getValue());

                                if (individualDestination.getKey().equals(FIREBASE_DESTINATION_NAME)) {
                                    destinationName = individualDestination.getValue().toString();
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LAT)) {
                                    lat = Double.parseDouble(individualDestination.getValue().toString());
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LNG)) {
                                    lng = Double.parseDouble(individualDestination.getValue().toString());
                                }

                            }
                            DestinationLocation destinationLocation = new DestinationLocation(destinationName, lat, lng);
                            mCompany.addDestinationLocation(destinationLocation);
                            Log.i(kLOG_TAG, "Destination: " + destinationLocation.toString());
                        }
                    }
                }
                Log.i(kLOG_TAG, "Companies: " + mCompany.toString());
                CompanyProvider.setCompany(mCompany);
                manager = getSupportFragmentManager();
                Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
                if (fragment == null) {
                    fragment = new PickupLocationFragment();
                    manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setFirebaseEndpoints() {
        FIREBASE_RIDER_ENDPOINT = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/";
        FIREBASE_RIDER_TRAVEL_MODE = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/travelMode";
        FIREBASE_RIDER_DESTINATION_LOCATION = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/destinationLocation";

        mFirebase.child(FIREBASE_RIDER_ENDPOINT).setValue(mRider);
    }

    /**************************************
     *       Android methods              *
     **************************************/
    @Override
    protected void onResume() {
        bus.register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (fragment instanceof PickupLocationFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(RIDER_KEY, mRider);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                buildAlertDialog().show();
                return true;
            case R.id.change_password:
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String kLOG_TAG = MainActivity.class.getSimpleName();
    public static String USER_INFO = "userInfo";
    public static final String RIDER_KEY = "riderKey";

    // firebase nodes
    public static final String FIREBASE_COMPANY_CODE = "companyCode";
    public static final String FIREBASE_COMPANY_NAME = "companyName";
    public static final String FIREBASE_DESTINATIONS = "destinations";
    public static final String FIREBASE_DESTINATION_NAME = "destinationName";
    public static final String FIREBASE_DESTINATION_LAT = "latitude";
    public static final String FIREBASE_DESTINATION_LNG = "longitude";

    // firebase endpoints to store rider info
    private String FIREBASE_RIDER_ENDPOINT;
    private String FIREBASE_RIDER_TRAVEL_MODE;
    private String FIREBASE_RIDER_DESTINATION_LOCATION;
}

