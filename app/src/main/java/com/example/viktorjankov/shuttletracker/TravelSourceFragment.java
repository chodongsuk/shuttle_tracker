package com.example.viktorjankov.shuttletracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.Events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.pickup_locations.PickupLocation;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;


public class TravelSourceFragment extends Fragment {
    public static final String kLOG_TAG = "TravelSourceFragment";
    TextView mBusTextView;
    TextView mCarTextView;
    Bus bus = BusProvider.getInstance();
    PickupLocation mPickupLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_source, container, false);

        mBusTextView = (TextView) v.findViewById(R.id.travel_way_one);
        mCarTextView = (TextView) v.findViewById(R.id.travel_way_two);

        return v;
    }

    @Override
    public void onResume() {
        bus.register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }
}
