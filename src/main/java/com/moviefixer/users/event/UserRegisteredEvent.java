package com.moviefixer.users.event;

import com.moviefixer.users.entity.Users;

public class UserRegisteredEvent {

    private final Users user;

    public UserRegisteredEvent(Users user) {
        this.user = user;
    }

    public Users getUser() {
        return user;
    }
}
