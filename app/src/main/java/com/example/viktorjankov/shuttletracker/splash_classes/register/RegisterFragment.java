//package com.example.viktorjankov.shuttletracker.splash_classes.register;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.IntentSender;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.example.viktorjankov.shuttletracker.R;
//import com.example.viktorjankov.shuttletracker.model.User;
//import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
//import com.facebook.Session;
//import com.facebook.SessionState;
//import com.facebook.widget.LoginButton;
//import com.firebase.client.AuthData;
//import com.firebase.client.DataSnapshot;
//import com.firebase.client.Firebase;
//import com.firebase.client.FirebaseError;
//import com.firebase.client.ValueEventListener;
//import com.google.android.gms.auth.GoogleAuthException;
//import com.google.android.gms.auth.GoogleAuthUtil;
//import com.google.android.gms.auth.UserRecoverableAuthException;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.Scopes;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.plus.Plus;
//import com.mobsandgeeks.saripaar.ValidationError;
//import com.mobsandgeeks.saripaar.Validator;
//import com.mobsandgeeks.saripaar.annotation.Email;
//import com.mobsandgeeks.saripaar.annotation.NotEmpty;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import butterknife.ButterKnife;
//import butterknife.InjectView;
//import butterknife.OnClick;
//
//public class RegisterFragment extends Fragment implements Validator.ValidationListener,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {
//    private static final String kLOG_TAG = "RegisterFragment";
//
//    private static final String FIREBASE_COMPANIES_ENDPOINT = "companies";
//    private static final String FIREBASE_COMPANY_NAME = "companyName";
//    private static final String FIREBASE_COMPANY_CODE = "companyCode";
//    private static final String FIREBASE_DESTINATIONS = "destinations";
//    private static final String FIREBASE_DESTINATION_NAME = "destinationName";
//    private static final String FIREBASE_DESTINATION_LAT = "lat";
//    private static final String FIREBASE_DESTINATION_LNG = "lng";
//    private static final String FIREBASE_REGISTERED_COMPANIES = "registeredCompanies";
//    private static final String FIREBASE_USERS = "users";
//
//    @InjectView(R.id.first_name)
//    @NotEmpty
//    EditText firstNameEditText;
//    String firstName;
//
//    @InjectView(R.id.last_name)
//    @NotEmpty
//    EditText lastNameEditText;
//    String lastName;
//
//    @InjectView(R.id.company_name)
//    @NotEmpty
//    AutoCompleteTextView companyNameAutoCompleteTextView;
//    String companyName;
//
//    @InjectView(R.id.company_code)
//    @NotEmpty
//    EditText companyCodeEditText;
//    String companyCode;
//
//    @InjectView(R.id.email)
//    @NotEmpty
//    @Email
//    EditText emailEditText;
//    String email;
//
//    @InjectView(R.id.password)
//    @NotEmpty
//    EditText passwordEditText;
//    String password;
//
//    @InjectView(R.id.registerButton)
//    Button registerButton;
//
//    @OnClick(R.id.registerButton)
//    public void onClick() {
//        validator.validate();
//
//        firstName = firstNameEditText.getText().toString();
//        lastName = lastNameEditText.getText().toString();
//        companyName = companyNameAutoCompleteTextView.getText().toString();
//        companyCode = companyCodeEditText.getText().toString();
//        email = emailEditText.getText().toString();
//        password = passwordEditText.getText().toString();
//
//        boolean companyValid = validateCompanyCode(companyName, companyCode);
//        if (companyValid) {
//            registerUser(email, password, firstName, lastName);
//        }
//    }
//
//    @InjectView(R.id.login_with_facebook)
//    LoginButton mFacebookLoginButton;
//
//    @InjectView(R.id.facebook_button)
//    LinearLayout facebookLayout;
//
//    @OnClick(R.id.facebook_button)
//    public void facebookClick() {
//        Log.i(kLOG_TAG, "performClick");
//        mFacebookLoginButton.performClick();
//    }
//
//    public static final int RC_GOOGLE_LOGIN = 1;
//    private GoogleApiClient mGoogleApiClient;
//    private boolean mGoogleIntentInProgress;
//    private boolean mGoogleLoginClicked;
//    private ConnectionResult mGoogleConnectionResult;
//
//    @InjectView(R.id.google_plus_button)
//    LinearLayout googlePlusLayout;
//
//    @OnClick({R.id.google_plus_button, R.id.google_icon})
//    public void googlePlusClick() {
//        Log.i(kLOG_TAG, "google login button clicked");
//        mGoogleLoginClicked = true;
//        if (!mGoogleApiClient.isConnecting()) {
//            if (mGoogleConnectionResult != null) {
//                resolveSignInError();
//            } else if (mGoogleApiClient.isConnected()) {
//                Log.i(kLOG_TAG, "GoogleOAuthTokenAndLogin");
//                getGoogleOAuthTokenAndLogin();
//            } else {
//                    /* connect API now */
//                Log.d(kLOG_TAG, "Trying to connect to Google API");
//                mGoogleApiClient.connect();
//            }
//        };
//    }
//
//
//    Validator validator;
//    Firebase mFirebase = FirebaseProvider.getInstance();
//    Map<String, String> companyCodesMap = new HashMap<String, String>();
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.register_layout, container, false);
//        ButterKnife.inject(this, v);
//
//        validator = new Validator(this);
//        validator.setValidationListener(this);
//
//        /* *************************************
//         *              FACEBOOK               *
//         ***************************************/
//        /* Load the Facebook login button and set up the session callback */
//
//        mFacebookLoginButton.setReadPermissions("email");
//        mFacebookLoginButton.setSessionStatusCallback(new Session.StatusCallback() {
//            @Override
//            public void call(Session session, SessionState state, Exception exception) {
//                Log.i(kLOG_TAG, "onFacebookSessionStateChange");
//                onFacebookSessionStateChange(session, state, exception);
//            }
//        });
//
//        /* *************************************
//         *               GOOGLE                *
//         ***************************************/
//        /* Load the Google login button */
//
//        /* Setup the Google API object to allow Google+ logins */
//        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_LOGIN)
//                .build();
//
//        /* *************************************
//         *       GET FIREBASE COMPANIES        *
//         ***************************************/
//        /* Load the Google login button */
//
//        mFirebase.child(FIREBASE_REGISTERED_COMPANIES).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                List<String> registeredCompanyList = new ArrayList<String>();
//                for (DataSnapshot company : dataSnapshot.getChildren()) {
//                    String companyName = "";
//                    String companyCode = "";
//                    for (DataSnapshot companyValues : company.getChildren()) {
//                        if (companyValues.getKey().equals("companyCode")) {
//                            companyCode = companyValues.getValue().toString();
//                        }
//                        if (companyValues.getKey().equals("companyName")) {
//                            companyName = companyValues.getValue().toString();
//                            registeredCompanyList.add(companyName);
//                        }
//                    }
//                    companyCodesMap.put(companyName, companyCode);
//                }
//
//                if (getActivity() != null) {
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
//                            android.R.layout.simple_dropdown_item_1line, registeredCompanyList);
//                    companyNameAutoCompleteTextView.setAdapter(adapter);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//
//        return v;
//    }
//
//    private void onFacebookSessionStateChange(Session session, SessionState state, Exception exception) {
//        Log.i(kLOG_TAG, "ON FACEBOOK SESSION STATE CHANGE");
//        if (state.isOpened()) {
//            Log.i(kLOG_TAG, "state is opened");
//            mFirebase.authWithOAuthToken("facebook", session.getAccessToken(), new Firebase.AuthResultHandler() {
//                @Override
//                public void onAuthenticated(AuthData authData) {
//                    // The Facebook user is now authenticated with Firebase
//                    Toast.makeText(getActivity(), "FACEBOOK AUTHENTICION SUCCESSFULL", Toast.LENGTH_LONG).show();
//                    Log.i(kLOG_TAG, "onAuthenticated");
//                }
//
//                @Override
//                public void onAuthenticationError(FirebaseError firebaseError) {
//                    // there was an error
//                    Toast.makeText(getActivity(), "FACEBOOK AUTHENTICION NOT SUCCESSFULL :(", Toast.LENGTH_LONG).show();
//                    Log.i(kLOG_TAG, "onAuthenticationError");
//                }
//            });
//        } else if (state.isClosed()) {
//        /* Logged out of Facebook so do a logout from Firebase */
//            Log.i(kLOG_TAG, "ON FACEBOOK SESSION STATE CHANGE");
//            mFirebase.unauth();
//        }
//        Log.i(kLOG_TAG, state.toString());
//
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    public void onValidationSucceeded() {
//
//    }
//
//    @Override
//    public void onValidationFailed(List<ValidationError> errors) {
//        for (ValidationError error : errors) {
//            View view = error.getView();
//            String message = error.getCollatedErrorMessage(getActivity());
//
//            if (view instanceof EditText) {
//                ((EditText) view).setError(message);
//            } else {
//                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    private void registerUser(final String email, String password, final String firstName, final String lastName) {
//        mFirebase.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
//            @Override
//            public void onSuccess(Map<String, Object> result) {
//                User user = new User(firstName, lastName, email, companyCode);
//                Map<Object, Object> oneUserMap = new HashMap<Object, Object>();
//                oneUserMap.put(result.get("uid"), user);
//                mFirebase.child(FIREBASE_USERS).setValue(oneUserMap);
//                Toast.makeText(getActivity(), "Congrats! You're registered!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(FirebaseError firebaseError) {
//                String errorMessage;
//                switch (firebaseError.getCode()) {
//                    case -18:
//                        errorMessage = "Email is already in use!";
//                        break;
//                    case -16:
//                        errorMessage = "Invalid password!";
//                        break;
//                    default:
//                        errorMessage = firebaseError.toString();
//                }
//                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }
//
//    private boolean validateCompanyCode(String companyName, String companyCode) {
//
//        String registeredCompanyCode = companyCodesMap.get(companyName);
//        if (registeredCompanyCode == null) {
//            companyNameAutoCompleteTextView.setError("Company name is not valid");
//            return false;
//        }
//
//        if (!registeredCompanyCode.equalsIgnoreCase(companyCode)) {
//            companyCodeEditText.setError("Company code is wrong");
//            return false;
//        }
//        return true;
//    }
//
//    /* ************************************
//     *              GOOGLE                *
//     **************************************
//     */
//    /* A helper method to resolve the current ConnectionResult error. */
//    private void resolveSignInError() {
//        if (mGoogleConnectionResult.hasResolution()) {
//            try {
//                mGoogleIntentInProgress = true;
//                mGoogleConnectionResult.startResolutionForResult(getActivity(), RC_GOOGLE_LOGIN);
//            } catch (IntentSender.SendIntentException e) {
//                // The intent was canceled before it was sent.  Return to the default
//                // state and attempt to connect to get an updated ConnectionResult.
//                mGoogleIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
//        }
//    }
//
//    private void getGoogleOAuthTokenAndLogin() {
//        Log.i(kLOG_TAG, "GoogleOAuthTokenAndLogin");
//        mAuthProgressDialog.show();
//        /* Get OAuth token in Background */
//        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
//            String errorMessage = null;
//
//            @Override
//            protected String doInBackground(Void... params) {
//                String token = null;
//
//                try {
//                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
//                    token = GoogleAuthUtil.getToken(getActivity(), Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
//                } catch (IOException transientEx) {
//                    /* Network or server error */
//                    Log.e(kLOG_TAG, "Error authenticating with Google: " + transientEx);
//                    errorMessage = "Network error: " + transientEx.getMessage();
//                } catch (UserRecoverableAuthException e) {
//                    Log.w(kLOG_TAG, "Recoverable Google OAuth error: " + e.toString());
//                    /* We probably need to ask for permissions, so start the intent if there is none pending */
//                    if (!mGoogleIntentInProgress) {
//                        mGoogleIntentInProgress = true;
//                        Intent recover = e.getIntent();
//                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
//                    }
//                } catch (GoogleAuthException authEx) {
//                    /* The call is not ever expected to succeed assuming you have already verified that
//                     * Google Play services is installed. */
//                    Log.e(kLOG_TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
//                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
//                }
//                return token;
//            }
//
//            @Override
//            protected void onPostExecute(String token) {
//                mGoogleLoginClicked = false;
//                if (token != null) {
//                    /* Successfully got OAuth token, now login with Google */
//                    Log.i(kLOG_TAG, "Successfully got OAuth from Google");
//                    mFirebase.authWithOAuthToken("google", token, new AuthResultHandler("google"));
//                } else if (errorMessage != null) {
//                    mAuthProgressDialog.hide();
//                    Toast.makeText(getActivity(), "GOOGLE AUTHENTICION NOT SUCCESSFULL :(", Toast.LENGTH_LONG).show();
//                }
//            }
//
//
//        };
//        task.execute();
//    }
//
//    @Override
//    public void onConnected(final Bundle bundle) {
//        /* Connected with Google API, use this to authenticate with Firebase */
//        getGoogleOAuthTokenAndLogin();
//    }
//
//
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        if (!mGoogleIntentInProgress) {
//            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
//            mGoogleConnectionResult = result;
//
//            if (mGoogleLoginClicked) {
//                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
//                 * or they cancel. */
//                resolveSignInError();
//            } else {
//                Log.e(kLOG_TAG, result.toString());
//            }
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        // ignore
//    }
//
//    /**
//     * Utility class for authentication results
//     */
//    private class AuthResultHandler implements Firebase.AuthResultHandler {
//
//        private final String provider;
//
//        public AuthResultHandler(String provider) {
//            this.provider = provider;
//        }
//
//        @Override
//        public void onAuthenticated(AuthData authData) {
//            Log.i(kLOG_TAG, provider + " auth successful");
//            Toast.makeText(getActivity(), "GOOGLE AUTHENTICION SUCCESSFULL :)", Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onAuthenticationError(FirebaseError firebaseError) {
//            Log.i(kLOG_TAG, provider + " auth not successful");
//            Toast.makeText(getActivity(), "GOOGLE AUTHENTICION NOT SUCCESSFULL :(", Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//    //    private void parseCompany() {
////        mFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                for (DataSnapshot companies : dataSnapshot.getChildren()) {
////                    Company company = new Company();
////                    for (DataSnapshot companyData : companies.getChildren()) {
////                        if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
////                            company.setCompanyCode(companyData.getValue().toString());
////                        } else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
////                            company.setCompanyName(companyData.getValue().toString());
////                        } else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {
////
////                            for (DataSnapshot destinations : companyData.getChildren()) {
////                                String destinationName = "";
////                                double lat = 0;
////                                double lng = 0;
////                                if (destinations.getKey().equals(FIREBASE_DESTINATION_NAME)) {
////                                    destinationName = destinations.getValue().toString();
////                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LAT)) {
////                                    lat = Double.parseDouble(destinations.getValue().toString());
////                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LNG)) {
////                                    lng = Double.parseDouble(destinations.getValue().toString());
////                                }
////                                DestinationLocation destinationLocation = new DestinationLocation(destinationName, lat, lng);
////                                company.addDestinationLocation(destinationLocation);
////                            }
////                        }
////                    }
////                    companyList.add(company);
////                }
////
////                for(Company company : companyList) {
////                    Log.i("RegisterFragment", company.toString());
////                }
////            }
////    }
//}
