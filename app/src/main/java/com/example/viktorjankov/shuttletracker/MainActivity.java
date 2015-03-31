package com.example.viktorjankov.shuttletracker;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.events.StartRegisterEvent;
import com.example.viktorjankov.shuttletracker.events.StartSignInEvent;
import com.example.viktorjankov.shuttletracker.events.TravelModeEvent;
import com.example.viktorjankov.shuttletracker.fragments.MapViewFragment;
import com.example.viktorjankov.shuttletracker.fragments.splash.RegisterFragment;
import com.example.viktorjankov.shuttletracker.fragments.splash.SignInFragment;
import com.example.viktorjankov.shuttletracker.fragments.splash.SplashFragment;
import com.example.viktorjankov.shuttletracker.fragments.TravelModeFragment;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    FragmentManager manager;

    Bus bus = BusProvider.getInstance();
    User mUser = UserProvider.getInstance();
    Firebase mFireBaseRef = FirebaseProvider.getInstance();

    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    Location mCurrentLocation;
    MapViewFragment mapViewFragment;
    TravelModeFragment travelModeFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUser.setUserName("viktor");
        mapViewFragment = new MapViewFragment();

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new SplashFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        Firebase.setAndroidContext(this);

        User randyUser = new User("randy");
        randyUser.setActive(true);
        randyUser.setLatitude(47.6062090);
        randyUser.setLongitude(-122.3320710);
        randyUser.setDestinationTime("55");
        randyUser.setDestinationName("South Kirkland");

        User aliUser = new User("ali");
        aliUser.setActive(false);
        aliUser.setLatitude(47.6559526);
        aliUser.setLongitude(-122.3035752);
        aliUser.setDestinationTime("12");
        aliUser.setDestinationName("Bellevue TC");


        Map<String, User> users = new HashMap<String, User>();
        users.put(mUser.getUserName(), mUser);
        users.put(randyUser.getUserName(), randyUser);
        users.put(aliUser.getUserName(), aliUser);
        mFireBaseRef.setValue(users);
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e) {
        mDestinationLocation = e.getPickupLocation();
        travelModeFragment = new TravelModeFragment();

        mUser.setDestinationName(mDestinationLocation.getDestinationName());
        mFireBaseRef.child(mUser.getUserName() + "/destinationName").setValue(mUser.getDestinationName());

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

    @Subscribe
    public void handleSignInEvent(StartSignInEvent e) {
       manager.beginTransaction()
               .replace(R.id.fragmentContainer, new SignInFragment())
               .addToBackStack(null)
               .commit();
    }

    @Subscribe
    public void handleRegisterEvent(StartRegisterEvent e) {
        manager.beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
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
}

