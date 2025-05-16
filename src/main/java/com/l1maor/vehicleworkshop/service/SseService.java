package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    
    private static final Logger logger = LoggerFactory.getLogger(SseService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    
    public SseEmitter createEmitter() {
        logger.info("Creating new SSE emitter");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            logger.debug("SSE emitter completed, removing from active emitters");
            emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            logger.debug("SSE emitter timed out, removing from active emitters");
            emitters.remove(emitter);
        });
        
        emitter.onError(e -> {
            logger.warn("SSE emitter error: {}, removing from active emitters", e.getMessage());
            emitters.remove(emitter);
        });
        
        emitters.add(emitter);
        logger.debug("Added new SSE emitter, current count: {}", emitters.size());

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connection established"));
            logger.debug("Sent initialization event to new SSE emitter");
        } catch (IOException e) {
            logger.error("Failed to send initialization event to SSE emitter: {}", e.getMessage());
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    public void broadcastVehicleUpdate(Vehicle vehicle) {
        if (emitters.isEmpty()) {
            logger.debug("No active SSE emitters to broadcast vehicle update");
            return;
        }
        
        logger.info("Broadcasting vehicle update for ID: {} to {} SSE emitters", 
                vehicle.getId(), emitters.size());
                
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("vehicleUpdate")
                        .data(vehicle));
            } catch (IOException e) {
                logger.warn("Failed to send vehicle update to emitter: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });
        
        if (!deadEmitters.isEmpty()) {
            logger.debug("Removing {} dead SSE emitters", deadEmitters.size());
            emitters.removeAll(deadEmitters);
        }
    }
    
    public void broadcastVehicleDelete(Long vehicleId) {
        if (emitters.isEmpty()) {
            logger.debug("No active SSE emitters to broadcast vehicle deletion");
            return;
        }
        
        logger.info("Broadcasting vehicle deletion for ID: {} to {} SSE emitters", 
                vehicleId, emitters.size());
                
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("vehicleDelete")
                        .data(vehicleId));
            } catch (IOException e) {
                logger.warn("Failed to send vehicle deletion to emitter: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });
        
        if (!deadEmitters.isEmpty()) {
            logger.debug("Removing {} dead SSE emitters", deadEmitters.size());
            emitters.removeAll(deadEmitters);
        }
    }
}
