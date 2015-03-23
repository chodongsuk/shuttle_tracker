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
import com.example.viktorjankov.shuttletracker.pickup_locations.PickupLocation;
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

public class MapViewFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String kLOG_TAG = "MapViewFragment";
    private static final String kMARKER_HUE = "marker_hue";

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    Location mLastLocation;
    Location mCurrentLocation;

    MapView mapView;
    GoogleMap map;

    PickupLocation mPickupLocation;
    Bus bus = BusProvider.getInstance();

    float mCurrentLocationMarkerColor;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        setRetainInstance(true);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        map = mapView.getMap();

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this.getActivity());
        map.addMarker(new MarkerOptions()
                .position(mPickupLocation.getLatLong())
                .title(mPickupLocation.getLocationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

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


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()), 15);
        map.moveCamera(cameraUpdate);


        map.addMarker(new MarkerOptions()
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(mCurrentLocationMarkerColor)));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void setPickupLocation(PickupLocation pickupLocation) {
        mPickupLocation = pickupLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

}
