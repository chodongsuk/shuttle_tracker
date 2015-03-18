package com.example.viktorjankov.shuttletracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TravelSourceFragment extends Fragment {

    TextView mBusTextView;
    TextView mCarTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.travel_source, container, false);

        mBusTextView = (TextView) v.findViewById(R.id.travel_way_one);
        mCarTextView = (TextView) v.findViewById(R.id.travel_way_two);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
