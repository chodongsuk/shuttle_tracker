package com.example.viktorjankov.shuttletracker.fragments;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.directions.DownloadTask;
import com.example.viktorjankov.shuttletracker.directions.ParserTask;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapViewFragment extends Fragment implements AsyncTaskCompletionListener<List<List<HashMap<String, String>>>> {

    private static final String DIRECTIONS_API_POINT = "https://maps.googleapis.com/maps/api/directions/";
    @InjectView(R.id.header)
    TextView destination;
    @InjectView(R.id.timeToDestination)
    TextView timeToDestination;
    @InjectView(R.id.record_button)
    ImageButton mRecordButton;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.record_button)
    public void onClick() {
        if (recordState) {
            mRecordButton.setImageResource(R.drawable.ic_play_arrow_white_18dp);
            mRecordButton.setBackground(getResources().getDrawable(R.drawable.green_play));
            recordState = false;
        } else {
            mRecordButton.setImageResource(R.drawable.ic_pause_white_18dp);
            mRecordButton.setBackground(getResources().getDrawable(R.drawable.red_stop));
            recordState = true;
        }
        mCurrentLocation = new Location("dummy location");
        mCurrentLocation.setLatitude(47.6207321);
        mCurrentLocation.setLongitude(-122.3253011);
        updatePolyLine(mCurrentLocation);
    }

    @InjectView(R.id.mapview)
    MapView mapView;
    GoogleMap map;

    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    Location mCurrentLocation;
    Location mPreviousLocation;

    List<List<HashMap<String, String>>> routesList;

    private boolean recordState;

    Bus bus = BusProvider.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        ButterKnife.inject(this, v);

        setRetainInstance(true);
        recordState = false;
        destination.setText(mDestinationLocation.getDestinationName());

        MapsInitializer.initialize(this.getActivity());
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()), 10);
        map.moveCamera(cameraUpdate);


        map.addMarker(new MarkerOptions()
                .position(mDestinationLocation.getDestination())
                .title(mDestinationLocation.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        LatLng dest = mDestinationLocation.getDestination();

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest, mTravelMode);

        ParserTask parserTask = new ParserTask(map, timeToDestination, this);
        DownloadTask downloadTask = new DownloadTask(map, parserTask);

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

        return v;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, TravelMode travelMode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Travel mode
        String travel_mode = "mode=" + travelMode.getTravelMode();

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + travel_mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        return DIRECTIONS_API_POINT + output + "?" + parameters;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        bus.register(this);
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

    public void setDestination(DestinationLocation destinationLocation) {
        mDestinationLocation = destinationLocation;
    }

    public void setCurrentLocation(Location location) {
        mPreviousLocation = mCurrentLocation == null ? location : mCurrentLocation;
        mCurrentLocation = location;

        updatePolyLine(location);
    }


    public void setTravelMode(TravelMode travelMode) {
        mTravelMode = travelMode;
    }

    @Override
    public void onTaskComplete(List<List<HashMap<String, String>>> result) {
        routesList = result;
    }

    private void updatePolyLine(Location currentLocation) {
        if (routesList != null) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            // Traversing through all the routes
            for (int i = 0; i < routesList.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = routesList.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    float[] results = new float[3];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                            lat, lng, results);

                    Log.i("Root", "Distance1: " + results[0]);
//                    Log.i("Root", "Distance2: " + results[1]);
//                    Log.i("Root", "Distance3: " + results[2]);
                    if (results[0] > 100) {
                        points.add(position);
                    } else {
                        points.remove(position);
                    }
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
            }

            timeToDestination.setText(duration);

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }
}
