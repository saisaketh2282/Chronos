package com.Prod.Chronos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Chronos Job Scheduler");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        Map<String, String> checks = new HashMap<>();
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            checks.put("database", connection.isValid(5) ? "UP" : "DOWN");
        } catch (Exception e) {
            checks.put("database", "DOWN");
        }
        
        // For now, assume Kafka and Splunk are UP (in a real implementation, 
        // you would check actual connectivity)
        checks.put("kafka", "UP");
        checks.put("splunk", "UP");
        
        boolean allUp = checks.values().stream().allMatch(status -> "UP".equals(status));
        
        readiness.put("status", allUp ? "READY" : "NOT_READY");
        readiness.put("timestamp", LocalDateTime.now());
        readiness.put("checks", checks);
        
        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(liveness);
    }
}
