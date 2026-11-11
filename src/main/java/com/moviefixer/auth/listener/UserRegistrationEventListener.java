package com.moviefixer.auth.listener;

import com.moviefixer.auth.service.AuthService;
import com.moviefixer.users.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationEventListener.class);

    private final AuthService authService;

    public UserRegistrationEventListener(AuthService authService) {
        this.authService = authService;
    }

    @EventListener
    public void userVerefyficationLinkEvent(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.getUser().getUsername());
        authService.createVerificationToken(event.getUser());
    }
}
