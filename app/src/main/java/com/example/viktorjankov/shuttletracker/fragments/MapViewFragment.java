package com.example.viktorjankov.shuttletracker.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class MapViewFragment extends Fragment {
    static Firebase mFirebase = FirebaseProvider.getInstance();
    Rider mRider = RiderProvider.getRider();

    private String DIRECTIONS_API_ENDPOINT = "https://maps.googleapis.com/maps/api/directions/";
    public String FIREBASE_LAT_ENDPOINT = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/latitude";
    public String FIREBASE_LNG_ENDPOINT = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/longitude";
    public String FIREBASE_ACTIVE_ENDPOINT = "companyData/" + mRider.getCompanyID() + "/riders/" + mRider.getuID() + "/active";

    @InjectView(R.id.destinationName)
    TextView destinationNameTV;
    @InjectView(R.id.destinationDuration)
    TextView destinationDurationTV;
    @InjectView(R.id.destinationProximity)
    TextView destinationProximityTV;

    @InjectView(R.id.record_button)
    ImageButton mRecordButton;

    @OnClick(R.id.record_button)
    public void onClick() {
        if (mRider.isActive()) {
            mRecordButton.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            mRecordButton.setBackground(getResources().getDrawable(R.drawable.green_play));
            mHandler.removeCallbacks(runnable);
            mRider.setActive(false);
        } else {
            mRecordButton.setImageResource(R.drawable.ic_pause_white_36dp);
            mRecordButton.setBackground(getResources().getDrawable(R.drawable.red_stop));
            mHandler.postDelayed(runnable, 5000);
            mRider.setActive(true);
        }

        mFirebase.child(FIREBASE_ACTIVE_ENDPOINT).setValue(mRider.isActive());
    }

    @InjectView(R.id.mapview)
    MapView mapView;
    GoogleMap map;

    DestinationLocation mDestinationLocation;
    TravelMode mTravelMode;

    Location mCurrentLocation;

    Bus bus = BusProvider.getInstance();
    Handler mHandler;

    DownloadTask downloadTask;
    ParserTask parserTask;
    String url;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        ButterKnife.inject(this, v);

        setRetainInstance(true);

        initMap(savedInstanceState);
        addMarkers();

        // Getting URL to the Google Directions API
        parserTask = createParserTask();
        downloadTask = createDownloadTask(parserTask);

        // Start downloading json data from Google Directions API
        url = getDirectionsUrl();
        downloadTask.execute(url);

        mHandler = new Handler();
        return v;
    }

    private ParserTask createParserTask() {
        parserTask = new ParserTask(map,destinationNameTV,
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

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()), 10);
        map.moveCamera(cameraUpdate);
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
        mCurrentLocation = location;
        if (mRider.isActive()) {
            mFirebase.child(FIREBASE_LAT_ENDPOINT).setValue(mCurrentLocation.getLatitude());
            mFirebase.child(FIREBASE_LNG_ENDPOINT).setValue(mCurrentLocation.getLongitude());
        }
    }

    public void setTravelMode(TravelMode travelMode) {
        mTravelMode = travelMode;
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
}
