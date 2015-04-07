package com.example.viktorjankov.shuttletracker.splash_classes.sign_in;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.example.viktorjankov.shuttletracker.R;

public class SignInActivity extends ActionBarActivity {
    private static final String FRAGMENT_TITLE = " " + "SIGN IN";
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(FRAGMENT_TITLE);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_36dp);

        setContentView(R.layout.activity_welcome);

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new SignInFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }
}
