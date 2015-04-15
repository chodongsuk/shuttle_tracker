package com.example.viktorjankov.shuttletracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;

public class PickupLocationFragment extends Fragment {
    public static final String kLOG_TAG = "PickupLocationFragment";

    private List<DestinationLocation> destinationsList;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


//    @InjectView(R.id.dest_one)
//    TextView mHoughtonTextView;
//    DestinationLocation mHoughton = new DestinationLocation("Houghton", 47.66785, -122.18536);
//
//    @InjectView(R.id.dest_two)
//    TextView mSouthKirklandTextView;
//    DestinationLocation mSouthKirkland = new DestinationLocation("South Kirkland", 47.64407, -122.19593);
//
//    @InjectView(R.id.dest_three)
//    TextView mBellevueTextView;
//    DestinationLocation mBellevue = new DestinationLocation("Bellevue TC", 47.61550, -122.19500);
//
//    @OnClick({R.id.dest_one, R.id.dest_two, R.id.dest_three})
//    public void onViewClicked(View v) {
//        DestinationLocation destinationLocation = null;
//        switch (v.getId()) {
//            case R.id.dest_one:
//                destinationLocation = mHoughton;
//                break;
//            case R.id.dest_two:
//                destinationLocation = mSouthKirkland;
//                break;
//            case R.id.dest_three:
//                destinationLocation = mBellevue;
//                break;
//
//        }
//
//        bus.post(new PickupLocationEvent(destinationLocation));
//    }

    Bus bus = BusProvider.getInstance();
    Company mCompany;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_location_recycler, container, false);
        ButterKnife.inject(this, v);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCompany = CompanyProvider.getCompany();
        mAdapter = new DestinationsAdapter(mCompany.getDestinationList());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        super.onResume();
    }

    public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {

        private List<DestinationLocation> dataSetList;


        public DestinationsAdapter(List<DestinationLocation> dataSet) {
            dataSetList = dataSet;
         }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.destination_layout, parent, false);

            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.oneDestinationTextView.setText(dataSetList.get(position).getDestinationName());
        }

        @Override
        public int getItemCount() {
            return dataSetList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView oneDestinationTextView;

            public ViewHolder(TextView v) {
                super(v);
                oneDestinationTextView = v;
            }
        }
    }
}
