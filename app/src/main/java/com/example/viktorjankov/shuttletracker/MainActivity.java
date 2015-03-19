package com.example.viktorjankov.shuttletracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.Events.PickupLocationEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends FragmentActivity {

    Bus bus = BusProvider.getInstance();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null)
        {
            fragment = new PickupLocationFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Subscribe
    public void handlePickupLocationEvent(PickupLocationEvent e)
    {
        Toast.makeText(this, e.getPickupLocation().getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
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

