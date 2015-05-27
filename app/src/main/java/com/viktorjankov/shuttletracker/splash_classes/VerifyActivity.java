package com.viktorjankov.shuttletracker.splash_classes;

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
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.viktorjankov.shuttletracker.MainActivity;
import com.viktorjankov.shuttletracker.R;
import com.viktorjankov.shuttletracker.firebase.RegisteredCompaniesProvider;
import com.viktorjankov.shuttletracker.model.Rider;
import com.viktorjankov.shuttletracker.model.User;
import com.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.viktorjankov.shuttletracker.singletons.RiderProvider;
import com.viktorjankov.shuttletracker.singletons.UserProvider;

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

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(kLOG_TAG, "Firebase: " + mFirebase.toString());
        // Validator, Toolbar, Butterknife, ProgressDialog
        prepareActivity(savedInstanceState);

        String firstName = getIntent().getExtras().getString(firstNameKey);
        String lastName = getIntent().getExtras().getString(lastNameKey);
        email = getIntent().getExtras().getString(emailKey);
        uID = getIntent().getExtras().getString(UID_KEY);

        /* *************************************
        *       Root Specific
        ***************************************/
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, RegisteredCompaniesProvider.getCompanyList());
//        companyNameAutoCompleteTextView.setAdapter(adapter);

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
    }

    private void registerUser(final String firstName, final String lastName, final String email, String companyID) {
        Log.i(kLOG_TAG, "Verify Gramatik Register User: " + firstName + " " + lastName + " " + email + " " + companyID);
        User user = new User(companyID.toLowerCase(), email.toLowerCase(), firstName, lastName, uID);
        Rider rider = new Rider(firstName, lastName, uID, companyID.toLowerCase(), false, false);

        String FIREBASE_RIDER_ENDPOINT = "companyRiders/" + rider.getCompanyID() + "/" + rider.getuID();
        String FIREBASE_USER_ENDPOINT = "users/" + uID;

        mFirebase.child(FIREBASE_RIDER_ENDPOINT).setValue(rider);
        mFirebase.child(FIREBASE_USER_ENDPOINT).setValue(user);

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        UserProvider.setUser(user);
        RiderProvider.setRider(rider);

        startActivity(intent);
    }

    private boolean isValidCompanyCode(String companyName, String companyCode) {
        Map<String, String> companyCodesMap = RegisteredCompaniesProvider.getCompanyCodesMap();
        String registeredCompanyCode = companyCodesMap.get(companyName);

        Log.i(kLOG_TAG, "Verify Gramatik Registered CompanyCode: " + registeredCompanyCode);
        Log.i(kLOG_TAG, "Verify Gramatik Company Code: " + companyCode);
        /* *************************************
        *       Root Specific
        ***************************************/
        if (registeredCompanyCode == null) {
//            companyNameAutoCompleteTextView.setError("Company name is not valid");
            return false;
        }

        if (!registeredCompanyCode.equalsIgnoreCase(companyCode)) {
//            companyCodeEditText.setError("Company code is wrong");
            return false;
        }
        return true;
    }

    @Override
    public void onValidationSucceeded() {
        Log.i(kLOG_TAG, "Verify Gramatik Validation Succeeded!");
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();

        /**************************************
         *       Root Specific
         ***************************************/
//        String companyName = companyNameAutoCompleteTextView.getText().toString();
//        String companyCode = companyCodeEditText.getText().toString();

        String companyName = "Root Metrics";
        String companyCode = "rm1";
        if (isValidCompanyCode(companyName, companyCode)) {
            registerUser(firstName, lastName, email, companyCode);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Log.i(kLOG_TAG, "Verify Gramatik: Validation Failed!");
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

    /* *************************************
     *       Root Specific
     ***************************************/

//    @InjectView(R.id.company_name)
//    @NotEmpty
//    AutoCompleteTextView companyNameAutoCompleteTextView;
//
//    @InjectView(R.id.company_code)
//    @NotEmpty
//    EditText companyCodeEditText;

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

    public static final String ACTIVITY_TITLE = " VERIFY";
}

