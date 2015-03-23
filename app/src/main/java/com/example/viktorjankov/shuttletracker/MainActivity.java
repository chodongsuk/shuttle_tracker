package com.example.viktorjankov.shuttletracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.viktorjankov.shuttletracker.events.PickupLocationEvent;
import com.example.viktorjankov.shuttletracker.events.TravelSourceEvent;
import com.example.viktorjankov.shuttletracker.fragments.MapViewFragment;
import com.example.viktorjankov.shuttletracker.fragments.PickupLocationFragment;
import com.example.viktorjankov.shuttletracker.fragments.TravelSourceFragment;
import com.example.viktorjankov.shuttletracker.pickup_locations.BellevueTC;
import com.example.viktorjankov.shuttletracker.pickup_locations.Houghton;
import com.example.viktorjankov.shuttletracker.pickup_locations.PickupLocation;
import com.example.viktorjankov.shuttletracker.pickup_locations.SouthKirkland;
import com.example.viktorjankov.shuttletracker.travel_sources.TravelSource;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends FragmentActivity {

    FragmentManager manager;

    Bus bus = BusProvider.getInstance();
    PickupLocation mPickupLocation;
    TravelSource mTravelSource;

    Houghton mHoughton;
    SouthKirkland mSouthKirkland;
    BellevueTC mBellevue;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new PickupLocationFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e) {
        mPickupLocation = e.getPickupLocation();
        TravelSourceFragment travelSourceFragment = new TravelSourceFragment();

        manager.beginTransaction()
                .replace(R.id.fragmentContainer, travelSourceFragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void handleTravelSourceEvent(TravelSourceEvent e) {
        mTravelSource = e.getTravelSource();
        MapViewFragment mapViewFragment = new MapViewFragment();

        mapViewFragment.setPickupLocation(mPickupLocation);

        manager.beginTransaction()
                .replace(R.id.fragmentContainer, mapViewFragment)
                .addToBackStack(null)
                .commit();


    }

    @Override
    protected void onResume() {
        bus.register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }
}

