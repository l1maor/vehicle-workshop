package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role createRole(String name);
    boolean deleteRole(Long id);
    Optional<Role> findById(Long id);
    Optional<Role> findByName(String name);
    List<Role> findAllRoles();
    Role updateRole(Long id, String name);
}
