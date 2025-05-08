package com.l1maor.vehicleworkshop.dto;

import java.util.HashSet;
import java.util.Set;

public class UserDto {
    private Long id;
    private String username;
    private String password; // Used only for creating/updating users
    private Set<String> roles = new HashSet<>();

    public UserDto() {
    }

    public UserDto(Long id, String username, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
