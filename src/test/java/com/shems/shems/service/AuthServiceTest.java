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
import static org.mockito.ArgumentMatchers.*;
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
        
        // Create a test user for reuse
        testUser = new User("testuser", "test@example.com", 
                           "VGVzdFBhc3MxMjM=", "OWNER"); // Base64 encoded "TestPass123"
        testUser.setUserId(1L);
        testUser.setActive(true);
    }

    // ============ SIGNUP TESTS ============

    @Test
    void testSignupSuccessWithOwnerRole() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.signup("newuser", "new@example.com", "Password123", "OWNER");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignupSuccessWithFamilyMemberRole() {
        // Arrange
        when(userRepository.existsByUsername("familymember")).thenReturn(false);
        when(userRepository.existsByEmail("family@example.com")).thenReturn(false);
        User familyMemberUser = new User("familymember", "family@example.com", 
                                        "VGVzdFBhc3MxMjM=", "FAMILY_MEMBER");
        when(userRepository.save(any(User.class))).thenReturn(familyMemberUser);

        // Act
        User result = authService.signup("familymember", "family@example.com", "Password123", "FAMILY_MEMBER");

        // Assert
        assertNotNull(result);
        assertEquals("FAMILY_MEMBER", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignupSuccessWithGuestRole() {
        // Arrange
        when(userRepository.existsByUsername("guestuser")).thenReturn(false);
        when(userRepository.existsByEmail("guest@example.com")).thenReturn(false);
        User guestUser = new User("guestuser", "guest@example.com", 
                                 "VGVzdFBhc3MxMjM=", "GUEST");
        when(userRepository.save(any(User.class))).thenReturn(guestUser);

        // Act
        User result = authService.signup("guestuser", "guest@example.com", "Password123", "GUEST");

        // Assert
        assertNotNull(result);
        assertEquals("GUEST", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignupFailsWithDuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        User result = authService.signup("testuser", "new@example.com", "Password123", "OWNER");

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSignupFailsWithDuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        User result = authService.signup("newuser", "test@example.com", "Password123", "OWNER");

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSignupFailsWithInvalidEmail() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("invalidEmail")).thenReturn(false);

        // Act
        User result = authService.signup("newuser", "invalidEmail", "Password123", "OWNER");

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSignupFailsWithWeakPassword() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act
        User result = authService.signup("newuser", "new@example.com", "weak", "OWNER");

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ LOGIN TESTS ============

    @Test
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.login("testuser", "TestPass123");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginFailsWithInvalidUsername() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        User result = authService.login("nonexistent", "Password123");

        // Assert
        assertNull(result);
    }

    @Test
    void testLoginFailsWithInvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.login("testuser", "WrongPassword123");

        // Assert
        assertNull(result);
    }

    @Test
    void testLoginFailsWithInactiveUser() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.login("testuser", "TestPass123");

        // Assert
        assertNull(result);
    }

    // ============ PASSWORD VALIDATION TESTS ============

    @Test
    void testValidatePasswordSuccess() {
        // Act & Assert
        assertTrue(authService.validatePassword("TestPass123", "VGVzdFBhc3MxMjM="));
    }

    @Test
    void testValidatePasswordFailure() {
        // Act & Assert
        assertFalse(authService.validatePassword("WrongPassword", "VGVzdFBhc3MxMjM="));
    }

    @Test
    void testIsValidPasswordWithValidPassword() {
        // Act & Assert
        assertTrue(authService.isValidPassword("StrongPass123"));
    }

    @Test
    void testIsValidPasswordTooShort() {
        // Act & Assert
        assertFalse(authService.isValidPassword("Weak1"));
    }

    @Test
    void testIsValidPasswordNoUppercase() {
        // Act & Assert
        assertFalse(authService.isValidPassword("lowercase123"));
    }

    @Test
    void testIsValidPasswordNoLowercase() {
        // Act & Assert
        assertFalse(authService.isValidPassword("UPPERCASE123"));
    }

    @Test
    void testIsValidPasswordNoDigit() {
        // Act & Assert
        assertFalse(authService.isValidPassword("NoDigitPassword"));
    }

    @Test
    void testIsValidPasswordNull() {
        // Act & Assert
        assertFalse(authService.isValidPassword(null));
    }

    // ============ EMAIL VALIDATION TESTS ============

    @Test
    void testIsValidEmailWithValidEmail() {
        // Act & Assert
        assertTrue(authService.isValidEmail("test@example.com"));
        assertTrue(authService.isValidEmail("user.name+tag@domain.co.uk"));
    }

    @Test
    void testIsValidEmailWithInvalidEmail() {
        // Act & Assert
        assertFalse(authService.isValidEmail("invalid.email"));
        assertFalse(authService.isValidEmail("@nodomain.com"));
        assertFalse(authService.isValidEmail("noatsign.com"));
    }

    @Test
    void testIsValidEmailNull() {
        // Act & Assert
        assertFalse(authService.isValidEmail(null));
    }

    // ============ FORGET PASSWORD TESTS ============

    @Test
    void testForgetPasswordSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = authService.forgetPassword("test@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void testForgetPasswordFailsWithNonexistentEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = authService.forgetPassword("nonexistent@example.com");

        // Assert
        assertFalse(result);
    }

    // ============ RESET PASSWORD TESTS ============

    @Test
    void testResetPasswordSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.resetPassword("test@example.com", "NewPassword123");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testResetPasswordFailsWithWeakPassword() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = authService.resetPassword("test@example.com", "weak");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResetPasswordFailsWithNonexistentEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = authService.resetPassword("nonexistent@example.com", "NewPassword123");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ GET USER TESTS ============

    @Test
    void testGetUserByUsernameSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserByUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        User result = authService.getUserByUsername("nonexistent");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetUserByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetUserByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        User result = authService.getUserByEmail("nonexistent@example.com");

        // Assert
        assertNull(result);
    }

    // ============ UPDATE PROFILE TESTS ============

    @Test
    void testUpdateUserProfileSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.updateUserProfile(1L, "newemail@example.com", "newusername");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserProfileNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = authService.updateUserProfile(999L, "newemail@example.com", "newusername");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }
}
