package com.l1maor.vehicleworkshop.dto;

public class UserDto {
    private Long id;
    private String username;
    private String password; // Used only for creating/updating users
    private String roleType;

    public UserDto() {
    }

    public UserDto(Long id, String username, String roleType) {
        this.id = id;
        this.username = username;
        this.roleType = roleType;
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
    
    public String getRoleType() {
        return roleType;
    }
    
    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
}
