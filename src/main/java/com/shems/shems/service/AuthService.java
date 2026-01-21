package com.shems.shems.service;

import com.shems.shems.model.User;
import com.shems.shems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;


    // Email validation pattern
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    
    /**
     * Register a new user (signup)
     * @param username the username
     * @param email the email
     * @param password the password
     * @param role the user role
     * @return the created user or null if signup failed
     */
    public User signup(String username, String email, String password, String role) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return null;
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return null;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            return null;
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            return null;
        }
        
        // Create new user
        User user = new User(username, email, encodePassword(password), role);
        return userRepository.save(user);
    }
    
    /**
     * Authenticate a user (login)
     * @param username the username
     * @param password the password
     * @return the user if login successful, null otherwise
     */
    public User login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        
        if (user.isPresent()) {
            User foundUser = user.get();
            
            // Check if user is active
            if (!foundUser.isActive()) {
                return null;
            }
            
            // Validate password
            if (validatePassword(password, foundUser.getPassword())) {
                return foundUser;
            }
        }
        
        return null;
    }
    
    /**
     * Validate password against hashed password
     * @param password the plain password
     * @param hashedPassword the hashed password from database
     * @return true if password is valid
     */
    public boolean validatePassword(String password, String hashedPassword) {
        // Simple comparison - in production, use BCrypt or similar
        return encodePassword(password).equals(hashedPassword);
    }
    
    /**
     * Encode password (simple encoding for demo, use BCrypt in production)
     * @param password the password to encode
     * @return the encoded password
     */
    private String encodePassword(String password) {
        // This is a simple implementation - in production use BCrypt
        return java.util.Base64.getEncoder().encodeToString(password.getBytes());
    }
    
    /**
     * Validate password strength
     * @param password the password to validate
     * @return true if password meets requirements
     */
    public boolean isValidPassword(String password) {
        // Minimum 8 characters, at least one uppercase, one lowercase, one digit
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUppercase && hasLowercase && hasDigit;
    }
    
    /**
     * Validate email format
     * @param email the email to validate
     * @return true if email is valid
     */
    public boolean isValidEmail(String email) {
        return email != null && emailPattern.matcher(email).matches();
    }
    
    /**
     * Process forget password request
     * @param email the user's email
     * @return true if user exists, false otherwise
     */
    public boolean forgetPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        String token = java.util.UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        sendResetEmail(user.getEmail(), token);
        return true;
    }


    
    private void sendResetEmail(String email, String token) {

        String resetLink =
            "http://localhost:8081/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Your Password - SHEMS");
        message.setText(
            "Hello,\n\n" +
            "You requested to reset your password.\n\n" +
            "Click the link below to reset your password:\n" +
            resetLink + "\n\n" +
            "This link will expire in 15 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Regards,\nSHEMS Team"
        );

        mailSender.send(message);
    }


    
    /**
     * Reset user password
     * @param email the user's email
     * @param newPassword the new password
     * @return true if password reset successful
     */
    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setPassword(encodePassword(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }

    
    /**
     * Get user by username
     * @param username the username
     * @return the user or null
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    /**
     * Get user by email
     * @param email the email
     * @return the user or null
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Get user by ID
     * @param userId the user id
     * @return the user or null
    */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
}

    
    /**
     * Update user profile
     * @param userId the user id
     * @param email the new email
     * @param username the new username
     * @return true if update successful
     */
    public boolean updateUserProfile(Long userId, String email, String username) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            User foundUser = user.get();
            foundUser.updateProfile(email, username);
            userRepository.save(foundUser);
            return true;
        }
        
        return false;
    }

    /**
     * Update user profile with validation
     * @param userId the user id
     * @param email the new email
     * @param username the new username
     * @return true if update successful
    */
    public boolean updateUserProfileWithValidation(Long userId, String email, String username) {

        if (!isValidEmail(email)) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Prevent duplicate email
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()
                && !existingEmailUser.get().getUserId().equals(userId)) {
            return false;
        }

        user.setEmail(email);
        user.setUsername(username);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }

    public boolean updateUserProfileWithPassword(
        Long userId,
        String email,
        String username,
        String password) {

        if (!isValidEmail(email)) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Prevent duplicate email
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()
                && !existingEmailUser.get().getUserId().equals(userId)) {
            return false;
        }

        user.setEmail(email);
        user.setUsername(username);

        // Update password only if provided
        if (password != null && !password.isBlank()) {
            user.setPassword(encodePassword(password));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }
}