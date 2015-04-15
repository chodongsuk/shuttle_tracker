package com.example.viktorjankov.shuttletracker.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;

public class PickupLocationFragment extends Fragment {
    public static final String kLOG_TAG = "PickupLocationFragment";

    Bus bus = BusProvider.getInstance();
    Company mCompany;
    int tileHeight;
    RecyclerView mRecyclerView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_location, container, false);
        ButterKnife.inject(this, v);


        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCompany = CompanyProvider.getCompany();
        RecyclerView.Adapter mAdapter = new DestinationsAdapter(mCompany.getDestinationList());
        mRecyclerView.setAdapter(mAdapter);

        setTileHeight(mCompany.getDestinationList().size());

        return v;
    }

    private final View.OnClickListener mOnClickListener = new MyOnClickListener();

    public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {

        private List<DestinationLocation> dataSetList;

        public DestinationsAdapter(List<DestinationLocation> dataSet) {
            dataSetList = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.destination_layout, parent, false);
            tv.setOnClickListener(mOnClickListener);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.oneDestinationTextView.setText(dataSetList.get(position).getDestinationName());
            holder.oneDestinationTextView.setBackgroundResource(holder.colors[position % 3]);
            holder.oneDestinationTextView.setHeight(tileHeight);
        }

        @Override
        public int getItemCount() {
            return dataSetList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public int[] colors = new int[]{R.drawable.purple, R.drawable.indigo, R.drawable.blue};
            public TextView oneDestinationTextView;

            public ViewHolder(TextView v) {
                super(v);
                oneDestinationTextView = v;
            }
        }

    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = mRecyclerView.getChildAdapterPosition(v);
            bus.post(new PickupLocationEvent(mCompany.getDestinationList().get(itemPosition)));
        }
    }

    private void setTileHeight(int listSize) {
        // If there are less than three destinations split the window in that number
        // otherwise show only 3 destinations per page
        int tiles = listSize < 3 ? listSize : 3;

        // Get the height of the screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // get the height of the toolbar
        int toolbarHeight = ((MainActivity) getActivity()).getSupportActionBar().getHeight();

        // get the height of the statusBar
        int statusBarHeight = getStatusBarHeight();

        // set what the height of a tile should be
        tileHeight = (size.y - toolbarHeight - statusBarHeight) / tiles;
    }

    // get the height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onResume() {
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        super.onResume();
    }
}
