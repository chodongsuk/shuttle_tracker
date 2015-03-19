package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.BusProvider;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.TravelSourceEvent;
import com.example.viktorjankov.shuttletracker.travel_sources.Bussing;
import com.example.viktorjankov.shuttletracker.travel_sources.Car;
import com.example.viktorjankov.shuttletracker.travel_sources.TravelSource;
import com.squareup.otto.Bus;


public class TravelSourceFragment extends Fragment {
    public static final String kLOG_TAG = "TravelSourceFragment";
    TextView mCarTextView;
    TravelSource mCar;

    TextView mBusTextView;
    TravelSource mBussing;

    Bus bus = BusProvider.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_source, container, false);

        mCar = new Car();
        mCarTextView = (TextView) v.findViewById(R.id.travel_way_two);
        mCarTextView.setOnClickListener(onClickListener);

        mBussing = new Bussing();
        mBusTextView = (TextView) v.findViewById(R.id.travel_way_one);
        mBusTextView.setOnClickListener(onClickListener);


        return v;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TravelSource travelSource = null;
            switch (v.getId()) {
                case R.id.travel_way_one:
                    travelSource = mCar;
                    break;
                case R.id.travel_way_two:
                    travelSource = mBussing;
                    break;
            }
            bus.post(new TravelSourceEvent(travelSource));
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
