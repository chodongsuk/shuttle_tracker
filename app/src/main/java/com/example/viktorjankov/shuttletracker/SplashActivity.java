package com.example.viktorjankov.shuttletracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.viktorjankov.shuttletracker.events.StartApplicationEvent;
import com.example.viktorjankov.shuttletracker.events.StartRegisterEvent;
import com.example.viktorjankov.shuttletracker.events.StartSignInEvent;
import com.example.viktorjankov.shuttletracker.fragments.splash.RegisterFragment;
import com.example.viktorjankov.shuttletracker.fragments.splash.SignInFragment;
import com.example.viktorjankov.shuttletracker.fragments.splash.SplashFragment;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class SplashActivity extends FragmentActivity {

    FragmentManager manager;
    Bus bus = BusProvider.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new SplashFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }


    @Subscribe
    public void handleSignInEvent(StartSignInEvent e) {
        manager.beginTransaction()
                .replace(R.id.fragmentContainer, new SignInFragment())
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void handleRegisterEvent(StartRegisterEvent e) {
        manager.beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void handleStartAppEvent(StartApplicationEvent e) {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
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
