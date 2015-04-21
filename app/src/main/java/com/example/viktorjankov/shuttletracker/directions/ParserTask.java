package com.example.viktorjankov.shuttletracker.directions;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class to parse the Google Places in JSON format
 */
public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    public static final String kLOG_TAG = ParserTask.class.getSimpleName();

    private String FIREBASE_TIME_ENDPOINT;
    private String FIREBASE_PROXIMITY_ENDPOINT;


    TextView destinationDurationTV;
    TextView destinationProximityTV;
    GoogleMap map;
    Rider mRider;

    public ParserTask(GoogleMap map, Rider rider, TextView destinationDuration, TextView destinationProximity) {
        this.map = map;
        this.mRider = rider;
        this.destinationDurationTV = destinationDuration;
        this.destinationProximityTV = destinationProximity;

        FIREBASE_TIME_ENDPOINT = "companyData/" + RiderProvider.getRider().getCompanyID()
                + "/riders/" + RiderProvider.getRider().getuID() + "/destinationTime";

        FIREBASE_PROXIMITY_ENDPOINT = "companyData/" + RiderProvider.getRider().getCompanyID()
                + "/riders/" + RiderProvider.getRider().getuID() + "/proximity";

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
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
                } else if (j == 1) { // Get duration from the list
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

        // Drawing polyline in the Google Map for the i-th route
        map.addPolyline(lineOptions);

        String rDestination = mRider.getDestinationLocation().getDestinationName();

        String[] distanceParsed = proximity.split("\\s+");
        double rProximity = Double.parseDouble(distanceParsed[0]);
        mRider.setProximity(rProximity);

        mRider.setDestinationTime(duration);

        FirebaseProvider.getInstance().child(FIREBASE_TIME_ENDPOINT).setValue(duration);
        FirebaseProvider.getInstance().child(FIREBASE_PROXIMITY_ENDPOINT).setValue(rProximity);

        destinationDurationTV.setText(duration);
        destinationProximityTV.setText(String.valueOf(rProximity) + " mi");
        Log.i(kLOG_TAG,"Gramatik: ParserTask updating map values");
    }
}
