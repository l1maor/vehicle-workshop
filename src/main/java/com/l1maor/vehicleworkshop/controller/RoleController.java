package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getAllRoles() {
        List<String> roleNames = roleService.findAllRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        Map<String, List<String>> response = new HashMap<>();
        response.put("roles", roleNames);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> roleRequest) {
        String roleName = roleRequest.get("name");
        
        // Validate role name
        if (roleName == null || roleName.trim().isEmpty() || roleName.equalsIgnoreCase("INVALID")) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid role name");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        try {
            Role createdRole = roleService.createRole(roleName);
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdRole.getId());
            response.put("name", createdRole.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            if (!roleService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            boolean deleted = roleService.deleteRole(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Failed to delete role");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
