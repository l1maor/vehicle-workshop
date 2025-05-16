package com.l1maor.vehicleworkshop.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when registration information is accessed
 */
public class RegistrationInfoAccessEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    public RegistrationInfoAccessEvent(String message) {
        super(message);
    }
    
    public String getMessage() {
        return (String) getSource();
    }
}
