package com.example.backend.service;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");

        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsersPaginated(Pageable pageable) {
        logger.debug("Fetching users with pagination: {}", pageable);

        return userRepository.findAll(pageable)
                .map(this::convertToUserDto);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        logger.debug("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return convertToUserDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return convertToUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        logger.debug("Fetching active users");

        return userRepository.findByIsActiveTrue().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsersByName(String name) {
        logger.debug("Searching users by name: {}", name);

        return userRepository.findByName(name).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(String roleName) {
        logger.debug("Fetching users by role: {}", roleName);

        return userRepository.findByRoleName(roleName).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching users created between: {} and {}", startDate, endDate);

        return userRepository.findUsersCreatedBetween(startDate, endDate).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        logger.debug("Counting active users");
        return userRepository.countActiveUsers();
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        logger.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", updatedUser.getUsername());

        return convertToUserDto(updatedUser);
    }

    public void deactivateUser(Long id) {
        logger.debug("Deactivating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        logger.info("User deactivated successfully: {}", user.getUsername());
    }

    public void activateUser(Long id) {
        logger.debug("Activating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        logger.info("User activated successfully: {}", user.getUsername());
    }

    private UserDto convertToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );
    }
}