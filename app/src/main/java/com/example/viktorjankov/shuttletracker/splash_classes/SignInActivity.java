package com.example.viktorjankov.shuttletracker.splash_classes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SignInActivity extends ActionBarActivity implements Validator.ValidationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTIVITY_TITLE = " " + "SIGN IN";
    private static final String kLOG_TAG = "SignInActivity";

    @NotEmpty
    @Email
    @InjectView(R.id.email_id)
    EditText emailEditText;
    String email;

    @InjectView(R.id.password_id)
    @NotEmpty
    EditText passwordEditText;
    String password;

    @InjectView(R.id.sign_in_id)
    Button signInButton;

    @OnClick(R.id.sign_in_id)
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

    public static final int RC_GOOGLE_LOGIN = 1;
    private GoogleApiClient mGoogleApiClient;
    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;

    @InjectView(R.id.google_plus_button)
    LinearLayout googlePlusLayout;

    @InjectView(R.id.google_icon)
    ImageButton googleIcon;

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

    private ProgressDialog mAuthProgressDialog;
    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(ACTIVITY_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.inject(this);

        validator = new Validator(this);
        validator.setValidationListener(this);

        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Flow");
        mAuthProgressDialog.setCancelable(false);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.google_login_dark));
        googleIcon.setImageDrawable(states);

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
    }

    /* *************************************
     *              FACEBOOK               *
     ***************************************/

    private void onFacebookSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            mAuthProgressDialog.show();
            mFirebase.authWithOAuthToken("facebook", session.getAccessToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    // The Facebook user is now authenticated with Firebase
                    Log.i(kLOG_TAG, "onAuthenticated");

                    String name = (String) authData.getProviderData().get("displayName");
                    String[] last = name.split("\\s+");
                    String email = (String) authData.getProviderData().get("email");
                    // check if user exists, if they do start MainActivity
                    userExists(authData.getUid(), last[0], last[1], email);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    // there was an error
                    Log.i(kLOG_TAG, "onAuthenticationError");
                }
            });
        } else if (state.isClosed()) {
        /* Logged out of Facebook so do a logout from Firebase */
            mFirebase.unauth();
        }
    }

    private void userExists(final String uid, final String first, final String last, final String email) {

        mFirebase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent;
                if (dataSnapshot.hasChildren()) {
                    mAuthProgressDialog.hide();

                    intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    mAuthProgressDialog.hide();
                    intent = new Intent(SignInActivity.this, VerifyActivity.class);

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

    /* ************************************
     *              GOOGLE                *
     **************************************/
    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(SignInActivity.this, RC_GOOGLE_LOGIN);
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
                    token = GoogleAuthUtil.getToken(SignInActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);

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
            Log.i(kLOG_TAG, provider + " auth successful");

            String name = (String) authData.getProviderData().get("displayName");
            String[] first_last = name.split("\\s+");
            String gMail = Plus.AccountApi.getAccountName(mGoogleApiClient);
            // check if user exists, if they do start MainActivity
            userExists(authData.getUid(), first_last[0], first_last[1], gMail);

        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.hide();
            Log.i(kLOG_TAG, provider + " auth not successful");
        }
    }

    @Override
    public void onValidationSucceeded() {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        mAuthProgressDialog.show();
        mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);

                mAuthProgressDialog.hide();
                startActivity(intent);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                String message;
                mAuthProgressDialog.hide();
                switch (firebaseError.getCode()) {
                    case -16:
                        message = "Incorrect Password";
                        break;
                    default:
                        message = firebaseError.toString();
                }
                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();

            String message = error.getCollatedErrorMessage(SignInActivity.this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


