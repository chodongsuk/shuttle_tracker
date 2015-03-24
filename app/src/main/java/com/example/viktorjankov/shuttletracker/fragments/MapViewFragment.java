package com.example.viktorjankov.shuttletracker.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.pickup_locations.DestinationLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Bus;

public class MapViewFragment extends Fragment {

    public static final String kLOG_TAG = "MapViewFragment";
    private static final String kMARKER_HUE = "marker_hue";

    MapView mapView;
    GoogleMap map;

    DestinationLocation mDestination;
    Location mOrigin;

    Bus bus = BusProvider.getInstance();

    float mCurrentLocationMarkerColor;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        setRetainInstance(true);

        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        map = mapView.getMap();

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this.getActivity());
        map.addMarker(new MarkerOptions()
                .position(mDestination.getLatLong())
                .title(mDestination.getLocationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mOrigin.getLatitude(),
                        mOrigin.getLongitude()), 15);
        map.moveCamera(cameraUpdate);


        map.addMarker(new MarkerOptions()
                .position(new LatLng(mOrigin.getLatitude(), mOrigin.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(mCurrentLocationMarkerColor)));

        LatLng origin = new LatLng(mOrigin.getLatitude(), mOrigin.getLongitude());
        LatLng dest = mDestination.getLatLong();

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

//        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
//        downloadTask.execute(url);



        return v;
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

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    public void setDestination(DestinationLocation destinationLocation) {
        mDestination = destinationLocation;
    }

    public void setOriginLocation(Location origin) {
        mOrigin = origin;
    }
}
