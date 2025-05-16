package com.l1maor.vehicleworkshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class VehicleworkshopApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleworkshopApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Vehicle Workshop application...");
        SpringApplication.run(VehicleworkshopApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        logger.info("=======================================================");
        logger.info("Vehicle Workshop server started successfully");
        logger.info("=======================================================");
    }
}
