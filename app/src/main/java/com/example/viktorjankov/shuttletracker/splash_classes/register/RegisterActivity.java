package com.example.viktorjankov.shuttletracker.splash_classes.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.viktorjankov.shuttletracker.R;
import com.facebook.Session;

public class RegisterActivity extends FragmentActivity {
    private static final String FRAGMENT_TITLE = " " + "REGISTER";
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
            fragment = new RegisterFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession()
                .onActivityResult(this, requestCode, resultCode, data);
    }
}
