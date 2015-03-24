package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.TravelSourceEvent;
import com.example.viktorjankov.shuttletracker.travel_mode.Bike;
import com.example.viktorjankov.shuttletracker.travel_mode.Bussing;
import com.example.viktorjankov.shuttletracker.travel_mode.Car;
import com.example.viktorjankov.shuttletracker.travel_mode.TravelMode;
import com.squareup.otto.Bus;


public class TravelModeFragment extends Fragment {
    public static final String kLOG_TAG = "TravelSourceFragment";
    ImageButton mCarTextView;
    TravelMode mCar;

    ImageButton mBusTextView;
    TravelMode mBussing;

    ImageButton mBikeTextView;
    TravelMode mBike;

    Bus bus = BusProvider.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_mode, container, false);

        mCar = new Car();
        mCarTextView = (ImageButton) v.findViewById(R.id.travel_way_two);
        mCarTextView.setOnClickListener(onClickListener);

        mBussing = new Bussing();
        mBusTextView = (ImageButton) v.findViewById(R.id.travel_way_one);
        mBusTextView.setOnClickListener(onClickListener);

        mBike = new Bike();
        mBikeTextView = (ImageButton) v.findViewById(R.id.travel_way_three);
        mBikeTextView.setOnClickListener(onClickListener);


        return v;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TravelMode travelMode = null;
            switch (v.getId()) {
                case R.id.travel_way_one:
                    travelMode = mCar;
                    break;
                case R.id.travel_way_two:
                    travelMode = mBussing;
                    break;
                case R.id.travel_way_three:
                    travelMode = mBike;
                    break;
            }
            bus.post(new TravelSourceEvent(travelMode));
        }
    };

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
