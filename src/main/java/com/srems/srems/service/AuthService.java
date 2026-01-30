package com.srems.srems.service;

import com.srems.srems.dto.AuthRequest;
import com.srems.srems.model.*;
import com.srems.srems.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private ApprovalRepository approvalRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null &&
               password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*");
    }

    /* ================= REGISTER ================= */

    public User register(AuthRequest req) {

        // ---------- DUPLICATES ----------
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // ---------- VALIDATION ----------
        if (!isValidEmail(req.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        if (!isValidPassword(req.getPassword())) {
            throw new RuntimeException(
                    "Password must be 8+ chars with uppercase, lowercase & digit"
            );
        }

        Role role = Role.valueOf(req.getRole().toUpperCase());
        Flat flat = null;

        // ---------- BLOCK + FLAT VALIDATION ----------
        if (role == Role.FLAT_OWNER ||
            role == Role.FLAT_MEMBER ||
            role == Role.FLAT_GUEST) {

            if (req.getBlockId() == null) {
                throw new RuntimeException("Block is required");
            }

            if (req.getFlatNumber() == null || req.getFlatNumber().isBlank()) {
                throw new RuntimeException("Flat number is required");
            }

            flat = flatRepository
                    .findByBlock_BlockIdAndFlatNumber(
                            req.getBlockId(),
                            req.getFlatNumber()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Flat " + req.getFlatNumber() +
                                    " does not exist in the selected block"
                            )
                    );
        }

        // ---------- ONLY ONE OWNER PER FLAT ----------
        if (role == Role.FLAT_OWNER &&
            userRepository.existsByFlat_FlatIdAndRole(
                    flat.getFlatId(), Role.FLAT_OWNER)) {

            throw new RuntimeException("This flat already has an owner");
        }

        // ---------- CREATE USER ----------
        User user = new User(
                req.getUsername(),
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),
                role,
                req.getBlockId(),
                flat
        );

        // ---------- DETERMINE APPROVAL NEEDS & APPROVER ----------
        Long approverId = null;
        boolean needsApproval = false;

        switch (role) {
            case FLAT_OWNER, SECRETARY -> {
                // Admin approves owners/secretaries
                var admins = userRepository.findByRole(Role.ADMIN);
                if (admins.isEmpty()) throw new RuntimeException("No admin present to approve");
                approverId = admins.get(0).getUserId();
                needsApproval = true;
            }

            case FLAT_MEMBER, FLAT_GUEST -> {
                // Flat owner must approve
                if (flat == null) throw new RuntimeException("Flat required for members/guests");
                var owners = userRepository.findByFlat_FlatIdAndRole(flat.getFlatId(), Role.FLAT_OWNER);
                if (owners.isEmpty()) throw new RuntimeException("Flat owner not assigned. Cannot accept members/guests yet");
                approverId = owners.get(0).getUserId();
                needsApproval = true;
            }

            case SECURITY, STAFF -> {
                // Block secretary must approve security/staff
                if (req.getBlockId() == null) throw new RuntimeException("Block required for security/staff");
                var blockUsers = userRepository.findByBlockId(req.getBlockId());
                var secretaries = blockUsers.stream().filter(u -> u.getRole() == Role.SECRETARY).toList();
                if (secretaries.isEmpty()) throw new RuntimeException("Block secretary not assigned. Cannot accept security/staff yet");
                approverId = secretaries.get(0).getUserId();
                needsApproval = true;
            }

            default -> needsApproval = false;
        }

        user.setApproved(!needsApproval);
        user.setActive(!needsApproval);

        userRepository.save(user);

        // ---------- CREATE APPROVAL ----------
        if (needsApproval && approverId != null) {
            Approval approval = new Approval(user, approverId);
            approvalRepository.save(approval);

            // Notify approver by email (if available)
            userRepository.findById(approverId).ifPresent(approver -> {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(approver.getEmail());
                    msg.setSubject("SREMS - Approval required");
                    msg.setText("A new user '" + user.getUsername() + "' has registered as '" + role.name() + "'.\n" +
                            "Please review and approve/reject: http://localhost:8081/admin/approvals");
                    mailSender.send(msg);
                } catch (Exception e) {
                    System.out.println("Failed to send approver notification: " + e.getMessage());
                }
            });
        }

        return user;
    }

    /* ================= LOGIN ================= */

    public User login(String input, String password) {

        User user = userRepository
                .findByUsernameOrEmail(input, input)
                .orElse(null);

        if (user == null) return null;
        if (!passwordEncoder.matches(password, user.getPassword())) return null;

        if (user.getRole() != Role.ADMIN &&
            (!user.isApproved() || !user.isActive())) {
            return null;
        }

        return user;
    }

    /* ================= FORGOT / RESET PASSWORD ================= */

    public boolean forgetPassword(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Reset Password - SREMS");
        msg.setText("Reset link:\nhttp://localhost:8081/reset-password?token=" + token);
        mailSender.send(msg);

        return true;
    }

    public boolean resetPassword(String token, String newPassword) {

        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (user.getTokenExpiry() == null ||
            user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return true;
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        return resetPassword(token, newPassword);
    }

}