package com.example.viktorjankov.shuttletracker.splash_classes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.StartApplicationEvent;
import com.example.viktorjankov.shuttletracker.events.StartRegisterEvent;
import com.example.viktorjankov.shuttletracker.events.StartSignInEvent;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.splash_classes.register.RegisterActivity;
import com.example.viktorjankov.shuttletracker.splash_classes.sign_in.SignInActivity;
import com.example.viktorjankov.shuttletracker.splash_classes.welcome.WelcomeFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class WelcomeActivity extends FragmentActivity {

    FragmentManager manager;
    Bus bus = BusProvider.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new WelcomeFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }


    @Subscribe
    public void handleSignInEvent(StartSignInEvent e) {
        Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void handleRegisterEvent(StartRegisterEvent e) {
        Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void handleStartAppEvent(StartApplicationEvent e) {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
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
