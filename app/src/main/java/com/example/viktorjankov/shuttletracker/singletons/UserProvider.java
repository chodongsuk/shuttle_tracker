package com.example.viktorjankov.shuttletracker.singletons;

import com.example.viktorjankov.shuttletracker.model.User;

public class UserProvider {
    private static final User USER = new User("viktor");

    public static User getInstance() {
        USER.setActive(false);
        return USER;
    }

}
