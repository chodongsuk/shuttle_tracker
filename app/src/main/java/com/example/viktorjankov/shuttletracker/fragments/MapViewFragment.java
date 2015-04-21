package com.example.viktorjankov.shuttletracker.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.directions.DownloadTask;
import com.example.viktorjankov.shuttletracker.directions.ParserTask;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapViewFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Singletons
    Firebase mFirebase = FirebaseProvider.getInstance();
    Rider mRider = RiderProvider.getRider();

    // Models
    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    // GMS classes
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;

    // Hander and Download threads
    Handler mHandler;
    DownloadTask downloadTask;
    ParserTask parserTask;

    String url;

    public static MapViewFragment newInstance(Rider rider)
    {
        MapViewFragment mapViewFragment = new MapViewFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(RIDER_KEY, rider);

        mapViewFragment.setArguments(arguments);
        Log.i(kLOG_TAG, "\nPutting as Argument Rider: " + rider.toString());

        return mapViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRider = (Rider) getArguments().get(RIDER_KEY);
        Log.i(kLOG_TAG, "MapView onCreate Rider:" + mRider.toString());

        mDestinationLocation = mRider.getDestinationLocation();
        mTravelMode = mRider.getTravelMode();

        double lat = mRider.getLatitude();
        double lng = mRider.getLongitude();

        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(lat);
        mCurrentLocation.setLongitude(lng);

        buildGoogleApiClient();
        createLocationRequest();

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        ButterKnife.inject(this, v);

        initMap(savedInstanceState);

        // Getting URL to the Google Directions API
        parserTask = createParserTask();
        downloadTask = createDownloadTask(parserTask);

        mHandler = new Handler();
        return v;
    }

    private ParserTask createParserTask() {
        Log.i(kLOG_TAG, "Before parse task Rider: " + mRider.toString());
        parserTask = new ParserTask(map, mRider,
                                         destinationNameTV,
                                         destinationDurationTV,
                                         destinationProximityTV);
        return parserTask;
    }

    private DownloadTask createDownloadTask(ParserTask parserTask) {
        downloadTask = new DownloadTask(map, parserTask);
        return downloadTask;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            parserTask = createParserTask();
            downloadTask = createDownloadTask(parserTask);
            url = getDirectionsUrl();

            downloadTask.execute(url);
            mHandler.postDelayed(this, 5000);
        }
    };

    private void initMap(Bundle savedInstanceState) {
        MapsInitializer.initialize(this.getActivity());
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
    }

    private void addMarkers() {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(mDestinationLocation.getLatitude(),
                        mDestinationLocation.getLongitude()))
                .title(mDestinationLocation.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        map.addMarker(new MarkerOptions()
                .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .title(mRider.getFirstName() + " to: " + mDestinationLocation.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCamera() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()), 10);
        map.moveCamera(cameraUpdate);
    }

    private String getDirectionsUrl() {
        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        LatLng dest = new LatLng(mDestinationLocation.getLatitude(), mDestinationLocation.getLongitude());

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Travel mode
        String travel_mode = "mode=" + mTravelMode.getTravelMode();

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + travel_mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        return DIRECTIONS_API_ENDPOINT + output + "?" + parameters;
    }


    /**************************************
     *       GMS Methods                  *
     **************************************/

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateCamera();
        addMarkers();

        // Start downloading json data from Google Directions API
        url = getDirectionsUrl();
        downloadTask.execute(url);

    }

    @Override
    public void onLocationChanged(Location location) {
        updateCamera();
        mCurrentLocation = location;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**************************************
     *       View Injections              *
     **************************************/
    @InjectView(R.id.destinationName)
    TextView destinationNameTV;
    @InjectView(R.id.destinationDuration)
    TextView destinationDurationTV;
    @InjectView(R.id.destinationProximity)
    TextView destinationProximityTV;

    @InjectView(R.id.start_trip_button)
    ImageButton mStartTripButton;

    @OnClick(R.id.start_trip_button)
    public void onClick() {
        if (mRider.isActive()) {
            stopLocationUpdates();

            mStartTripButton.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            mStartTripButton.setBackground(getResources().getDrawable(R.drawable.blue_start));

            mHandler.removeCallbacks(runnable);

            mRider.setActive(false);
        } else {
            startLocationUpdates();

            mStartTripButton.setImageResource(R.drawable.ic_pause_white_36dp);
            mStartTripButton.setBackground(getResources().getDrawable(R.drawable.red_stop));

            mRider.setActive(true);

            mHandler.postDelayed(runnable, 5000);
        }

        mFirebase.child(FIREBASE_ACTIVE_ENDPOINT).setValue(mRider.isActive());
    }

    @InjectView(R.id.mapview)
    MapView mapView;
    GoogleMap map;

    /**************************************
     *           Strings                  *
     **************************************/

    public static final String kLOG_TAG = MapViewFragment.class.getSimpleName();
    public static final String RIDER_KEY = "riderKey";

    private String DIRECTIONS_API_ENDPOINT = "https://maps.googleapis.com/maps/api/directions/";
    public String FIREBASE_LAT_ENDPOINT = "companyData/" + RiderProvider.getRider().getCompanyID() + "/riders/" + RiderProvider.getRider().getuID() + "/latitude";
    public String FIREBASE_LNG_ENDPOINT = "companyData/" + RiderProvider.getRider().getCompanyID() + "/riders/" + RiderProvider.getRider().getuID() + "/longitude";
    public String FIREBASE_ACTIVE_ENDPOINT = "companyData/" + RiderProvider.getRider().getCompanyID() + "/riders/" + RiderProvider.getRider().getuID() + "/active";
}
