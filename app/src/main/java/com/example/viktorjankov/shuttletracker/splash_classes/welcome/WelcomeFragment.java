//package com.example.viktorjankov.shuttletracker.splash_classes.welcome;
//
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import com.example.viktorjankov.shuttletracker.R;
//import com.example.viktorjankov.shuttletracker.events.StartRegisterEvent;
//import com.example.viktorjankov.shuttletracker.events.StartSignInEvent;
//import com.example.viktorjankov.shuttletracker.singletons.BusProvider;
//import com.squareup.otto.Bus;
//
//import butterknife.ButterKnife;
//import butterknife.InjectView;
//import butterknife.OnClick;
//
//public class WelcomeFragment extends Fragment {
//
//    @InjectView(R.id.sign_in)
//    Button signInButton;
//    @InjectView(R.id.register)
//    Button registerButton;
//    Bus bus = BusProvider.getInstance();
//
//    @OnClick({R.id.sign_in, R.id.register})
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.sign_in:
//                bus.post(new StartSignInEvent());
//                break;
//            case R.id.register:
//                bus.post(new StartRegisterEvent());
//                break;
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.welcome_layout, container, false);
//        ButterKnife.inject(this, v);
//
//        int resource;
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            resource = R.drawable.ripple;
//        } else {
//            resource = R.drawable.flat_pressed;
//        }
//        signInButton.setBackgroundResource(resource);
//        registerButton.setBackgroundResource(resource);
//
//        return v;
//    }
//}
