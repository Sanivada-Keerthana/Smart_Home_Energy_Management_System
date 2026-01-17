package com.shems.shems.controller;

import com.shems.shems.dto.AuthRequest;
import com.shems.shems.dto.AuthResponse;
import com.shems.shems.dto.PasswordResetRequest;
import com.shems.shems.model.User;
import com.shems.shems.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Signup endpoint
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest authRequest) {
        try {
            if (authRequest.getUsername() == null || authRequest.getUsername().isEmpty() ||
                authRequest.getEmail() == null || authRequest.getEmail().isEmpty() ||
                authRequest.getPassword() == null || authRequest.getPassword().isEmpty() ||
                authRequest.getRole() == null || authRequest.getRole().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "All fields are required"));
            }
            
            // Validate role
            String role = authRequest.getRole().toUpperCase();
            if (!role.equals("OWNER") && !role.equals("FAMILY_MEMBER") && !role.equals("GUEST")) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Invalid role. Must be OWNER, FAMILY_MEMBER, or GUEST"));
            }
            
            // Validate email format
            if (!authService.isValidEmail(authRequest.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Invalid email format"));
            }
            
            // Validate password strength
            if (!authService.isValidPassword(authRequest.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Password must be at least 8 characters with uppercase, lowercase, and digit"));
            }
            
            // Attempt to signup user with specified role
            User user = authService.signup(authRequest.getUsername(), authRequest.getEmail(), 
                                           authRequest.getPassword(), role);
            
            if (user != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(true, "User registered successfully", user.getUsername(), user.getEmail()));
            } else {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Username or email already exists"));
            }
        } catch (Exception e) {
            System.err.println("Signup error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Database connection error. Please check if MySQL is running on localhost:3306"));
        }
    }
    
    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpSession session) {
        try {
            if (authRequest.getUsername() == null || authRequest.getUsername().isEmpty() ||
                authRequest.getPassword() == null || authRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Username and password are required"));
            }
            
            User user = authService.login(authRequest.getUsername(), authRequest.getPassword());
            
            if (user != null) {
                // Store user in session
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("email", user.getEmail());
                session.setAttribute("role", user.getRole());
                
                return ResponseEntity.ok()
                    .body(new AuthResponse(true, "Login successful", user.getUsername(), user.getEmail()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid username or password"));
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Database connection error. Please check if MySQL is running on localhost:3306"));
        }
    }
    
    /**
     * Forget Password endpoint
     * POST /api/auth/forget-password
     */
    @PostMapping("/forget-password")
    public ResponseEntity<AuthResponse> forgetPassword(@RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Email is required"));
            }
            
            if (!authService.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Invalid email format"));
            }
            
            if (authService.forgetPassword(email)) {
                return ResponseEntity.ok()
                    .body(new AuthResponse(true, "Password reset link has been sent to your email"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Email not found"));
            }
        } catch (Exception e) {
            System.err.println("Forget password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Database connection error. Please check if MySQL is running on localhost:3306"));
        }
    }
    
    /**
     * Reset Password endpoint
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody PasswordResetRequest resetRequest) {
        try {
            if (resetRequest.getEmail() == null || resetRequest.getEmail().isEmpty() ||
                resetRequest.getNewPassword() == null || resetRequest.getNewPassword().isEmpty() ||
                resetRequest.getConfirmPassword() == null || resetRequest.getConfirmPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "All fields are required"));
            }
            
            // Check if passwords match
            if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Passwords do not match"));
            }
            
            // Validate password strength
            if (!authService.isValidPassword(resetRequest.getNewPassword())) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Password must be at least 8 characters with uppercase, lowercase, and digit"));
            }
            
            if (authService.resetPassword(resetRequest.getEmail(), resetRequest.getNewPassword())) {
                return ResponseEntity.ok()
                    .body(new AuthResponse(true, "Password reset successful"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Failed to reset password"));
            }
        } catch (Exception e) {
            System.err.println("Reset password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Database connection error. Please check if MySQL is running on localhost:3306"));
        }
    }
    
    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        try {
            session.invalidate();
            return ResponseEntity.ok()
                .body(new AuthResponse(true, "Logout successful"));
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "An error occurred during logout"));
        }
    }
    
    /**
     * Check session endpoint
     * GET /api/auth/check-session
     */
    @GetMapping("/check-session")
    public ResponseEntity<AuthResponse> checkSession(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            String email = (String) session.getAttribute("email");
            
            if (username != null) {
                return ResponseEntity.ok()
                    .body(new AuthResponse(true, "Session active", username, email));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "No active session"));
            }
        } catch (Exception e) {
            System.err.println("Check session error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "An error occurred while checking session"));
        }
    }
}
