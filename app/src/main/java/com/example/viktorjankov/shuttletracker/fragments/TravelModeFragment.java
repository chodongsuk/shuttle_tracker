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
import com.example.viktorjankov.shuttletracker.events.TravelModeEvent;
import com.example.viktorjankov.shuttletracker.model.TravelMode;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TravelModeFragment extends Fragment {
    public static final String kLOG_TAG = "TravelSourceFragment";

    @InjectView(R.id.travel_way_one) ImageButton mCarImageButton;
    TravelMode mCar = new TravelMode("driving");

    @InjectView(R.id.travel_way_two) ImageButton mBusImageButton;
    TravelMode mBussing = new TravelMode("transit");

    @InjectView(R.id.travel_way_three) ImageButton mBikeTextView;
    TravelMode mBike = new TravelMode("bicycling");

   @OnClick({R.id.travel_way_one, R.id.travel_way_two, R.id.travel_way_three})
   public void onClickListener(View v) {
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
       bus.post(new TravelModeEvent(travelMode));
   }

    Bus bus = BusProvider.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_mode, container, false);
        ButterKnife.inject(this, v);

        return v;
    }
}
