package com.example.viktorjankov.shuttletracker.splash_classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.MainActivity;
import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.firebase.FirebaseAuthProvider;
import com.example.viktorjankov.shuttletracker.firebase.RegisteredCompaniesProvider;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.facebook.Session;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VerifyActivity extends ActionBarActivity implements Validator.ValidationListener {
    public static final String ACTIVITY_TITLE = " " + "REGISTER";
    private static final String FIREBASE_USERS = "users";

    public static final String firstNameKey = "first_name";
    public static final String lastNameKey = "last_name";
    public static final String emailKey = "email";
    public static final String registeredCompaniesKey = "registeredCompaniesList";
    public static final String registeredCompaniesCodesKey = "registeredCompaniesCodesKey";
    public static final String UID_KEY = "userID";


    @InjectView(R.id.first_name)
    @NotEmpty
    EditText firstNameEditText;
    String firstName;

    @InjectView(R.id.last_name)
    @NotEmpty
    EditText lastNameEditText;
    String lastName;

    @InjectView(R.id.company_name)
    @NotEmpty
    AutoCompleteTextView companyNameAutoCompleteTextView;
    String companyName;

    @InjectView(R.id.company_code)
    @NotEmpty
    EditText companyCodeEditText;
    String companyCode;

    @InjectView(R.id.registerButton)
    Button registerButton;

    @OnClick(R.id.registerButton)
    public void onClick() {
        validator.validate();
    }
    ProgressDialog mAuthProgressDialog;
    Toolbar toolbar;

    Map<String, String> companyCodesMap = new HashMap<String, String>();
    List<String> registeredCompaniesList;
    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();
    String email;
    String uID;
    AuthData mAuthData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_layout);

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
        mAuthProgressDialog.setMessage("Registering with Flow");
        mAuthProgressDialog.setCancelable(false);

        firstName = getIntent().getExtras().getString(firstNameKey);
        lastName = getIntent().getExtras().getString(lastNameKey);
        email = getIntent().getExtras().getString(emailKey);
        uID  = getIntent().getExtras().getString(UID_KEY);

        mAuthData = FirebaseAuthProvider.getAuthData();

        registeredCompaniesList = RegisteredCompaniesProvider.getCompanyList() ;
        companyCodesMap = RegisteredCompaniesProvider.getCompanyCodesMap();


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, registeredCompaniesList);
        companyNameAutoCompleteTextView.setAdapter(adapter);


        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);

    }

    private void registerUser(final String firstName, final String lastName, final String email, String companyCode) {
        User user = new User(firstName, lastName, email, companyCode);
        mFirebase.child(FIREBASE_USERS).child(uID).setValue(user);

        Toast.makeText(VerifyActivity.this, "Congrats! You're registered!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);
    }

    private boolean validateCompanyCode(String companyName, String companyCode) {

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

        firstName = firstNameEditText.getText().toString();
        lastName = lastNameEditText.getText().toString();
        companyName = companyNameAutoCompleteTextView.getText().toString();
        companyCode = companyCodeEditText.getText().toString();

        boolean companyValid = validateCompanyCode(companyName, companyCode);
        if (companyValid) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                buildAlertDialt().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        buildAlertDialt().show();
    }

    private AlertDialog.Builder buildAlertDialt() {
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
        if (mAuthData != null) {
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
}

