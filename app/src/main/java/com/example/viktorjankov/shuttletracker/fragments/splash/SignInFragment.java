package com.example.viktorjankov.shuttletracker.fragments.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktorjankov.shuttletracker.R;
import com.example.viktorjankov.shuttletracker.events.StartApplicationEvent;
import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
import com.example.viktorjankov.shuttletracker.singletons.FirebaseProvider;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SignInFragment extends Fragment implements Validator.ValidationListener {

    @InjectView(R.id.email_id)
    @NotEmpty
    @Email
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
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Toast.makeText(getActivity(), authData.getUid(), Toast.LENGTH_LONG).show();
                bus.post(new StartApplicationEvent(authData.getUid()));
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                String message;
                switch (firebaseError.getCode()) {
                    case -16:
                        message = "Incorrect Password";
                        break;
                    default:
                        message = firebaseError.toString();
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    Bus bus = BusProvider.getInstance();

    Validator validator;
    Firebase mFirebase = FirebaseProvider.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sign_in_layout, container, false);
        ButterKnife.inject(this, v);

        validator = new Validator(this);
        validator.setValidationListener(this);

        return v;
    }

    @Override
    public void onValidationSucceeded() {

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();

            String message = error.getCollatedErrorMessage(getActivity());

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
