package app.user.service;

import app.web.dto.UserProfileResponse;
import app.web.dto.UserRegisterRequest;
import app.web.dto.UserUpdateRequest;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.UserStatus;
import app.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(UserRegisterRequest registerRequest) {
        logger.info("Attempting to register user with username: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: username {} already exists", registerRequest.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: email {} already exists", registerRequest.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(hashedPassword)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found with username: " + username);
                });
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        logger.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID id) {
        logger.debug("Getting profile for user ID: {}", id);
        User user = findById(id);
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void updateProfile(UUID id, UserUpdateRequest updateRequest) {
        logger.info("Updating profile for user ID: {}", id);
        User user = findById(id);

        if (!user.getUsername().equals(updateRequest.getUsername()) &&
                userRepository.existsByUsername(updateRequest.getUsername())) {
            logger.warn("Update failed: username {} already exists", updateRequest.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (!user.getEmail().equals(updateRequest.getEmail()) &&
                userRepository.existsByEmail(updateRequest.getEmail())) {
            logger.warn("Update failed: email {} already exists", updateRequest.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(updateRequest.getUsername());
        user.setEmail(updateRequest.getEmail());
        userRepository.save(user);
        logger.info("Profile updated successfully for user ID: {}", id);
    }

    @Transactional
    public void changeUserRole(UUID userId, UserRole newRole) {
        logger.info("Changing role for user ID: {} to role: {}", userId, newRole);
        User user = findById(userId);
        user.setRole(newRole);
        userRepository.save(user);
        logger.info("Role changed successfully for user ID: {}", userId);
    }

    @Transactional
    public void blockUser(UUID userId) {
        logger.info("Blocking user ID: {}", userId);
        User user = findById(userId);
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        logger.info("User blocked successfully with ID: {}", userId);
    }

    @Transactional
    public void unblockUser(UUID userId) {
        logger.info("Unblocking user ID: {}", userId);
        User user = findById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        logger.info("User unblocked successfully with ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
