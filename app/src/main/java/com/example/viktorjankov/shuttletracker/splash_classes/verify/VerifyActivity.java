package com.example.viktorjankov.shuttletracker.splash_classes.verify;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VerifyActivity extends Activity implements Validator.ValidationListener {
    public static final String ACTIVITY_TITLE = " " + "REGISTER";
    private static final String FIREBASE_USERS = "users";

    public static final String firstNameKey = "first_name";
    public static final String lastNameKey = "last_name";
    public static final String emailKey = "email";
    public static final String registeredCompaniesKey = "registeredCompaniesList";
    public static final String registeredCompaniesCodesKey = "registeredCompaniesCodesKey";


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

        firstName = firstNameEditText.getText().toString();
        lastName = lastNameEditText.getText().toString();
        companyName = companyNameAutoCompleteTextView.getText().toString();
        companyCode = companyCodeEditText.getText().toString();

        boolean companyValid = validateCompanyCode(companyName, companyCode);
        if (companyValid) {
            registerUser(firstName, lastName, email, companyCode);
        }
    }
    Map<String, String> companyCodesMap = new HashMap<String, String>();
    List<String> registeredCompaniesList;
    List<String> registeredCompanyCodesList;
    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();
    String email;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(ACTIVITY_TITLE);
        getActionBar().setIcon(R.drawable.ic_arrow_back_black_36dp);

        setContentView(R.layout.verify_layout);
        ButterKnife.inject(this);

        validator = new Validator(this);
        validator.setValidationListener(this);

        firstName = getIntent().getExtras().getString(firstNameKey);
        lastName = getIntent().getExtras().getString(lastNameKey);
        email = getIntent().getExtras().getString(emailKey);
        registeredCompaniesList = getIntent().getExtras().getStringArrayList(registeredCompaniesKey);
        registeredCompanyCodesList = getIntent().getExtras().getStringArrayList(registeredCompaniesCodesKey);

        for (int i = 0; i < registeredCompaniesList.size(); i++) {
            companyCodesMap.put(registeredCompaniesList.get(i), registeredCompanyCodesList.get(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, registeredCompaniesList);
        companyNameAutoCompleteTextView.setAdapter(adapter);


        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);

    }

    private void registerUser(final String firstName, final String lastName, final String email, String companyCode) {
        User user = new User(firstName, lastName, email, companyCode);
        mFirebase.child(FIREBASE_USERS).push().setValue(user);
        Toast.makeText(VerifyActivity.this, "Congrats! You're registered!", Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message;

            if (view.getId() == R.id.password) {
                message = "Password must be at least 5 characters and contain letters and numbers only";
            } else {
                message = error.getCollatedErrorMessage(this);
            }

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}

