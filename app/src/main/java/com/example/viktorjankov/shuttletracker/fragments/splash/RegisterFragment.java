package com.example.viktorjankov.shuttletracker.fragments.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.model.Company;
import com.example.viktorjankov.shuttletracker.model.DestinationLocation;
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
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RegisterFragment extends Fragment implements Validator.ValidationListener {

    private static final String FIREBASE_COMPANIES_ENDPOINT = "companies";
    private static final String FIREBASE_COMPANY_NAME = "companyName";
    private static final String FIREBASE_COMPANY_CODE = "companyCode";
    private static final String FIREBASE_DESTINATIONS = "destinations";
    private static final String FIREBASE_DESTINATION_NAME = "destinationName";
    private static final String FIREBASE_DESTINATION_LAT = "lat";
    private static final String FIREBASE_DESTINATION_LNG ="lng";

    @InjectView(R.id.first_name)
    @NotEmpty
    EditText firstNameEditText;

    @InjectView(R.id.last_name)
    @NotEmpty
    EditText lastNameEditText;

    @InjectView(R.id.company_name)
    @NotEmpty
    EditText companyNameEditText;

    @InjectView(R.id.company_code)
    @NotEmpty
    EditText companyCodeEditText;

    @InjectView(R.id.email)
    @NotEmpty
    @Email
    EditText emailEditText;

    @InjectView(R.id.password)
    @Password(min = 5, scheme = Password.Scheme.ALPHA_NUMERIC_SYMBOLS)
    EditText passwordEditText;

    @InjectView(R.id.registerButton)
    Button registerButton;

    @OnClick(R.id.registerButton)
    public void onClick() {
        validator.validate();
    }

    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance().child(FIREBASE_COMPANIES_ENDPOINT);
    List<Company> companyList = new ArrayList<Company>();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_layout, container, false);
        ButterKnife.inject(this, v);

        validator = new Validator(this);
        validator.setValidationListener(this);

        mFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot companies : dataSnapshot.getChildren()) {
                    Company company = new Company();
                    for (DataSnapshot companyData : companies.getChildren()) {
                        if (companyData.getKey().equals(FIREBASE_COMPANY_CODE)) {
                            company.setCompanyCode(companyData.getValue().toString());
                        } else if (companyData.getKey().equals(FIREBASE_COMPANY_NAME)) {
                            company.setCompanyName(companyData.getValue().toString());
                        } else if (companyData.getKey().equals(FIREBASE_DESTINATIONS)) {

                            for (DataSnapshot destinations : companyData.getChildren()) {
                                String destinationName = "";
                                double lat = 0;
                                double lng = 0;
                                if (destinations.getKey().equals(FIREBASE_DESTINATION_NAME)) {
                                    destinationName = destinations.getValue().toString();
                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LAT)) {
                                    lat = Double.parseDouble(destinations.getValue().toString());
                                } else if (destinations.getKey().equals(FIREBASE_DESTINATION_LNG)) {
                                    lng = Double.parseDouble(destinations.getValue().toString());
                                }
                                DestinationLocation destinationLocation = new DestinationLocation(destinationName, lat, lng);
                                company.addDestinationLocation(destinationLocation);
                            }
                        }
                    }
                    companyList.add(company);
                }

                for(Company company : companyList) {
                    Log.i("RegisterFragment", company.toString());
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
                message = "Password must be at least 5 characters";
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
}
