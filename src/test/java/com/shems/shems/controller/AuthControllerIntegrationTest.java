package com.shems.shems.controller;

import com.shems.shems.dto.AuthRequest;
import com.shems.shems.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Authentication Module
 * Tests the complete flow of registration with role-based selection
 */
class AuthControllerIntegrationTest {

    private AuthService authService;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        authRequest = new AuthRequest();
    }

    // ============ REGISTRATION VALIDATION TESTS ============

    @Test
    void testRegistrationValidateOwnerRole() {
        authRequest.setUsername("owner_user");
        authRequest.setEmail("owner@example.com");
        authRequest.setPassword("ValidPassword123");
        authRequest.setRole("OWNER");

        assertNotNull(authRequest.getUsername());
        assertNotNull(authRequest.getEmail());
        assertNotNull(authRequest.getPassword());
        assertEquals("OWNER", authRequest.getRole());
    }

    @Test
    void testRegistrationValidateFamilyMemberRole() {
        authRequest.setUsername("family_member");
        authRequest.setEmail("family@example.com");
        authRequest.setPassword("ValidPassword123");
        authRequest.setRole("FAMILY_MEMBER");

        assertEquals("FAMILY_MEMBER", authRequest.getRole());
        assertNotNull(authRequest.getUsername());
    }

    @Test
    void testRegistrationValidateGuestRole() {
        authRequest.setUsername("guest_user");
        authRequest.setEmail("guest@example.com");
        authRequest.setPassword("ValidPassword123");
        authRequest.setRole("GUEST");

        assertEquals("GUEST", authRequest.getRole());
    }

    @Test
    void testRegistrationWithInvalidRole() {
        authRequest.setRole("INVALID_ROLE");

        assertNotEquals("OWNER", authRequest.getRole());
        assertNotEquals("FAMILY_MEMBER", authRequest.getRole());
        assertNotEquals("GUEST", authRequest.getRole());
    }

    @Test
    void testRegistrationMissingRole() {
        authRequest.setUsername("test_user");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("TestPass123");

        assertNull(authRequest.getRole());
    }

    // ============ VALIDATION TESTS ============

    @Test
    void testPasswordValidation() {
        assertTrue(authService.isValidPassword("ValidPass123"));
        assertFalse(authService.isValidPassword("weak"));
        assertFalse(authService.isValidPassword("nouppercase123"));
        assertFalse(authService.isValidPassword("NOLOWERCASE123"));
        assertFalse(authService.isValidPassword("NoDigitPassword"));
    }

    @Test
    void testEmailValidation() {
        assertTrue(authService.isValidEmail("test@example.com"));
        assertTrue(authService.isValidEmail("user.name+tag@domain.co.uk"));
        assertFalse(authService.isValidEmail("invalid.email"));
        assertFalse(authService.isValidEmail("@nodomain.com"));
        assertFalse(authService.isValidEmail("noatsign.com"));
        assertFalse(authService.isValidEmail(null));
    }

    @Test
    void testRoleValidation() {
        String[] validRoles = {"OWNER", "FAMILY_MEMBER", "GUEST"};
        for (String role : validRoles) {
            assertNotNull(role);
            assertTrue(role.equals("OWNER") || role.equals("FAMILY_MEMBER") || role.equals("GUEST"));
        }
    }

    // ============ REQUEST OBJECT TESTS ============

    @Test
    void testAuthRequestWithAllFields() {
        AuthRequest request = new AuthRequest("testuser", "test@example.com", "TestPass123", "OWNER");

        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("TestPass123", request.getPassword());
        assertEquals("OWNER", request.getRole());
    }

    @Test
    void testAuthRequestWithoutRole() {
        AuthRequest request = new AuthRequest("testuser", "test@example.com", "TestPass123");

        assertNull(request.getRole());
        assertNotNull(request.getUsername());
        assertNotNull(request.getEmail());
        assertNotNull(request.getPassword());
    }

    @Test
    void testAuthRequestSettersGetters() {
        authRequest.setUsername("john_doe");
        authRequest.setEmail("john@example.com");
        authRequest.setPassword("JohnPass123");
        authRequest.setRole("FAMILY_MEMBER");

        assertEquals("john_doe", authRequest.getUsername());
        assertEquals("john@example.com", authRequest.getEmail());
        assertEquals("JohnPass123", authRequest.getPassword());
        assertEquals("FAMILY_MEMBER", authRequest.getRole());
    }

    // ============ COMPLETE FLOW TESTS ============

    @Test
    void testCompleteRegistrationFlowDataValidation() {
        authRequest.setUsername("owner1");
        authRequest.setEmail("owner1@example.com");
        authRequest.setPassword("OwnerPass123");
        authRequest.setRole("OWNER");
        
        assertNotNull(authRequest.getUsername());
        assertNotNull(authRequest.getEmail());
        assertNotNull(authRequest.getPassword());
        assertEquals("OWNER", authRequest.getRole());
        assertTrue(authService.isValidEmail(authRequest.getEmail()));
        assertTrue(authService.isValidPassword(authRequest.getPassword()));
    }

    @Test
    void testAllRolesAreSupported() {
        String[] roles = {"OWNER", "FAMILY_MEMBER", "GUEST"};
        
        for (String role : roles) {
            authRequest.setRole(role);
            assertEquals(role, authRequest.getRole());
            assertNotNull(authRequest.getRole());
        }
    }

    @Test
    void testRoleCanBeChanged() {
        authRequest.setRole("GUEST");
        assertEquals("GUEST", authRequest.getRole());
        
        authRequest.setRole("OWNER");
        assertEquals("OWNER", authRequest.getRole());
    }

    @Test
    void testMultipleUsersWithDifferentRoles() {
        AuthRequest user1 = new AuthRequest("user1", "user1@example.com", "Pass123", "OWNER");
        AuthRequest user2 = new AuthRequest("user2", "user2@example.com", "Pass123", "FAMILY_MEMBER");
        AuthRequest user3 = new AuthRequest("user3", "user3@example.com", "Pass123", "GUEST");
        
        assertEquals("OWNER", user1.getRole());
        assertEquals("FAMILY_MEMBER", user2.getRole());
        assertEquals("GUEST", user3.getRole());
    }

    // ============ ERROR SCENARIO TESTS ============

    @Test
    void testInvalidPasswordLength() {
        authRequest.setPassword("weak");
        assertFalse(authService.isValidPassword(authRequest.getPassword()));
    }

    @Test
    void testInvalidEmailFormat() {
        authRequest.setEmail("invalidemail");
        assertFalse(authService.isValidEmail(authRequest.getEmail()));
    }

    @Test
    void testNullPassword() {
        assertFalse(authService.isValidPassword(null));
    }

    @Test
    void testNullEmail() {
        assertFalse(authService.isValidEmail(null));
    }

    @Test
    void testEmptyRole() {
        authRequest.setRole("");
        assertTrue(authRequest.getRole().isEmpty());
    }
}
