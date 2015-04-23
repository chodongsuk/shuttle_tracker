package com.example.viktorjankov.shuttletracker.splash_classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VerifyActivity extends ActionBarActivity implements Validator.ValidationListener {

    private static final String kLOG_TAG = VerifyActivity.class.getSimpleName();
    Validator validator;
    Toolbar toolbar;
    Firebase mFirebase = FirebaseProvider.getInstance();
    ProgressDialog mAuthProgressDialog;

    private String email;
    private String uID;

    User mUser;
    Rider mRider;
    Company mCompany;
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(kLOG_TAG, "Firebase: " + mFirebase.toString());
        // Validator, Toolbar, Butterknife, ProgressDialog
        prepareActivity(savedInstanceState);

        intent = new Intent(VerifyActivity.this, MainActivity.class);

        String firstName = getIntent().getExtras().getString(firstNameKey);
        String lastName = getIntent().getExtras().getString(lastNameKey);
        email = getIntent().getExtras().getString(emailKey);
        uID = getIntent().getExtras().getString(UID_KEY);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, RegisteredCompaniesProvider.getCompanyList());
        companyNameAutoCompleteTextView.setAdapter(adapter);

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
    }

    private void registerUser(final String firstName, final String lastName, final String email, String companyCode) {
        mAuthProgressDialog.show();
        mUser = new User(uID, companyCode.toLowerCase(), email.toLowerCase(), firstName, lastName);
        Rider rider = new Rider(companyCode.toLowerCase(), firstName, uID);

        UserProvider.setUser(mUser);
        RiderProvider.setRider(rider);

        String FIREBASE_RIDER_ENDPOINT = "companyRiders/" + rider.getCompanyID() + "/" + rider.getuID() + "/";
        mFirebase.child(FIREBASE_RIDER_ENDPOINT).setValue(rider);
        mFirebase.child(FIREBASE_USERS).child(uID).setValue(mUser);

        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(MainActivity.USER_INFO, mUser);
        intent.putExtra(MainActivity.RIDER_INFO, rider);
        getCompanyData();

    }

    private boolean isValidCompanyCode(String companyName, String companyCode) {
        Map<String,String> companyCodesMap = RegisteredCompaniesProvider.getCompanyCodesMap();
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

    @Override
    public void onValidationSucceeded() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();

        String companyName = companyNameAutoCompleteTextView.getText().toString();
        String companyCode = companyCodeEditText.getText().toString();

        if (isValidCompanyCode(companyName, companyCode)) {
            registerUser(firstName, lastName, email, companyCode);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
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
                        logout();
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
                } else {
                    session = new Session(getApplicationContext());
                    Session.setActiveSession(session);
                    session.closeAndClearTokenInformation();
                }
            } else if (mAuthData.getProvider().equals("google")) {
                /* Logout from Google+ */
                GoogleApiClient mGoogleApiClient = FirebaseAuthProvider.getGoogleApiClient();
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }

    /* *************************************
     *       Activity preparation stuff    *
     ***************************************/

    private void prepareActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_layout);

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

    @InjectView(R.id.registerButton)
    Button registerButton;

    @OnClick(R.id.registerButton)
    public void onClick() {

        validator.validate();
    }

    public static final String firstNameKey = "first_name";
    public static final String lastNameKey = "last_name";
    public static final String emailKey = "email";
    public static final String UID_KEY = "userID";

    public static final String ACTIVITY_TITLE = " " + VerifyActivity.class.getSimpleName();
    private static final String FIREBASE_USERS = "users";

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

