package com.example.viktorjankov.shuttletracker;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.Events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.pickup_locations.BellevueTC;
import com.example.viktorjankov.shuttletracker.pickup_locations.Houghton;
import com.example.viktorjankov.shuttletracker.pickup_locations.SouthKirkland;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class PickupLocationFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String kLOG_TAG = "PickupLocationFragment";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    Location mLastLocation;
    Location mCurrentLocation;

    TextView mHoughtonTextView;
    Houghton mHoughton;

    TextView mSouthKirklandTextView;
    SouthKirkland mSouthKirkland;

    TextView mBellevueTextView;
    BellevueTC mBellevue;

    Bus bus = BusProvider.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_loc, container, false);
        initTextViewsAndPickupLocations(v);

        buildGoogleApiClient();
        createLocationRequest();

        return v;
    }

    private void initTextViewsAndPickupLocations(View v) {
        mHoughton = new Houghton(47.66785, -122.18536);
        mHoughtonTextView = (TextView) v.findViewById(R.id.dest_one);
        mHoughtonTextView.setOnClickListener(onClickListener);

        mSouthKirkland = new SouthKirkland(47.64407, -122.19593);
        mSouthKirklandTextView = (TextView) v.findViewById(R.id.dest_two);
        mSouthKirklandTextView.setOnClickListener(onClickListener);

        mBellevue = new BellevueTC(47.61550, -122.19500);
        mBellevueTextView = (TextView) v.findViewById(R.id.dest_three);
        mBellevueTextView.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dest_one:
                    bus.post(new PickupLocationEvent(mHoughton));
                    break;
                case R.id.dest_two:
                    bus.post(new PickupLocationEvent(mSouthKirkland));
                    break;
                case R.id.dest_three:
                    bus.post(new PickupLocationEvent(mBellevue));
                    break;
                default:
            }
            startTravelSourceFragment();
        }
    };

    private void startTravelSourceFragment() {
        TravelSourceFragment travelSourceFragment = new TravelSourceFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, travelSourceFragment)
                .addToBackStack(null)
                .commit();
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
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onResume() {
        bus.register(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }
}
