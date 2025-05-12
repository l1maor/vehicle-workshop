package com.l1maor.vehicleworkshop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        List<Role> roles = roleService.findAllRoles();
        
        List<Map<String, Object>> rolesList = roles.stream()
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    return roleMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("roles", rolesList);
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> roleRequest) {
        try {
            if (!roleService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Role updatedRole = roleService.updateRole(id, roleRequest.get("name"));
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedRole.getId());
            response.put("name", updatedRole.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
