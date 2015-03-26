package com.example.viktorjankov.shuttletracker.fragments;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.directions.DirectionsJSONParser;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapViewFragment extends Fragment {

    @InjectView(R.id.header) TextView destination;
    @InjectView(R.id.timeToDestination) TextView timeToDestination;
    @InjectView(R.id.record_button) ImageButton mRecordButton;

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
    }

    @InjectView(R.id.mapview) MapView mapView;
    GoogleMap map;

    DestinationLocation mDestination;
    Location mOrigin;
    TravelMode mTravelMode;

    private boolean recordState;

    Bus bus = BusProvider.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        ButterKnife.inject(this, v);
        MapsInitializer.initialize(this.getActivity());

        setRetainInstance(true);
        recordState = false;
        destination.setText(mDestination.getDestinationName());

        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        map.addMarker(new MarkerOptions()
                .position(mDestination.getDestination())
                .title(mDestination.getDestinationName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mOrigin.getLatitude(),
                        mOrigin.getLongitude()), 10);
        map.moveCamera(cameraUpdate);


        map.addMarker(new MarkerOptions()
                .position(new LatLng(mOrigin.getLatitude(), mOrigin.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        LatLng origin = new LatLng(mOrigin.getLatitude(), mOrigin.getLongitude());
        LatLng dest = mDestination.getDestination();

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest, mTravelMode);

        DownloadTask downloadTask = new DownloadTask();

//        Start downloading json data from Google Directions API
        downloadTask.execute(url);

        return v;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(map, timeToDestination);

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
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
        return  "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    public void setDestination(DestinationLocation destinationLocation) {
        mDestination = destinationLocation;
    }

    public void setOriginLocation(Location origin) {
        mOrigin = origin;
    }

    public void setTravelMode(TravelMode travelMode) {
        mTravelMode = travelMode;
    }
}
