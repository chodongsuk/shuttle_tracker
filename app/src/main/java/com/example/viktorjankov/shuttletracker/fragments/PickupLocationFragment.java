package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PickupLocationFragment extends Fragment {
    public static final String kLOG_TAG = "PickupLocationFragment";

    @InjectView(R.id.dest_one)
    TextView mHoughtonTextView;
    DestinationLocation mHoughton = new DestinationLocation("Houghton", 47.66785, -122.18536);

    @InjectView(R.id.dest_two)
    TextView mSouthKirklandTextView;
    DestinationLocation mSouthKirkland = new DestinationLocation("South Kirkland", 47.64407, -122.19593);

    @InjectView(R.id.dest_three)
    TextView mBellevueTextView;
    DestinationLocation mBellevue = new DestinationLocation("Bellevue TC", 47.61550, -122.19500);

    @OnClick({R.id.dest_one, R.id.dest_two, R.id.dest_three})
    public void onViewClicked(View v) {
        DestinationLocation destinationLocation = null;
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

        }

        bus.post(new PickupLocationEvent(destinationLocation));
    }

    Bus bus = BusProvider.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_location, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onResume() {
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        super.onResume();
    }
}
