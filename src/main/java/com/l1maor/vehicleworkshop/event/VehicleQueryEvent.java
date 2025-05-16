package com.l1maor.vehicleworkshop.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a vehicle is queried
 */
public class VehicleQueryEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    public VehicleQueryEvent(String message) {
        super(message);
    }
    
    public String getMessage() {
        return (String) getSource();
    }
}
