package com.example.viktorjankov.shuttletracker.splash_classes.register;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.model.User;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RegisterFragment extends Fragment implements Validator.ValidationListener {
    private static final String kLOG_TAG = "RegisterFragment";

    private static final String FIREBASE_COMPANIES_ENDPOINT = "companies";
    private static final String FIREBASE_COMPANY_NAME = "companyName";
    private static final String FIREBASE_COMPANY_CODE = "companyCode";
    private static final String FIREBASE_DESTINATIONS = "destinations";
    private static final String FIREBASE_DESTINATION_NAME = "destinationName";
    private static final String FIREBASE_DESTINATION_LAT = "lat";
    private static final String FIREBASE_DESTINATION_LNG = "lng";
    private static final String FIREBASE_REGISTERED_COMPANIES = "registeredCompanies";
    private static final String FIREBASE_USERS = "users";

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

    @InjectView(R.id.email)
    @NotEmpty
    @Email
    EditText emailEditText;
    String email;

    @InjectView(R.id.password)
    @Password(min = 5, scheme = Password.Scheme.ALPHA_NUMERIC)
    EditText passwordEditText;
    String password;

    @InjectView(R.id.registerButton)
    Button registerButton;

    @OnClick(R.id.registerButton)
    public void onClick() {
        validator.validate();

        firstName = firstNameEditText.getText().toString();
        lastName = lastNameEditText.getText().toString();
        companyName = companyNameAutoCompleteTextView.getText().toString();
        companyCode = companyCodeEditText.getText().toString();
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        boolean companyValid = validateCompanyCode(companyName, companyCode);
        if (companyValid) {
            registerUser(email, password, firstName, lastName);
        }

    }

    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();
    Map<String, String> companyCodesMap = new HashMap<String, String>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_layout, container, false);
        ButterKnife.inject(this, v);

        validator = new Validator(this);
        validator.setValidationListener(this);

        mFirebase.child(FIREBASE_REGISTERED_COMPANIES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> registeredCompanyList = new ArrayList<String>();
                for (DataSnapshot company : dataSnapshot.getChildren()) {
                    String companyName = "";
                    String companyCode = "";
                    for (DataSnapshot companyValues : company.getChildren()) {
                        if (companyValues.getKey().equals("companyCode")) {
                            companyCode = companyValues.getValue().toString();
                        }
                        if (companyValues.getKey().equals("companyName")) {
                            companyName = companyValues.getValue().toString();
                            registeredCompanyList.add(companyName);
                        }
                    }
                    companyCodesMap.put(companyName, companyCode);
                }

                if (getActivity() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_dropdown_item_1line, registeredCompanyList);
                    companyNameAutoCompleteTextView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return v;
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
                message = error.getCollatedErrorMessage(getActivity());
            }

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerUser(final String email, String password, final String firstName, final String lastName) {
        mFirebase.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                User user = new User(firstName, lastName, email, companyCode);
                Map<Object, Object> oneUserMap = new HashMap<Object, Object>();
                oneUserMap.put(result.get("uid"), user);
                mFirebase.child(FIREBASE_USERS).setValue(oneUserMap);
                Toast.makeText(getActivity(), "Congrats! You're registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                String errorMessage;
                switch (firebaseError.getCode()) {
                    case -18:
                        errorMessage = "Email is already in use!";
                        break;
                    case -16:
                        errorMessage = "Invalid password!";
                        break;
                    default:
                        errorMessage = firebaseError.toString();
                }
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

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

    //    private void parseCompany() {
//        mFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot companies : dataSnapshot.getChildren()) {
//                    Company company = new Company();
//                    for (DataSnapshot companyData : companies.getChildren()) {
//                        if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
//                            company.setCompanyCode(companyData.getValue().toString());
//                        } else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
//                            company.setCompanyName(companyData.getValue().toString());
//                        } else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {
//
//                            for (DataSnapshot destinations : companyData.getChildren()) {
//                                String destinationName = "";
//                                double lat = 0;
//                                double lng = 0;
//                                if (destinations.getKey().equals(FIREBASE_DESTINATION_NAME)) {
//                                    destinationName = destinations.getValue().toString();
//                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LAT)) {
//                                    lat = Double.parseDouble(destinations.getValue().toString());
//                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LNG)) {
//                                    lng = Double.parseDouble(destinations.getValue().toString());
//                                }
//                                DestinationLocation destinationLocation = new DestinationLocation(destinationName, lat, lng);
//                                company.addDestinationLocation(destinationLocation);
//                            }
//                        }
//                    }
//                    companyList.add(company);
//                }
//
//                for(Company company : companyList) {
//                    Log.i("RegisterFragment", company.toString());
//                }
//            }
//    }
}
