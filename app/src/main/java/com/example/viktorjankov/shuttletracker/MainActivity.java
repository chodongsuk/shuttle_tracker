package com.example.viktorjankov.shuttletracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.example.viktorjankov.shuttletracker.fragments.MapViewFragment;
import com.example.viktorjankov.shuttletracker.fragments.PickupLocationFragment;
import com.example.viktorjankov.shuttletracker.fragments.TravelModeFragment;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.example.viktorjankov.shuttletracker.splash_classes.WelcomeActivity;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity {

    FragmentManager manager;

    ProgressDialog mAuthProgressDialog;

    DestinationLocation mDestinationLocation;
    String mTravelMode;

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

        setContentView(R.layout.activity_main);
        initProgressDialog();
        mAuthProgressDialog.show();

        setToolbarStuff();

        // Get user and company data
        mUser = UserProvider.getUser();
        if (mUser == null) {
            getUserInfo();
        }
        else {
            getCompanyData();
        }
    }

    private void setToolbarStuff() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText("FLOW");
        title.setTextColor(Color.WHITE);
        title.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e) {
        mDestinationLocation = e.getPickupLocation();
        if (mRider == null) {
            mRider = RiderProvider.getRider();
        }
        mRider.setDestinationLocation(mDestinationLocation);

        Log.i(kLOG_TAG, "HandlePickup Rider:" + mRider.toString());

        String FIREBASE_RIDER_DESTINATION_LOCATION = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/destinationLocation";
        mFirebase.child(FIREBASE_RIDER_DESTINATION_LOCATION).setValue(mDestinationLocation);

        travelModeFragment = TravelModeFragment.newInstance();
        travelModeFragment.setClickable(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, travelModeFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Subscribe
    public void handleTravelModeEvent(TravelModeEvent e) {
        mTravelMode = e.getTravelSource();
        Log.i(kLOG_TAG, "Rider is null? " + (mRider == null));
        Log.i(kLOG_TAG, "TravelMode is null? " + (mTravelMode == null));
        if (mRider == null) {
            mRider = RiderProvider.getRider();
        }
        mRider.setTravelMode(mTravelMode);

        String FIREBASE_RIDER_TRAVEL_MODE = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/travelMode";
        mFirebase.child(FIREBASE_RIDER_TRAVEL_MODE).setValue(mTravelMode);

        Log.i(kLOG_TAG, "HandleTravel Rider:" + mRider.toString());
        mapViewFragment = MapViewFragment.newInstance(mRider);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, mapViewFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
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
            Log.i(kLOG_TAG, "Provider: " + mAuthData.getProvider());
            Log.i(kLOG_TAG, "Uid: " + mAuthData.getUid());
        }
    }

    /**
     * ***********************************
     * Firebase stuff               *
     * ************************************
     */

    private void getUserInfo() {
        mFirebase.child("users/" + mFirebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = getUserFromFirebase(dataSnapshot);
                getCompanyData();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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

            }
            else if (userInfo.getKey().equals("email")) {
                email = (String) userInfo.getValue();

            }
            else if (userInfo.getKey().equals("firstName")) {
                firstName = (String) userInfo.getValue();

            }
            else if (userInfo.getKey().equals("lastName")) {

                lastName = (String) userInfo.getValue();
            }
            else if (userInfo.getKey().equals("uID")) {
                uID = (String) userInfo.getValue();
            }
        }

        if (companyCode.equals("")) {
            return null;
        }
        else {
            return new User(companyCode, email, firstName, lastName, uID);
        }
    }


    public void getCompanyData() {
        mFirebase.child("companyData").child(mUser.getCompanyCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCompany = new Company();
                for (DataSnapshot companyData : dataSnapshot.getChildren()) {
                    if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
                        mCompany.setCompanyCode(companyData.getValue().toString());
                    }
                    else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
                        mCompany.setCompanyName(companyData.getValue().toString());
                    }
                    else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {

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
                                }
                                else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LAT)) {
                                    lat = Double.parseDouble(individualDestination.getValue().toString());
                                }
                                else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LNG)) {
                                    lng = Double.parseDouble(individualDestination.getValue().toString());
                                }
                                else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_ADDRESS)) {
                                    destinationAddress = individualDestination.getValue().toString();
                                }

                            }
                            DestinationLocation destinationLocation = new DestinationLocation(destinationName, destinationAddress, lat, lng);
                            mCompany.addDestinationLocation(destinationLocation);
