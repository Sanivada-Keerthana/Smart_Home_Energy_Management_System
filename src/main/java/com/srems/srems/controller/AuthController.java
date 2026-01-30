package com.srems.srems.controller;

import com.srems.srems.dto.AuthRequest;
import com.srems.srems.dto.AuthResponse;
import com.srems.srems.dto.PasswordResetRequest;
import com.srems.srems.model.User;
import com.srems.srems.model.Role;
import com.srems.srems.service.AuthService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    /* ================= REGISTER ================= */

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest req) {

        User user = authService.register(req);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Registration failed"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true,
                        "Registration successful. Waiting for approval"));
    }

    /* ================= LOGIN ================= */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest req,
            HttpSession session) {

        User user = authService.login(
                req.getUsername(), req.getPassword());
        
        System.out.println("REQ USERNAME = " + req.getUsername());
        System.out.println("REQ PASSWORD = " + req.getPassword());
        System.out.println("CONTROLLER USER = " + user);        

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false,
                            "Invalid credentials or approval pending"));
        }

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("flatId", user.getFlat() != null ? user.getFlat().getFlatId() : null);

        if (user.getRole() != Role.ADMIN) {
                session.setAttribute("blockId", user.getBlockId());
        }

        if (user.getFlat() != null) {
                session.setAttribute("flatId", user.getFlat().getFlatId());
        }

        // Keep full user object in session for APIs that expect it
        session.setAttribute("user", user);

        return ResponseEntity.ok(
                new AuthResponse(true, "Login successful"));
    }

    /* ================= LOGOUT ================= */

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(
                new AuthResponse(true, "Logout successful"));
    }

    /* ================= FORGOT PASSWORD ================= */

    @PostMapping("/forget-password")
    public ResponseEntity<AuthResponse> forgetPassword(
            @RequestParam String email) {

        boolean ok = authService.forgetPassword(email);

        return ResponseEntity.ok(
                new AuthResponse(true,
                        "If the account exists, a reset link has been sent"));
    }

    /* ================= RESET PASSWORD ================= */

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @RequestBody PasswordResetRequest req) {

        if (!req.getNewPassword()
                .equals(req.getConfirmPassword())) {

            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false,
                            "Passwords do not match"));
        }

        boolean ok = authService.resetPasswordWithToken(
                req.getToken(), req.getNewPassword());

        if (!ok) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false,
                            "Invalid or expired token"));
        }

        return ResponseEntity.ok(
                new AuthResponse(true,
                        "Password reset successful"));
    }
}