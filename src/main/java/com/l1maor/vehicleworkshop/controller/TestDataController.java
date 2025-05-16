package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/test")
public class TestDataController {

    private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
    private final TestDataGenerator testDataGenerator;

    @Autowired
    public TestDataController(TestDataGenerator testDataGenerator) {
        this.testDataGenerator = testDataGenerator;
    }

    @PostMapping("/seed/small")
    public ResponseEntity<String> seedSmallDatabase() {
        logger.info("Starting database seeding with small dataset");
        testDataGenerator.seedSmallDataset();
        logger.info("Database successfully seeded with small dataset");
        return ResponseEntity.ok("Database seeded with small dataset successfully");
    }
    
    @PostMapping("/seed/large/{count}")
    public ResponseEntity<String> seedLargeDatabase(@PathVariable int count) {
        int recordCount = Math.min(Math.max(count, 100), 1000); // Between 100 and 1000
        logger.info("Starting database seeding with {} records", recordCount);
        testDataGenerator.seedLargeDataset(recordCount);
        logger.info("Database successfully seeded with {} records", recordCount);
        return ResponseEntity.ok("Database seeded with " + recordCount + " records successfully");
    }
    
    @GetMapping("/seed/status")
    public ResponseEntity<String> getDatabaseStatus() {
        logger.info("Test data status endpoint accessed");
        return ResponseEntity.ok("Test data endpoints are available. Use POST to /api/test/seed/small or /api/test/seed/large/{count} to seed the database.");
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearDatabase() {
        logger.warn("Clearing entire database");
        testDataGenerator.clearDatabase();
        logger.info("Database cleared successfully");
        return ResponseEntity.ok("Database cleared successfully");
    }
}
