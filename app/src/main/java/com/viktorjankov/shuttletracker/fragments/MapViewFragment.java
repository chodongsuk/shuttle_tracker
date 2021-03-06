package com.viktorjankov.shuttletracker.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.viktorjankov.shuttletracker.MainActivity;
import com.viktorjankov.shuttletracker.R;
import com.viktorjankov.shuttletracker.directions.DirectionsJSONParser;
import com.viktorjankov.shuttletracker.directions.DownloadTask;
import com.viktorjankov.shuttletracker.model.DestinationLocation;
import com.viktorjankov.shuttletracker.model.Rider;
import com.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.viktorjankov.shuttletracker.singletons.RiderProvider;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapViewFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String LOCATION_UPDATES_RUNNING = "locationUpdatesRunning";
    public static int mID = 5;
    public static NotificationManager mNotificationManager;

    boolean firstRun = true;

    // Singletons
    Firebase mFirebase = FirebaseProvider.getInstance();
    Rider mRider = RiderProvider.getRider();

    // Models
    DestinationLocation mDestinationLocation;
    String mTravelMode;

    // GMS classes
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;

    // Hander and Download threads
    Handler mHandler;
    DownloadTask downloadTask;
    ParserTask parserTask;

    String url;

    Marker driverMarker;
    Marker destinationMarker;

    private boolean startLocationUpdates = false;
    private boolean locationUpdatesAlreadyRequested;

    public static MapViewFragment newInstance(Rider rider) {
        MapViewFragment mapViewFragment = new MapViewFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(RIDER_KEY, rider);

        mapViewFragment.setArguments(arguments);

        return mapViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildGoogleApiClient();
        createLocationRequest();

        mRider = (Rider) getArguments().get(RIDER_KEY);

        // Set and upload the rider to firebase
        setFirebaseEndpoints();

        mDestinationLocation = mRider.getDestinationLocation();

        mTravelMode = mRider.getTravelMode();

        double lat = mRider.getLatitude();
        double lng = mRider.getLongitude();

        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(lat);
        mCurrentLocation.setLongitude(lng);

        if (getActivity().getIntent() != null) {
            Log.i(kLOG_TAG, "Gramatik: Location Updates Already Requested: " + locationUpdatesAlreadyRequested);
            locationUpdatesAlreadyRequested = getActivity().getIntent().getBooleanExtra(LOCATION_UPDATES_RUNNING, false);
        }
        Log.i(kLOG_TAG, "Gramatik: Location Updates Already Requested After Intent: " + locationUpdatesAlreadyRequested);

        if (mNotificationManager != null && !mRider.getActive()) {
            mNotificationManager.cancelAll();
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        ButterKnife.inject(this, v);

        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        initMap(savedInstanceState);

        destinationNameTV.setText(mRider.getDestinationLocation().getDestinationName());

        // Getting URL to the Google Directions API
        parserTask = createParserTask();
        downloadTask = createDownloadTask(parserTask);

        mHandler = new Handler();

        mStartTripButton.bringToFront();

        setDriverServicingListener();
        setActiveStateListener();

        return v;
    }

    private void setDriverServicingListener() {
        String FIREBASE_SERVICING = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/serviced";
        mFirebase.child(FIREBASE_SERVICING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    boolean servicing = (boolean) dataSnapshot.getValue();

                    if (servicing && mRider.getActive()) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Driver is on the way!", Toast.LENGTH_LONG).show();
                            plotDriverLocation(true);
                        }
                    }
                    else {
                        if (driverMarker != null) {
                            driverMarker.remove();
                        }
                        plotDriverLocation(false);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void plotDriverLocation(boolean listen) {
        String FIREBASE_DRIVER_LOCATION = "companyDrivers/" + mRider.getCompanyID();

        if (listen) {
            mFirebase.child(FIREBASE_DRIVER_LOCATION).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double lat = 0;
                    double lng = 0;
                    if (dataSnapshot != null) {
                        for (DataSnapshot loc : dataSnapshot.getChildren()) {
                            if (loc.getKey().equals("lat")) {
                                lat = (double) loc.getValue();
                            }
                            else if (loc.getKey().equals("lng")) {
                                lng = (double) loc.getValue();
                            }
                        }
                    }

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    driverMarker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(mRider.getFirstName() + " to: " + mDestinationLocation.getDestinationName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.van64)));

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void setActiveStateListener() {
        String FIREBASE_SERVICING = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/active";
        mFirebase.child(FIREBASE_SERVICING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    boolean active = (boolean) dataSnapshot.getValue();
                    mRider.setActive(active);
                    Log.i(kLOG_TAG, "Gramatik: Driver is active");
                    handleActiveRider();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setFirebaseEndpoints() {
        FIREBASE_LAT_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/latitude";
        FIREBASE_LNG_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/longitude";
        FIREBASE_SERVICED_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/serviced";
        FIREBASE_ACTIVE_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/active";
        FIREBASE_DESTINATION_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/destinationName";

        FIREBASE_TIME_ENDPOINT = "companyRiders/" + mRider.getCompanyID()
                + "/" + mRider.getuID() + "/destinationTime";

        FIREBASE_PROXIMITY_ENDPOINT = "companyRiders/" + mRider.getCompanyID()
                + "/" + mRider.getuID() + "/proximity";

    }

    private ParserTask createParserTask() {
        Log.i(kLOG_TAG, "Parser Task created");
        return new ParserTask();
    }

    private DownloadTask createDownloadTask(ParserTask parserTask) {
        return new DownloadTask(map, parserTask);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            parserTask = createParserTask();
            downloadTask = createDownloadTask(parserTask);

            url = getDirectionsUrl();

            downloadTask.execute(url);
            mHandler.postDelayed(this, 20000);
        }
    };

    private void initMap(Bundle savedInstanceState) {
        MapsInitializer.initialize(this.getActivity());
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
    }

    private void addDestinationLocMarker() {
        if (map != null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            destinationMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(mDestinationLocation.getLatitude(),
                            mDestinationLocation.getLongitude()))
                    .title(mDestinationLocation.getDestinationName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private void updateCamera(boolean zoom, float zoomLevel) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()), zoomLevel);
        if (map != null) {
            if (zoom) {
                map.animateCamera(cameraUpdate);
            }
            else {
                map.moveCamera(cameraUpdate);
            }
        }
    }

    public void backPressed() {
        mRider.setActive(false);
        mRider.setServiced(false);
        mRider.setProximity(999.2);

        mFirebase.child(FIREBASE_SERVICED_ENDPOINT).setValue(mRider.getServiced());
        mFirebase.child(FIREBASE_PROXIMITY_ENDPOINT).setValue(mRider.getProximity());
        mFirebase.child(FIREBASE_ACTIVE_ENDPOINT).setValue(mRider.getActive());

        handleActiveRider();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    private String getDirectionsUrl() {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        Log.i(kLOG_TAG, "Gramatik Last Location Lat: " + mCurrentLocation.getLatitude());
        Log.i(kLOG_TAG, "Gramatik Last Location Lng: " + mCurrentLocation.getLongitude());

        mFirebase.child(FIREBASE_LAT_ENDPOINT).setValue(mCurrentLocation.getLatitude());
        mFirebase.child(FIREBASE_LNG_ENDPOINT).setValue(mCurrentLocation.getLongitude());

        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        LatLng dest = new LatLng(mDestinationLocation.getLatitude(), mDestinationLocation.getLongitude());

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Travel mode
        String localTravelMode = mTravelMode.equals("transit") ? "driving" : mTravelMode;

        Log.i(kLOG_TAG, "Travel Mode Local: " + localTravelMode);

        String travel_mode = "mode=" + localTravelMode;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + travel_mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        return DIRECTIONS_API_ENDPOINT + output + "?" + parameters;
    }

    /**
     * ***********************************
     * GMS Methods
     * ************************************
     */

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
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mFirebase.child(FIREBASE_LAT_ENDPOINT).setValue(mCurrentLocation.getLatitude());
        mFirebase.child(FIREBASE_LNG_ENDPOINT).setValue(mCurrentLocation.getLongitude());

        updateCamera(false, 14);
        if (mCurrentLocationIB != null) {
            mCurrentLocationIB.setClickable(true);
        }
        addDestinationLocMarker();

        // Start downloading json data from Google Directions API
        url = getDirectionsUrl();

        if (downloadTask != null) {
            if (downloadTask.getStatus() != AsyncTask.Status.RUNNING) {
                downloadTask.execute(url);
            }
        }

        if (mGoogleApiClient != null) {
            if (startLocationUpdates) {
//                startLocationUpdates();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.i(kLOG_TAG, "Gramatik: Got Location!");
        mFirebase.child(FIREBASE_LAT_ENDPOINT).setValue(mCurrentLocation.getLatitude());
        mFirebase.child(FIREBASE_LNG_ENDPOINT).setValue(mCurrentLocation.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * ***********************************
     * View Injections
     * ************************************
     */
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
        if (mRider.getActive()) {
            mRider.setActive(false);
            mRider.setServiced(false);
            mFirebase.child(FIREBASE_SERVICED_ENDPOINT).setValue(mRider.getServiced());
            mRider.setProximity(999.2);
            mFirebase.child(FIREBASE_PROXIMITY_ENDPOINT).setValue(mRider.getProximity());
        }
        else {
            mRider.setActive(true);
        }
        mFirebase.child(FIREBASE_ACTIVE_ENDPOINT).setValue(mRider.getActive());
    }

    private void handleActiveRider() {
        Log.i(kLOG_TAG, "Inside Handle Active Rider");
        if (isAdded()) {
            Log.i(kLOG_TAG, "Inside Added Active Rider");
            if (mRider.getActive()) {
                Log.i(kLOG_TAG, "Inside Active Rider");
//                startLocationUpdates();

                if (mStartTripButton != null && mHandler != null && isAdded()) {

                    if (getActivity() != null) {
                        Animation pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
                        pulse.setRepeatCount(Animation.INFINITE);
                        mStartTripButton.startAnimation(pulse);
                    }

                    mStartTripButton.setImageResource(R.drawable.ic_pause_white_36dp);
                    mStartTripButton.setBackground(getResources().getDrawable(R.drawable.red_stop));
                    mHandler.post(runnable);
                }

                createNotification(mRider.getFirstName() + " " + mRider.getLastName(), mRider.getDestinationLocation().getDestinationName());
            }
            else {
//                stopLocationUpdates();

                if (mStartTripButton != null && mHandler != null) {
                    mStartTripButton.clearAnimation();

                    mStartTripButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
                    mStartTripButton.setBackground(getResources().getDrawable(R.drawable.green_start));
                    mHandler.removeCallbacks(runnable);
                }

                if (MapViewFragment.mNotificationManager != null) {
                    MapViewFragment.mNotificationManager.cancelAll();
                }
            }
        }
    }

    @Override
    public void onDetach() {
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
        }
        super.onDetach();
    }

    @InjectView(R.id.mapview)
    MapView mapView;
    GoogleMap map;

    @InjectView(R.id.my_location)
    ImageButton mCurrentLocationIB;

    @OnClick(R.id.my_location)
    public void onClickButton() {
        updateCamera(true, map.getCameraPosition().zoom);
    }

    /**
     * ***********************************
     * Strings
     * ************************************
     */

    public static final String kLOG_TAG = MapViewFragment.class.getSimpleName();
    public static final String RIDER_KEY = "riderKey";

    private String DIRECTIONS_API_ENDPOINT = "https://maps.googleapis.com/maps/api/directions/";
    private String FIREBASE_LAT_ENDPOINT;
    private String FIREBASE_LNG_ENDPOINT;
    private String FIREBASE_SERVICED_ENDPOINT;
    private String FIREBASE_ACTIVE_ENDPOINT;
    private String FIREBASE_DESTINATION_ENDPOINT;
    private String FIREBASE_TIME_ENDPOINT;
    private String FIREBASE_PROXIMITY_ENDPOINT;

    /**
     * A class to parse the Google Places in JSON format
     */
    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        public final String kLOG_TAG = ParserTask.class.getSimpleName();

        public ParserTask() {
            FIREBASE_TIME_ENDPOINT = "companyRiders/" + RiderProvider.getRider().getCompanyID()

                    + "/" + RiderProvider.getRider().getuID() + "/destinationTime";

            FIREBASE_PROXIMITY_ENDPOINT = "companyRiders/" + RiderProvider.getRider().getCompanyID()
                    + "/" + RiderProvider.getRider().getuID() + "/proximity";

        }

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            String proximity = "";
            String duration = "";

            if (result.size() < 1) {
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        proximity = (String) point.get("distance");
                        continue;
                    }
                    else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
            }

            // Draw markers and polyines on map

            addDestinationLocMarker();

            String[] distanceParsed = proximity.split("\\s+");
            double rProximity = Double.parseDouble(distanceParsed[0]);
            int timeAsMins = parseTime(duration);

            if (firstRun) {
                mRider.setProximity(rProximity);

                if (mTravelMode.equals("transit")) {
                    timeAsMins += Math.round(rProximity);
                }
                mRider.setDestinationTime(timeAsMins);

                FirebaseProvider.getInstance().child(FIREBASE_TIME_ENDPOINT).setValue(timeAsMins);
                FirebaseProvider.getInstance().child(FIREBASE_PROXIMITY_ENDPOINT).setValue(rProximity);

                destinationDurationTV.setText(mRider.getDestinationTime() + " mins");
                destinationProximityTV.setText(mRider.getProximity() + " mi");
                Log.i(kLOG_TAG, "Gramatik First Run: ParserTask updating map values");
                Log.i(kLOG_TAG, "Gramatik First Run Changed Time as Int: " + mRider.getDestinationTime());
                Log.i(kLOG_TAG, "Gramatik First Run Changed Proximity: " + mRider.getProximity());
                firstRun = false;
            }
            else if (rProximity <= mRider.getProximity()) {
                mRider.setProximity(rProximity);

                if (mTravelMode.equals("transit")) {
                    timeAsMins += Math.round(rProximity);
                }
                mRider.setDestinationTime(timeAsMins);

                FirebaseProvider.getInstance().child(FIREBASE_TIME_ENDPOINT).setValue(timeAsMins);
                FirebaseProvider.getInstance().child(FIREBASE_PROXIMITY_ENDPOINT).setValue(rProximity);

                destinationDurationTV.setText(mRider.getDestinationTime() + " mins");
                destinationProximityTV.setText(mRider.getProximity() + " mi");
                Log.i(kLOG_TAG, "Gramatik: ParserTask updating map values");
                Log.i(kLOG_TAG, "Gramatik Changed Time as Int: " + mRider.getDestinationTime());
                Log.i(kLOG_TAG, "Gramatik Changed Proximity: " + mRider.getProximity());
            }
            else {
                destinationDurationTV.setText(mRider.getDestinationTime() + " mins");
                destinationProximityTV.setText(mRider.getProximity() + " mi");
                Log.i(kLOG_TAG, "Gramatik Time as Int: " + mRider.getDestinationTime());
                Log.i(kLOG_TAG, "Gramatik Proximity: " + mRider.getProximity());
            }
        }
    }

    private int parseTime(String time) {
        String[] parsedTime = time.split(" ");

        int timeAsMins = 0;
        if (parsedTime.length == 2) {
            timeAsMins = Integer.parseInt(parsedTime[0]);
        }
        else if (parsedTime.length == 4) {

            timeAsMins = Integer.parseInt(parsedTime[0]) * 60 + Integer.parseInt(parsedTime[2]);
        }
        return timeAsMins;
    }

    private void createNotification(String contentTitle, String contentText) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_drive_eta_white_24dp)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getActivity(), MainActivity.class);
        resultIntent.putExtra("mapViewFragment", "mapViewFragmentScreen");
        resultIntent.putExtra(LOCATION_UPDATES_RUNNING, true);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mID, mBuilder.build());
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent notificationIntent = new Intent(getActivity(), MainActivity.class);
        PendingIntent test = PendingIntent.getActivity(getActivity(), mID, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        if (test == null && mRider.getActive()) {
            createNotification(mRider.getFirstName() + " " + mRider.getLastName(), mRider.getDestinationLocation().getDestinationName());
        }
    }

    public void stopActiveUser() {

    }
}
