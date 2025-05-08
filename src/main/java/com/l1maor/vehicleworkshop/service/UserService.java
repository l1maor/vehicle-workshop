package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user, String rawPassword, List<String> roleNames);
    User updateUser(Long id, User user);
    boolean deleteUser(Long id);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAllUsers();
    boolean existsByUsername(String username);
}
