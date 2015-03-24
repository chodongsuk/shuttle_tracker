package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.pickup_locations.BellevueTC;
import com.example.viktorjankov.shuttletracker.pickup_locations.Houghton;
import com.example.viktorjankov.shuttletracker.pickup_locations.DestinationLocation;
import com.example.viktorjankov.shuttletracker.pickup_locations.SouthKirkland;
import com.squareup.otto.Bus;

public class PickupLocationFragment extends Fragment {
    public static final String kLOG_TAG = "PickupLocationFragment";

    TextView mHoughtonTextView;
    Houghton mHoughton;

    TextView mSouthKirklandTextView;
    SouthKirkland mSouthKirkland;

    TextView mBellevueTextView;
    BellevueTC mBellevue;

    Bus bus = BusProvider.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_location, container, false);
        initTextViewsAndPickupLocations(v);

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
        DestinationLocation destinationLocation = null;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dest_one:
                    destinationLocation = mHoughton;
                    break;
                case R.id.dest_two:
                    destinationLocation = mSouthKirkland;
                    break;
                case R.id.dest_three:
                    destinationLocation = mBellevue;
                    break;
                default:
            }

            bus.post(new PickupLocationEvent(destinationLocation));
        }
    };

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
