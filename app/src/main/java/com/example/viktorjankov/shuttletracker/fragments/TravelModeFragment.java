package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.TravelModeEvent;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TravelModeFragment extends Fragment {
    public static final String kLOG_TAG = "TravelSourceFragment";

    Bus bus = BusProvider.getInstance();
    private boolean clickable = false;


    public static TravelModeFragment newInstance() {
        return new TravelModeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_mode, container, false);
        ButterKnife.inject(this, v);

        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        travelWayOneRL.setClickable(clickable);
        travelWayTwoRL.setClickable(clickable);
        travelWayThreeRL.setClickable(clickable);

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setLayoutsClickable(boolean state) {
        clickable = state;
        travelWayOneRL.setClickable(clickable);
        travelWayTwoRL.setClickable(clickable);
        travelWayThreeRL.setClickable(clickable);
    }

    String mCar = "driving";
    String mBussing = "transit";
    String mBike = "bicycling";
    @InjectView(R.id.travel_way_one)
    RelativeLayout travelWayOneRL;
    @InjectView(R.id.travel_way_two)
    RelativeLayout travelWayTwoRL;
    @InjectView(R.id.travel_way_three)
    RelativeLayout travelWayThreeRL;

    @OnClick({R.id.travel_way_one, R.id.travel_way_two, R.id.travel_way_three})
    public void onClickListener(View v) {
        String travelMode = null;
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
        BusProvider.getInstance().post(new TravelModeEvent(travelMode));
    }
}
