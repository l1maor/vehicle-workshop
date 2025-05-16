package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.RoleType;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(User user, String rawPassword, List<String> roleNames) {
        logger.info("Creating new user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("User creation failed - username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        logger.debug("Password encoded for user: {}", user.getUsername());

        if (roleNames != null && !roleNames.isEmpty()) {
            if (roleNames.contains("ROLE_ADMIN")) {
                logger.debug("Setting ADMIN role for user: {}", user.getUsername());
                user.setRoleType(RoleType.ROLE_ADMIN);
            } else {
                logger.debug("Setting USER role for user: {}", user.getUsername());
                user.setRoleType(RoleType.ROLE_USER);
            }
        } else {
            logger.debug("No roles provided, setting default USER role for user: {}", user.getUsername());
            user.setRoleType(RoleType.ROLE_USER);
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        logger.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update failed - user not found with ID: {}", id);
                    return new EntityNotFoundException("User not found with id: " + id);
                });
        
        logger.debug("Updating username from '{}' to '{}'", existingUser.getUsername(), updatedUser.getUsername());
        existingUser.setUsername(updatedUser.getUsername());

        if (updatedUser.getRoleType() != null) {
            logger.debug("Updating user role from '{}' to '{}'", existingUser.getRoleType(), updatedUser.getRoleType());
            existingUser.setRoleType(updatedUser.getRoleType());
        }

        User savedUser = userRepository.save(existingUser);
        logger.info("User updated successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            logger.info("User deleted successfully with ID: {}", id);
            return true;
        }
        
        logger.warn("Delete failed - user not found with ID: {}", id);
        return false;
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.debug("Found user: {} (ID: {})", user.get().getUsername(), id);
        } else {
            logger.debug("User not found with ID: {}", id);
        }
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("Found user with username: {}", username);
        } else {
            logger.debug("User not found with username: {}", username);
        }
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        logger.debug("Finding all users");
        List<User> users = userRepository.findAll();
        logger.debug("Found {} users", users.size());
        return users;
    }
    
    @Override
    public Page<User> findAllUsersPaginated(Pageable pageable) {
        logger.debug("Finding paginated users: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findAll(pageable);
        logger.debug("Found {} users on page {} of {}", 
                userPage.getNumberOfElements(), pageable.getPageNumber(), userPage.getTotalPages());
        return userPage;
    }

    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking if username exists: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        logger.debug("Username '{}' exists: {}", username, exists);
        return exists;
    }

    @Override
    public RoleType findRoleByName(String roleName) {
        logger.debug("Finding role by name: {}", roleName);
        try {
            RoleType roleType = RoleType.valueOf(roleName);
            logger.debug("Found role: {}", roleType);
            return roleType;
        } catch (IllegalArgumentException e) {
            logger.warn("Role not found with name: {}", roleName);
            throw new EntityNotFoundException("Role not found: " + roleName);
        }
    }
}