//                            Log.i(kLOG_TAG, "Destination: " + destinationLocation.toString());
                        }
                    }
                }
                Log.i(kLOG_TAG, "Companies: " + mCompany.toString());
                CompanyProvider.setCompany(mCompany);

                getRiderData();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
                    }
                    else if (rider.getKey().equals("companyID")) {
                        String companyID = (String) rider.getValue();
                        mRider.setCompanyID(companyID);
                    }
                    else if (rider.getKey().equals("firstName")) {
                        String firstName = (String) rider.getValue();
                        mRider.setFirstName(firstName);
                    }
                    else if (rider.getKey().equals("lastName")) {
                        String lastName = (String) rider.getValue();
                        mRider.setLastName(lastName);
                    }
                    else if (rider.getKey().equals("proximity")) {
                        double proximity = (double) rider.getValue();
                        mRider.setProximity(proximity);
                    }
                    else if (rider.getKey().equals("serviced")) {
                        boolean serviced = (boolean) rider.getValue();
                        mRider.setServiced(serviced);
                    }
                    else if (rider.getKey().equals("destinationTime")) {
                        long destinationTime = (long) rider.getValue();
                        mRider.setDestinationTime(destinationTime);
                    }
                    else if (rider.getKey().equals("active")) {
                        boolean active = (boolean) rider.getValue();
                        mRider.setActive(active);
                    }
                    else if (rider.getKey().equals("longitude")) {
                        double lng = (double) rider.getValue();
                        mRider.setLongitude(lng);
                    }
                    else if (rider.getKey().equals("latitude")) {
                        double lat = (double) rider.getValue();
                        mRider.setLatitude(lat);
                    }
                    else if (rider.getKey().equals("travelMode")) {
                        String travelMode = (String) rider.getValue();
                        mRider.setTravelMode(travelMode);
                    }
                    else if (rider.getKey().equals("destinationLocation")) {
                        DestinationLocation destination = new DestinationLocation();
                        for (DataSnapshot dest : rider.getChildren()) {
                            if (dest.getKey().equals("destinationName")) {
                                String destName = (String) dest.getValue();
                                destination.setDestinationName(destName);
                            }
                            else if (dest.getKey().equals("destinationAddress")) {
                                String destAddr = (String) dest.getValue();
                                destination.setDestinationAddress(destAddr);
                            }
                            else if (dest.getKey().equals("latitude")) {
                                double lat = (double) dest.getValue();
                                destination.setLatitude(lat);
                            }
                            else if (dest.getKey().equals("longitude")) {
                                double lng = (double) dest.getValue();
                                destination.setLongitude(lng);
                            }
                        }
                        mRider.setDestinationLocation(destination);
                    }
                }
                Log.i(kLOG_TAG, "Rider in MainActivity: " + mRider.toString());
                Log.i(kLOG_TAG, mRider.toString());

                RiderProvider.setRider(mRider);

                mapViewFragment = MapViewFragment.newInstance(mRider);
                travelModeFragment = TravelModeFragment.newInstance();

                manager = getSupportFragmentManager();

                String intentFragment = getIntent().getStringExtra("mapViewFragment");
                Log.i(kLOG_TAG, "Intent is null? " + (intentFragment == null));

                mAuthProgressDialog.hide();

                if (intentFragment != null) {
                    Log.i(kLOG_TAG, "Intent is = " + intentFragment);
                    if (intentFragment.equals("mapViewFragmentScreen")) {
                        Fragment travelFragment = TravelModeFragment.newInstance();
                        Log.i(kLOG_TAG, "I should be starting MapViewFragment");
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fragmentContainer, mapViewFragment)
                                .commitAllowingStateLoss();
                    }
                }
                else {
                    Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
                    if (fragment == null) {
                        fragment = PickupLocationFragment.newInstance(mCompany);
                        ((PickupLocationFragment) fragment).setClickable(true);
                        manager.beginTransaction().add(R.id.fragmentContainer, fragment)
                                .commitAllowingStateLoss();

                    }
                    else if (fragment instanceof TravelModeFragment) {

                        ((TravelModeFragment) fragment).setLayoutsClickable(true);
                    }
                    else if (fragment instanceof PickupLocationFragment) {
                        ((PickupLocationFragment) fragment).setClickable(true);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * ***********************************
     * Android methods
     * ************************************
     */
    @Override
    protected void onResume() {
        bus.register(this);
        if (mAuthProgressDialog == null) {
            initProgressDialog();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuthProgressDialog.dismiss();
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
        }
        else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MapViewFragment.mNotificationManager != null) {
            MapViewFragment.mNotificationManager.cancelAll();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                buildAlertDialog().show();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Signing in...");
        mAuthProgressDialog.setCancelable(false);
    }

    private String kLOG_TAG = MainActivity.class.getSimpleName();
    public static String USER_INFO = "userInfo";
    public static final String RIDER_KEY = "riderKey";

    // firebase nodes
    public static final String FIREBASE_COMPANY_CODE = "companyCode";
    public static final String FIREBASE_COMPANY_NAME = "companyName";
    public static final String FIREBASE_DESTINATIONS = "destinations";
    public static final String FIREBASE_DESTINATION_NAME = "destinationName";
    public static final String FIREBASE_DESTINATION_ADDRESS = "destinationAddress";
    public static final String FIREBASE_DESTINATION_LAT = "latitude";
    public static final String FIREBASE_DESTINATION_LNG = "longitude";

    // firebase endpoints to store rider info
    private String FIREBASE_RIDER_ENDPOINT;
}

