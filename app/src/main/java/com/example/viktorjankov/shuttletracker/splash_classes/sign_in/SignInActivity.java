package com.example.viktorjankov.shuttletracker.splash_classes.sign_in;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.viktorjankov.shuttletracker.R;

public class SignInActivity extends FragmentActivity {
    private static final String FRAGMENT_TITLE = " " + "SIGN IN";
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(FRAGMENT_TITLE);
        getActionBar().setIcon(R.drawable.ic_arrow_back_black_36dp);

        setContentView(R.layout.activity_welcome);

        manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new SignInFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }
}
