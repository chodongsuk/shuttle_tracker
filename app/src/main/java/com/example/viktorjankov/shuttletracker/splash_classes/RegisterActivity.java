package com.example.viktorjankov.shuttletracker.splash_classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.firebase.FirebaseAuthProvider;
import com.example.viktorjankov.shuttletracker.firebase.RegisteredCompaniesProvider;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
import com.example.viktorjankov.shuttletracker.model.Rider;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.CompanyProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.example.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.example.viktorjankov.shuttletracker.singletons.UserProvider;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RegisterActivity extends ActionBarActivity implements Validator.ValidationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Google Variables
    public static final int RC_GOOGLE_LOGIN = 1;
    private GoogleApiClient mGoogleApiClient;
    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;

    AuthData mAuthData;
    Validator validator;
    Toolbar toolbar;
    Firebase mFirebase = FirebaseProvider.getInstance();
    ProgressDialog mAuthProgressDialog;

    User mUser;
    Rider mRider;
    Company mCompany;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(kLOG_TAG, "Firebase: " + mFirebase.toString());
        // Validator, Toolbar, Butterknife, ProgressDialog
        prepareActivity(savedInstanceState);
        intent = new Intent(RegisterActivity.this, MainActivity.class);

        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        /* Load the Facebook login button and set up the session callback */

        mFacebookLoginButton.setReadPermissions("email");
        mFacebookLoginButton.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.i(kLOG_TAG, "onFacebookSessionStateChange");
                onFacebookSessionStateChange(session, state, exception);
            }
        });

        /* *************************************
         *               GOOGLE                *
         ***************************************/
        /* Load the Google login button */

        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        FirebaseAuthProvider.setGoogleApiClient(mGoogleApiClient);

        /* *************************************
         *       GET FIREBASE COMPANIES        *
         ***************************************/

        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this,
                android.R.layout.simple_dropdown_item_1line, RegisteredCompaniesProvider.getCompanyList());
        companyNameAutoCompleteTextView.setAdapter(adapter);

    }

    /* *************************************
     *              FACEBOOK               *
     ***************************************/
    private void onFacebookSessionStateChange(Session session, SessionState state, Exception exception) {
        Log.i(kLOG_TAG, "Facebook state: " + state.toString());

        if (state.isOpened()) {
            mAuthProgressDialog.show();
            Log.i(kLOG_TAG, "state is opened");

            mFirebase.authWithOAuthToken("facebook", session.getAccessToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    // The Facebook user is now authenticated with Firebase
                    Log.i(kLOG_TAG, "onAuthenticated");

                    String name = (String) authData.getProviderData().get("displayName");
                    String[] first_last = name.split("\\s+");
                    String firstName = first_last[0];
                    String lastName = first_last[1];
                    String email = (String) authData.getProviderData().get("email");

                    // check if user exists, if they do start MainActivity
                    Log.i(kLOG_TAG, "Verifying user exists");
                    userExists(authData.getUid(), firstName, lastName, email);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    mAuthProgressDialog.hide();
                    Log.i(kLOG_TAG, "onAuthenticationError");
                }
            });
        } else if (state.isClosed()) {
        /* Logged out of Facebook so do a logout from Firebase */
            /* Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout */
            if (mAuthData != null && mAuthData.getProvider().equals("facebook")) {
                mFirebase.unauth();
            }
        }
    }

    private void userExists(final String uid, final String first, final String last, final String email) {

        mFirebase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    mUser = getUserFromFirebase(dataSnapshot);

                    if (mUser == null) {
                        mFirebase.unauth();
                        mAuthProgressDialog.hide();
                    } else {
                        UserProvider.setUser(mUser);
                        getRiderData();
                        getCompanyData();
                    }
                } else {
                    mAuthProgressDialog.hide();
                    Log.i(kLOG_TAG, "User does not exists so register");
                    Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);

                    intent.putExtra(VerifyActivity.firstNameKey, first);
                    intent.putExtra(VerifyActivity.lastNameKey, last);
                    intent.putExtra(VerifyActivity.emailKey, email);
                    intent.putExtra(VerifyActivity.UID_KEY, uid);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String companyName = companyNameAutoCompleteTextView.getText().toString();
        String companyCode = companyCodeEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (isValidCompanyCode(companyName, companyCode)) {
            registerUser(email, password, firstName, lastName, companyCode);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(RegisterActivity.this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
        }
    }

    private void registerUser(final String email, final String password, final String firstName, final String lastName, final String companyCode) {
        mAuthProgressDialog.show();
        mFirebase.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                String uID = (String) result.get("uid");
                mUser = new User(uID, companyCode.toLowerCase(), email.toLowerCase(), firstName, lastName);
                mRider = new Rider(companyCode.toLowerCase(), firstName, uID);

                UserProvider.setUser(mUser);
                RiderProvider.setRider(mRider);

                String FIREBASE_RIDER_ENDPOINT = "companyRiders/" + mRider.getCompanyID() + "/" + mRider.getuID() + "/";

                mFirebase.child(FIREBASE_USERS).child(uID).setValue(mUser);
                mFirebase.child(FIREBASE_RIDER_ENDPOINT).setValue(mRider);

                mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {

                        getCompanyData();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                String errorMessage;
                switch (firebaseError.getCode()) {
                    case -18:
                        errorMessage = "Email is already in use!";
                        break;
                    case -16:
                        errorMessage = "Invalid password!";
                        break;
                    case -15:
                        errorMessage = "The Email is invalid";
                        break;
                    default:
                        errorMessage = firebaseError.toString();
                }
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

    }

    private boolean isValidCompanyCode(String companyName, String companyCode) {

        Map<String, String> companyCodesMap = RegisteredCompaniesProvider.getCompanyCodesMap();
        String registeredCompanyCode = companyCodesMap.get(companyName);
        if (registeredCompanyCode == null) {
            companyNameAutoCompleteTextView.setError("Company name is not valid");
            return false;
        }

        if (!registeredCompanyCode.equalsIgnoreCase(companyCode)) {
            companyCodeEditText.setError("Company code is wrong");
            return false;
        }
        return true;
    }

    /* ************************************
     *              GOOGLE                *
     **************************************/
    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(RegisterActivity.this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        mAuthProgressDialog.show();
        Log.i(kLOG_TAG, "GoogleOAuthTokenAndLogin");
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(RegisterActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);

                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(kLOG_TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(kLOG_TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(kLOG_TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    Log.i(kLOG_TAG, "Successfully got OAuth from Google");
                    mFirebase.authWithOAuthToken("google", token, new AuthResultHandler("google"));
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                }
            }
        };
        task.execute();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
        getGoogleOAuthTokenAndLogin();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(kLOG_TAG, result.toString());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            mAuthProgressDialog.show();
            Log.i(kLOG_TAG, provider + " auth successful");

            String name = (String) authData.getProviderData().get("displayName");
            String[] first_last = name.split("\\s+");
            String firstName = first_last[0];
            String lastName = first_last[1];
            String gMail = Plus.AccountApi.getAccountName(mGoogleApiClient);

            userExists(authData.getUid(), firstName, lastName, gMail);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.show();
            Log.i(kLOG_TAG, provider + " auth not successful");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_LOGIN) {
            /* This was a request by the Google API */
            if (resultCode != RESULT_OK) {
                mGoogleLoginClicked = false;
            }
            mGoogleIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Session.getActiveSession()
                    .onActivityResult(this, requestCode, resultCode, data);
        }
    }

    /* *************************************
     *       HANDLE BACK PRESS             *
     ***************************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                buildAlertDialog().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        buildAlertDialog().show();
    }

    private AlertDialog.Builder buildAlertDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.dialog_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    /* *************************************
     *       Activity preparation stuff    *
     ***************************************/

    private void prepareActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        validator = new Validator(this);
        validator.setValidationListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(ACTIVITY_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.inject(this);

        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Registering with Flow");
        mAuthProgressDialog.setCancelable(false);
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

            } else if (userInfo.getKey().equals("email")) {
                email = (String) userInfo.getValue();

            } else if (userInfo.getKey().equals("firstName")) {
                firstName = (String) userInfo.getValue();

            } else if (userInfo.getKey().equals("lastName")) {

                lastName = (String) userInfo.getValue();
            } else if (userInfo.getKey().equals("uID")) {
                uID = (String) userInfo.getValue();
            }
        }

        if (companyCode.equals("")) {
            return null;
        } else {
            return new User(uID, companyCode, email, firstName, lastName);
        }
    }

    @InjectView(R.id.first_name)
    @NotEmpty
    EditText firstNameEditText;

    @InjectView(R.id.last_name)
    @NotEmpty
    EditText lastNameEditText;

    @InjectView(R.id.company_name)
    @NotEmpty
    AutoCompleteTextView companyNameAutoCompleteTextView;

    @InjectView(R.id.company_code)
    @NotEmpty
    EditText companyCodeEditText;

    @InjectView(R.id.email)
    @NotEmpty
    @Email
    EditText emailEditText;

    @InjectView(R.id.password)
    @NotEmpty
    EditText passwordEditText;

    @InjectView(R.id.registerButton)
    Button registerButton;

    @OnClick(R.id.registerButton)
    public void onClick() {
        validator.validate();
    }

    @InjectView(R.id.login_with_facebook)
    LoginButton mFacebookLoginButton;

    @InjectView(R.id.facebook_button)
    LinearLayout facebookLayout;

    @OnClick(R.id.facebook_button)
    public void facebookClick() {
        Log.i(kLOG_TAG, "performClick");
        mFacebookLoginButton.performClick();
    }

    @InjectView(R.id.google_plus_button)
    LinearLayout googlePlusLayout;

    @OnClick({R.id.google_plus_button, R.id.google_icon})
    public void googlePlusClick() {
        Log.i(kLOG_TAG, "google login button clicked");

        mGoogleLoginClicked = true;

        if (!mGoogleApiClient.isConnecting()) {
            if (mGoogleConnectionResult != null) {
                resolveSignInError();
            } else if (mGoogleApiClient.isConnected()) {
                Log.i(kLOG_TAG, "GoogleOAuthTokenAndLogin");
                getGoogleOAuthTokenAndLogin();
            } else {
                    /* connect API now */
                Log.d(kLOG_TAG, "Trying to connect to Google API");
                mGoogleApiClient.connect();
            }
        }
    }

    private final String ACTIVITY_TITLE = " " + RegisterActivity.this.getClass().getSimpleName();
    private final String kLOG_TAG = RegisterActivity.this.getClass().getSimpleName();

    private static final String FIREBASE_USERS = "users";

    private void getRiderData() {

        mFirebase.child("companyRiders/" + mUser.getCompanyCode() + "/" + mUser.getuID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRider = new Rider();
                for (DataSnapshot rider : dataSnapshot.getChildren()) {

                    if (rider.getKey().equals("uID")) {
                        String uID = (String) rider.getValue();
                        mRider.setuID(uID);
                    } else if (rider.getKey().equals("companyID")) {
                        String companyID = (String) rider.getValue();
                        mRider.setCompanyID(companyID);
                    } else if (rider.getKey().equals("firstName")) {
                        String firstName = (String) rider.getValue();
                        mRider.setFirstName(firstName);
                    } else if (rider.getKey().equals("proximity")) {
                        double proximity = (double) rider.getValue();
                        mRider.setProximity(proximity);
                    } else if (rider.getKey().equals("destinationTime")) {
                        String destinationTime = (String) rider.getValue();
                        mRider.setDestinationTime(destinationTime);
                    } else if (rider.getKey().equals("active")) {
                        boolean active = (boolean) rider.getValue();
                        mRider.setActive(active);
                    } else if (rider.getKey().equals("longitude")) {
                        double lng = (double) rider.getValue();
                        mRider.setLongitude(lng);
                    } else if (rider.getKey().equals("latitude")) {
                        double lat = (double) rider.getValue();
                        mRider.setLatitude(lat);
                    } else if (rider.getKey().equals("travelMode")) {
                        String travelMode = (String) rider.getValue();
                        mRider.setTravelMode(travelMode);
                    } else if (rider.getKey().equals("destinationLocation")) {
                        DestinationLocation destination = new DestinationLocation();
                        for (DataSnapshot dest : rider.getChildren()) {
                            if (dest.getKey().equals("destinationName")) {
                                String destName = (String) dest.getValue();
                                destination.setDestinationName(destName);
                            } else if (dest.getKey().equals("destinationAddress")) {
                                String destAddr = (String) dest.getValue();
                                destination.setDestinationAddress(destAddr);
                            } else if (dest.getKey().equals("latitude")) {
                                double lat = (double) dest.getValue();
                                destination.setLatitude(lat);
                            } else if (dest.getKey().equals("longitude")) {
                                double lng = (double) dest.getValue();
                                destination.setLongitude(lng);
                            }
                        }
                        mRider.setDestinationLocation(destination);
                    }
                }
                Log.i(kLOG_TAG, mRider.toString());
                Log.i(kLOG_TAG, "Rider in MainActivity: " + mRider.toString());
                RiderProvider.setRider(mRider);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getCompanyData() {
        mFirebase.child("companyData").child(mUser.getCompanyCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCompany = new Company();
                for (DataSnapshot companyData : dataSnapshot.getChildren()) {
                    if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
                        mCompany.setCompanyCode(companyData.getValue().toString());
                    } else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
                        mCompany.setCompanyName(companyData.getValue().toString());
                    } else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {

                        for (DataSnapshot destinations : companyData.getChildren()) {

                            String destinationName = "";
                            String destinationAddress = "";
                            double lat = 0;
                            double lng = 0;
                            for (DataSnapshot individualDestination : destinations.getChildren()) {

//                                Log.i(kLOG_TAG, "Destinations key: " + individualDestination.getKey());
//                                Log.i(kLOG_TAG, "Destinations value: " + individualDestination.getValue());

                                if (individualDestination.getKey().equals(FIREBASE_DESTINATION_NAME)) {
                                    destinationName = individualDestination.getValue().toString();
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LAT)) {
                                    lat = Double.parseDouble(individualDestination.getValue().toString());
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_LNG)) {
                                    lng = Double.parseDouble(individualDestination.getValue().toString());
                                } else if (individualDestination.getKey().equals(FIREBASE_DESTINATION_ADDRESS)) {
                                    destinationAddress = individualDestination.getValue().toString();
                                }

                            }
                            DestinationLocation destinationLocation = new DestinationLocation(destinationName, destinationAddress, lat, lng);
                            mCompany.addDestinationLocation(destinationLocation);
//                            Log.i(kLOG_TAG, "Destination: " + destinationLocation.toString());
                        }
                    }
                }
//                Log.i(kLOG_TAG, "Companies: " + mCompany.toString());
                CompanyProvider.setCompany(mCompany);

                intent.putExtra(MainActivity.USER_INFO, mUser);
                intent.putExtra(MainActivity.RIDER_INFO, mRider);
                intent.putExtra(MainActivity.COMPANY_INFO, mCompany);

                mAuthProgressDialog.hide();
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // firebase nodes
    public static final String FIREBASE_COMPANY_CODE = "companyCode";
    public static final String FIREBASE_COMPANY_NAME = "companyName";
    public static final String FIREBASE_DESTINATIONS = "destinations";
    public static final String FIREBASE_DESTINATION_NAME = "destinationName";
    public static final String FIREBASE_DESTINATION_ADDRESS = "destinationAddress";
    public static final String FIREBASE_DESTINATION_LAT = "latitude";
    public static final String FIREBASE_DESTINATION_LNG = "longitude";
}
