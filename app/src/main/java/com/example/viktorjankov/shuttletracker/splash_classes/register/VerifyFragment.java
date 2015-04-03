package com.example.viktorjankov.shuttletracker.splash_classes.register;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AutoCompleteTextView;
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

import butterknife.InjectView;
import butterknife.OnClick;

public class VerifyFragment extends Fragment implements Validator.ValidationListener {
    private static final String FIREBASE_USERS = "users";
    @InjectView(R.id.company_name)
    @NotEmpty
    AutoCompleteTextView companyNameAutoCompleteTextView;
    String companyName;

    @InjectView(R.id.company_code)
    @NotEmpty
    EditText companyCodeEditText;
    String companyCode;

    @OnClick(R.id.registerButton)
    public void onClick() {

        validator.validate();

        companyName = companyNameAutoCompleteTextView.getText().toString();
        companyCode = companyCodeEditText.getText().toString();

        boolean companyValid = validateCompanyCode(companyName, companyCode);
    }



    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();



    private boolean validateCompanyCode(String companyName, String companyCode) {

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
        return true;
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
}
