package com.example.viktorjankov.shuttletracker.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;

public class PickupLocationFragment extends Fragment {
    public static final String kLOG_TAG = "PickupLocationFragment";
    public static final String COMPANY_KEY = "companyKey`";

    Bus bus = BusProvider.getInstance();
    Company mCompany;
    int tileHeight;
    RecyclerView mRecyclerView;
    RelativeLayout relativeLayout;
    private boolean clickable = false;
    private final View.OnClickListener mOnClickListener = new MyOnClickListener();

    public static PickupLocationFragment newInstance(Company company) {
        PickupLocationFragment pickupLocationFragment = new PickupLocationFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(COMPANY_KEY, company);

        pickupLocationFragment.setArguments(arguments);

        return pickupLocationFragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pickup_location, container, false);
        ButterKnife.inject(this, v);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCompany = (Company) getArguments().getSerializable(COMPANY_KEY);
        setTileHeight(mCompany.getDestinationList().size());

        RecyclerView.Adapter mAdapter = new DestinationsAdapter(mCompany.getDestinationList());
        mRecyclerView.setAdapter(mAdapter);

        NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MapViewFragment.mID);

        return v;
    }


    public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {

        private List<DestinationLocation> dataSetList;

        public DestinationsAdapter(List<DestinationLocation> dataSet) {
            dataSetList = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.destination_layout, parent, false);
            if (clickable) {
                relativeLayout.setOnClickListener(mOnClickListener);
            }
            else if (RiderProvider.getRider() != null) {
                relativeLayout.setOnClickListener(mOnClickListener);

            }
            else {
                relativeLayout.setOnClickListener(null);
            }
            return new ViewHolder(relativeLayout);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            String destinationName = dataSetList.get(position).getDestinationName();
            String firstChar = String.valueOf(destinationName.charAt(0));
            String destinationAddress = dataSetList.get(position).getDestinationAddress();

            int circleColor = holder.circle_colors[position % 3];
            int arrow = holder.arrows[position % 3];
//            int backgroundDrawable = holder.backgroundDrawable[position % 3];


            holder.viewContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, tileHeight));

            holder.destinationLetterTV.setText(firstChar);

            holder.destinationLetterTV.setBackgroundResource(circleColor);

            holder.destinationNameTV.setText(destinationName);
            holder.destinationAddressTV.setText(destinationAddress);

            holder.destinationCarrotIV.setImageResource(arrow);
//            holder.viewContainer.setBackgroundResource(backgroundDrawable);
        }

        @Override
        public int getItemCount() {
            return dataSetList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public int[] circle_colors = new int[]{R.drawable.destination_list_circle_blue,
                    R.drawable.destination_list_circle_indigo,
                    R.drawable.destination_list_circle_purple};

            public int[] arrows = new int[]{R.drawable.ic_chevron_right_blue_36dp,
                    R.drawable.ic_chevron_right_indigo_36dp,
                    R.drawable.ic_chevron_right_purple_36dp};

//            public int[] backgroundDrawable = new int[] {R.drawable.blue,
//                                                         R.drawable.indigo,
//                                                         R.drawable.purple};

            public RelativeLayout viewContainer;
            public TextView destinationLetterTV;
            public TextView destinationNameTV;
            public TextView destinationAddressTV;
            public ImageView destinationCarrotIV;

            public ViewHolder(RelativeLayout v) {
                super(v);

                viewContainer = v;
                destinationLetterTV = (TextView) v.findViewById(R.id.destination_letter_id);
                destinationNameTV = (TextView) v.findViewById(R.id.destination_name_id);
                destinationAddressTV = (TextView) v.findViewById(R.id.destination_address_id);
                destinationCarrotIV = (ImageView) v.findViewById(R.id.destination_carrot_id);
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

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        if (mRecyclerView != null) {
            RecyclerView.Adapter mAdapter = new DestinationsAdapter(mCompany.getDestinationList());
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}
