package com.viktorjankov.shuttletracker.singletons;

import com.viktorjankov.shuttletracker.model.User;

public class UserProvider {

    private static User mUser;

    public static User getUser() {
        return mUser;
    }

    public static void setUser(User user) {
        mUser = user;
    }
}
