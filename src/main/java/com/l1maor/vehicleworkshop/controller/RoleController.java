package com.l1maor.vehicleworkshop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.l1maor.vehicleworkshop.entity.RoleType;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        logger.info("Fetching all available roles");
        List<Map<String, Object>> rolesList = new ArrayList<>();
        
        for (RoleType roleType : RoleType.values()) {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("name", roleType.name());
            rolesList.add(roleMap);
        }
        
        logger.debug("Retrieved {} roles", rolesList.size());
        Map<String, Object> response = new HashMap<>();
        response.put("roles", rolesList);
        return ResponseEntity.ok(response);
    }
}
