package com.mediconnect.event;

import com.mediconnect.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    // User associated with the registration event
    private final User user;

    public UserRegisteredEvent(User user) {
        super(user);
        this.user = user;
    }
}
