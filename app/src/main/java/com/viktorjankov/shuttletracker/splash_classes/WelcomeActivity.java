package com.viktorjankov.shuttletracker.splash_classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.Session;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.viktorjankov.shuttletracker.MainActivity;
import com.viktorjankov.shuttletracker.R;
import com.viktorjankov.shuttletracker.firebase.FirebaseAuthProvider;
import com.viktorjankov.shuttletracker.firebase.RegisteredCompaniesProvider;
import com.viktorjankov.shuttletracker.model.User;
import com.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.viktorjankov.shuttletracker.singletons.UserProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class WelcomeActivity extends FragmentActivity {
    public final String kLOG_TAG = WelcomeActivity.this.getClass().getSimpleName();

    ProgressDialog mAuthProgressDialog;
    boolean locationEnabled = false;
    Firebase mFirebase = FirebaseProvider.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set content view and inject butterknife
        prepareActivity(savedInstanceState);

        // Change typeface of the two front button
        setTypeface();

        // Check phone SDK version and set the correct state selector
        setButtonsDrawables();

        // Download the registered companies from Firebase
        RegisteredCompaniesProvider.init();
    }

    private void controlFlow() {

        Log.i(kLOG_TAG, "Firebase: " + mFirebase.toString());
        AuthData authData = mFirebase.getAuth();
        if (authData != null) {

            mAuthProgressDialog.show();

            final Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            String uID = authData.getUid();

            mFirebase.child("users").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = getUserFromFirebase(dataSnapshot);
                    if (user == null) {
                        logout();
                        mAuthProgressDialog.hide();

                    }
                    else {
                        UserProvider.setUser(user);
                        intent.putExtra(MainActivity.USER_INFO, user);
                        mAuthProgressDialog.hide();

                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void setTypeface() {
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        signInButton.setTypeface(type);
        registerButton.setTypeface(type);
        appTitle.setTypeface(type);
        appTitle.setTypeface(null, Typeface.ITALIC);
    }

    private void setButtonsDrawables() {
        int resource_sign;
        int resource_reg;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            resource_sign = R.drawable.ripple_signin;
            resource_reg = R.drawable.ripple_register;
        }
        else {
            resource_sign = R.drawable.flat_pressed_signin;
            resource_reg = R.drawable.flat_pressed_register;
        }

        signInButton.setBackgroundResource(resource_sign);
        registerButton.setBackgroundResource(resource_reg);
    }

    private void checkLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            locationEnabled = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setCancelable(false);
            builder.setMessage(R.string.enabled_location);
            builder.setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            builder.show();
        }
        else {
            locationEnabled = true;
            controlFlow();
        }
    }

    private void prepareActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.inject(this);
        initProgressDialog();
    }

    @InjectView(R.id.sign_in)
    TextView signInButton;
    @InjectView(R.id.register)
    TextView registerButton;
    @InjectView(R.id.app_title)
    TextView appTitle;

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

    private User getUserFromFirebase(DataSnapshot dataSnapshot) {
        String companyCode = "";
        String email = "";
        String firstName = "";
        String lastName = "";
        String uID = "";

        for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
            Log.i(kLOG_TAG, "Key: " + userInfo.getKey());
            Log.i(kLOG_TAG, "Value: " + userInfo.getValue());
            if (userInfo.getKey().equals("companyCode")) {
                companyCode = (String) userInfo.getValue();

            }
            else if (userInfo.getKey().equals("email")) {
                email = (String) userInfo.getValue();

            }
            else if (userInfo.getKey().equals("firstName")) {
                firstName = (String) userInfo.getValue();

            }
            else if (userInfo.getKey().equals("lastName")) {

                lastName = (String) userInfo.getValue();
            }
            else if (userInfo.getKey().equals("uID")) {
                uID = (String) userInfo.getValue();
            }
        }

        if (companyCode.equals("")) {
            return null;
        }
        else {
            return new User(companyCode, email, firstName, lastName, uID);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthProgressDialog == null) {
            initProgressDialog();
        }
        if (!locationEnabled) {
            checkLocationServiceEnabled();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthProgressDialog != null) {
            mAuthProgressDialog.dismiss();
        }
    }

    private void initProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Signing in...");
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        AuthData mAuthData = mFirebase.getAuth();

        if (mAuthData != null) {
            /* logout of Firebase */
            mFirebase.unauth();
            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
             * Facebook/Google+ after logging out of Firebase. */
            if (mAuthData.getProvider().equals("facebook")) {
                /* Logout from Facebook */
                Session session = Session.getActiveSession();
                if (session != null) {
                    if (!session.isClosed()) {
                        session.closeAndClearTokenInformation();
                    }
                }
                else {
                    session = new Session(getApplicationContext());
                    Session.setActiveSession(session);
                    session.closeAndClearTokenInformation();
                }
            }
            else if (mAuthData.getProvider().equals("google")) {
                /* Logout from Google+ */
                GoogleApiClient mGoogleApiClient = FirebaseAuthProvider.getGoogleApiClient();
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }
}
