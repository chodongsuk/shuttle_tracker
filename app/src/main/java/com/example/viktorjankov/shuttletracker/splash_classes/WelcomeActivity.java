package com.example.viktorjankov.shuttletracker.splash_classes;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.example.viktorjankov.shuttletracker.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class WelcomeActivity extends FragmentActivity {

    @InjectView(R.id.sign_in)
    TextView signInButton;
    @InjectView(R.id.register)
    TextView registerButton;

    @OnClick({R.id.sign_in, R.id.register})
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.sign_in:
                intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                break;
            case R.id.register:
                intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.inject(this);

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        int resource_sign;
        int resource_reg;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            resource_sign = R.drawable.ripple_signin;
            resource_reg = R.drawable.ripple_register;
        } else {
            resource_sign = R.drawable.flat_pressed_signin;
            resource_reg = R.drawable.flat_pressed_register;
        }

        signInButton.setBackgroundResource(resource_sign);
        signInButton.setTypeface(type);

        registerButton.setBackgroundResource(resource_reg);
        registerButton.setTypeface(type);

    }
}
