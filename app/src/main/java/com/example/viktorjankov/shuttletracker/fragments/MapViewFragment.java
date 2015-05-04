package com.example.viktorjankov.shuttletracker.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.directions.DirectionsJSONParser;
import com.example.viktorjankov.shuttletracker.directions.DownloadTask;
import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.events.TravelModeEvent;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapViewFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static int mID = 5;
    public static NotificationManager mNotificationManager;

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

    public static MapViewFragment newInstance(Rider rider) {
        MapViewFragment mapViewFragment = new MapViewFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(RIDER_KEY, rider);

        mapViewFragment.setArguments(arguments);
//        Log.i(kLOG_TAG, "\nPutting as Argument Rider: " + rider.toString());
        Log.i(kLOG_TAG, "Rider From MainActivity" + rider.toString());

        return mapViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRider = (Rider) getArguments().get(RIDER_KEY);
        Log.i(kLOG_TAG, "MapView onCreate Rider:" + mRider.toString());

        // Set and upload the rider to firebase
        setFirebaseEndpoints();

        mDestinationLocation = mRider.getDestinationLocation();

        mTravelMode = mRider.getTravelMode();

        double lat = mRider.getLatitude();
        double lng = mRider.getLongitude();

        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(lat);
        mCurrentLocation.setLongitude(lng);

        buildGoogleApiClient();
        createLocationRequest();

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        createNotification(mRider.getFirstName() + " " + mRider.getLastName(), mRider.getDestinationLocation().getDestinationName());

        setDriverServicingListener();
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

        return v;
    }

    private void setDriverServicingListener() {
        String FIREBASE_SERVICING = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/serviced";
        mFirebase.child(FIREBASE_SERVICING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    boolean servicing = (boolean) dataSnapshot.getValue();

                    if (servicing) {
                        mDriverComingTV.setVisibility(View.VISIBLE);
                        plotDriverLocation(true);
                    }
                    else {
                        mDriverComingTV.setVisibility(View.GONE);
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
                    for (DataSnapshot loc : dataSnapshot.getChildren()) {
                        if (loc.getKey().equals("lat")) {
                            lat = (double) loc.getValue();
                        }
                        else if (loc.getKey().equals("lng")) {
                            lng = (double) loc.getValue();
                        }
                    }

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    driverMarker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(mRider.getFirstName() + " to: " + mDestinationLocation.getDestinationName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
        else {
            // remove listener
        }
    }

    private void setFirebaseEndpoints() {
        FIREBASE_LAT_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/latitude";
        FIREBASE_LNG_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/longitude";
        FIREBASE_ACTIVE_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/active";
        FIREBASE_DESTINATION_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/destinationName";

        FIREBASE_TIME_ENDPOINT = "companyRiders/" + mRider.getCompanyID()
                + "/" + mRider.getuID() + "/destinationTime";

        FIREBASE_PROXIMITY_ENDPOINT = "companyRiders/" + mRider.getCompanyID()
                + "/" + mRider.getuID() + "/proximity";

    }

    private ParserTask createParserTask() {
        parserTask = new ParserTask();
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
            mHandler.postDelayed(this, 30000);
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
        map.addMarker(new MarkerOptions()
                .position(new LatLng(mDestinationLocation.getLatitude(),
                        mDestinationLocation.getLongitude()))
                .title(mDestinationLocation.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void addCurrentLocationMarker() {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .title(mRider.getFirstName() + " to: " + mDestinationLocation.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void updateCamera(boolean zoom, float zoomLevel) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()), zoomLevel);
        if (zoom) {
            map.animateCamera(cameraUpdate);
        }
        else {
            map.moveCamera(cameraUpdate);
        }
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

    private String getDirectionsUrl() {
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
     * GMS Methods                  *
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
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        Log.i(kLOG_TAG, "Gramatik: Starting location updates!");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        Log.i(kLOG_TAG, "Gramatik: Stopping location updates!");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mFirebase.child(FIREBASE_LAT_ENDPOINT).setValue(mCurrentLocation.getLatitude());
        mFirebase.child(FIREBASE_LNG_ENDPOINT).setValue(mCurrentLocation.getLongitude());

        updateCamera(false, 10);
        mCurrentLocationIB.setClickable(true);
        addDestinationLocMarker();
        addCurrentLocationMarker();

        // Start downloading json data from Google Directions API
        url = getDirectionsUrl();
        downloadTask.execute(url);

        handleActiveRider();
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
     * View Injections              *
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
        if (mRider.isActive()) {
            mRider.setActive(false);
        }
        else {
            mRider.setActive(true);
        }
        handleActiveRider();
    }

    private void handleActiveRider() {
        if (mRider.isActive()) {
            startLocationUpdates();

            Animation pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
            pulse.setRepeatCount(Animation.INFINITE);
            mStartTripButton.startAnimation(pulse);

            mStartTripButton.setImageResource(R.drawable.ic_pause_white_36dp);
            mStartTripButton.setBackground(getResources().getDrawable(R.drawable.red_stop));

            mHandler.post(runnable);
        }
        else {
            stopLocationUpdates();

            mStartTripButton.clearAnimation();

            mStartTripButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
            mStartTripButton.setBackground(getResources().getDrawable(R.drawable.green_start));

            mHandler.removeCallbacks(runnable);
        }

        mFirebase.child(FIREBASE_ACTIVE_ENDPOINT).setValue(mRider.isActive());
    }

    @Override
    public void onDetach() {
        mHandler.removeCallbacks(runnable);
        stopLocationUpdates();
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

    @InjectView(R.id.driver_coming)
    TextView mDriverComingTV;

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
    private String FIREBASE_ACTIVE_ENDPOINT;
    private String FIREBASE_DESTINATION_ENDPOINT;
    private String FIREBASE_TIME_ENDPOINT;
    private String FIREBASE_PROXIMITY_ENDPOINT;

    /**
     * A class to parse the Google Places in JSON format
     */
    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        public final String kLOG_TAG = ParserTask.class.getSimpleName();


        PolylineOptions lineOptions;

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
            lineOptions = null;
            String proximity = "";
            String duration = "";

            if (result.size() < 1) {
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

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

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
            }

            // Draw markers and polyines on map
            map.clear();
            addDestinationLocMarker();
            map.addPolyline(lineOptions);

            String rDestination = mRider.getDestinationLocation().getDestinationName();

            String[] distanceParsed = proximity.split("\\s+");
            double rProximity = Double.parseDouble(distanceParsed[0]);
            mRider.setProximity(rProximity);

            int timeAsMins = parseTime(duration);
            if (mTravelMode.equals("transit")) {
                timeAsMins += Math.round(rProximity);
            }
            mRider.setDestinationTime(timeAsMins);

            FirebaseProvider.getInstance().child(FIREBASE_TIME_ENDPOINT).setValue(timeAsMins);
            FirebaseProvider.getInstance().child(FIREBASE_PROXIMITY_ENDPOINT).setValue(rProximity);

            destinationDurationTV.setText(timeAsMins + " mins");
            destinationProximityTV.setText(String.valueOf(rProximity) + " mi");
            Log.i(kLOG_TAG, "Gramatik: ParserTask updating map values");
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
        if (test == null) {
            createNotification(mRider.getFirstName() + " " + mRider.getLastName(), mRider.getDestinationLocation().getDestinationName());
        }
    }
}
