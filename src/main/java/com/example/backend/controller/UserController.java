package com.example.backend.controller;

import com.example.backend.dto.UserDto;
import com.example.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.debug("Fetching all users");

        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getUsersPaginated(Pageable pageable) {
        logger.debug("Fetching users with pagination");

        Page<UserDto> users = userService.getUsersPaginated(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user by id: {}", id);

        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile(Authentication authentication) {
        logger.debug("Fetching current user profile: {}", authentication.getName());

        UserDto user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        logger.debug("Fetching active users");

        List<UserDto> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> searchUsersByName(@RequestParam String name) {
        logger.debug("Searching users by name: {}", name);

        List<UserDto> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String roleName) {
        logger.debug("Fetching users by role: {}", roleName);

        List<UserDto> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/created-between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersCreatedBetween(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        logger.debug("Fetching users created between: {} and {}", start, end);

        List<UserDto> users = userService.getUsersCreatedBetween(start, end);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countActiveUsers() {
        logger.debug("Counting active users");

        long count = userService.countActiveUsers();
        return ResponseEntity.ok(Map.of("activeUserCount", count));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        logger.debug("Updating user with id: {}", id);

        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long id) {
        logger.debug("Deactivating user with id: {}", id);

        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long id) {
        logger.debug("Activating user with id: {}", id);

        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }
}