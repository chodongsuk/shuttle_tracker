package com.example.viktorjankov.shuttletracker.singletons;

import com.example.viktorjankov.shuttletracker.model.User;

public class UserProvider {

    private static User mUser;

    public static User getInstance() {
        if (mUser != null) {
            return mUser;

        } else {
            return new User();
        }
    }

    public static void setUser(User user) {
        mUser = user;
    }
}
