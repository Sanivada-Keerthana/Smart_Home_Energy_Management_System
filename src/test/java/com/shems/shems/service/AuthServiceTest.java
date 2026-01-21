package com.shems.shems.service;

import com.shems.shems.model.User;
import com.shems.shems.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(
                "testuser",
                "test@example.com",
                "VGVzdFBhc3MxMjM=", // Base64(TestPass123)
                "OWNER"
        );
        testUser.setUserId(1L);
        testUser.setActive(true);
        testUser.setResetToken("valid-reset-token");
    }

    // ================= SIGNUP TESTS =================

    @Test
    void testSignupSuccess() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.signup(
                "newuser",
                "new@example.com",
                "Password123",
                "OWNER"
        );

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSignupFailsDuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        User result = authService.signup(
                "testuser",
                "new@example.com",
                "Password123",
                "OWNER"
        );

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSignupFailsWeakPassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        User result = authService.signup(
                "newuser",
                "new@example.com",
                "weak",
                "OWNER"
        );

        assertNull(result);
    }

    // ================= LOGIN TESTS =================

    @Test
    void testLoginSuccess() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        User result = authService.login("testuser", "TestPass123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginFailsInvalidPassword() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        User result = authService.login("testuser", "WrongPass");

        assertNull(result);
    }

    @Test
    void testLoginFailsInactiveUser() {
        testUser.setActive(false);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        User result = authService.login("testuser", "TestPass123");

        assertNull(result);
    }

    // ================= EMAIL VALIDATION =================

    @Test
    void testIsValidEmail() {
        assertTrue(authService.isValidEmail("test@example.com"));
        assertFalse(authService.isValidEmail("invalid-email"));
    }

    // ================= PASSWORD VALIDATION =================

    @Test
    void testIsValidPassword() {
        assertTrue(authService.isValidPassword("StrongPass123"));
        assertFalse(authService.isValidPassword("weak"));
    }

    // ================= FORGET PASSWORD =================

    @Test
    void testForgetPasswordSuccess() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        boolean result = authService.forgetPassword("test@example.com");

        assertTrue(result);
        verify(userRepository).save(any(User.class)); // token saved
    }

    @Test
    void testForgetPasswordFails() {
        when(userRepository.findByEmail("no@example.com"))
                .thenReturn(Optional.empty());

        boolean result = authService.forgetPassword("no@example.com");

        assertFalse(result);
    }

    // ================= RESET PASSWORD WITH TOKEN =================

    @Test
    void testResetPasswordWithTokenSuccess() {
        when(userRepository.findByResetToken("valid-reset-token"))
                .thenReturn(Optional.of(testUser));

        boolean result = authService.resetPasswordWithToken(
                "valid-reset-token",
                "NewPassword123"
        );

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPasswordWithTokenFailsInvalidToken() {
        when(userRepository.findByResetToken("invalid-token"))
                .thenReturn(Optional.empty());

        boolean result = authService.resetPasswordWithToken(
                "invalid-token",
                "NewPassword123"
        );

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // ================= PROFILE UPDATE =================

    @Test
    void testUpdateUserProfileSuccess() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        boolean result = authService.updateUserProfile(
                1L,
                "new@example.com",
                "newusername"
        );

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserProfileWithPassword() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        boolean result = authService.updateUserProfileWithPassword(
                1L,
                "new@example.com",
                "newusername",
                "NewPass123"
        );

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }
}
