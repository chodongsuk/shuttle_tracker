package com.example.viktorjankov.shuttletracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.example.viktorjankov.shuttletracker.splash_classes.WelcomeActivity;
import com.facebook.Session;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String USER_INFO = "userID";
    public static final String FIREBASE_COMPANY_DATA = "companyData";
    public static final String kLOG_TAG = MainActivity.class.getSimpleName();

    FragmentManager manager;

    Bus bus = BusProvider.getInstance();
    Firebase mFirebase = FirebaseProvider.getInstance();

    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    Location mCurrentLocation;
    MapViewFragment mapViewFragment;
    TravelModeFragment travelModeFragment;

    User mUser;
    Company mCompany;


    protected void onCreate(Bundle savedInstanceState) {
        // content view, toolbar and title
        prepareActivity(savedInstanceState);

        mapViewFragment = new MapViewFragment();

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new PickupLocationFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        mUser = UserProvider.getInstance();
        mFirebase.child(FIREBASE_COMPANY_DATA).child(mUser.getCompanyCode()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        }
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e) {
        mDestinationLocation = e.getPickupLocation();
        travelModeFragment = new TravelModeFragment();

        mUser.setDestinationName(mDestinationLocation.getDestinationName());
        mFirebase.child(mUser.getFirstName() + "/destinationName").setValue(mUser.getDestinationName());

        manager.beginTransaction()
                .replace(R.id.fragmentContainer, travelModeFragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void handleTravelModeEvent(TravelModeEvent e) {
        mTravelMode = e.getTravelSource();

        mapViewFragment.setDestination(mDestinationLocation);
        mapViewFragment.setCurrentLocation(mCurrentLocation);
        mapViewFragment.setTravelMode(mTravelMode);

        manager.beginTransaction()
                .replace(R.id.fragmentContainer, mapViewFragment)
                .addToBackStack(null)
                .commit();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mapViewFragment.setCurrentLocation(location);
    }

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


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (fragment instanceof PickupLocationFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

     /* *************************************
      *       Activity preparation stuff    *
      ***************************************/

    private void prepareActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText("FLOW");
        title.setTextColor(Color.WHITE);
        title.setVisibility(View.VISIBLE);

    }
}

