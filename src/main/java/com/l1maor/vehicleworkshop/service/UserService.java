package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.RoleType;
import com.l1maor.vehicleworkshop.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User createUser(User user, String rawPassword, List<String> roleNames);
    User updateUser(Long id, User user);
    boolean deleteUser(Long id);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);

    List<User> findAllUsers();

    Page<User> findAllUsersPaginated(Pageable pageable);
    
    boolean existsByUsername(String username);
    RoleType findRoleByName(String roleName);
}
