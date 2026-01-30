package com.srems.srems.dto;

public class AuthRequest {

    private String username;
    private String email;
    private String password;
    private String role;

    private Long blockId;

    private String flatNumber;

    public AuthRequest() {}

    // 🔐 LOGIN
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 📝 REGISTER
    public AuthRequest(
            String username,
            String email,
            String password,
            String role,
            Long blockId,
            String flatNumber
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.blockId = blockId;
        this.flatNumber = flatNumber;
    }

    /* ---------- GETTERS & SETTERS ---------- */

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public Long getBlockId() {
        return blockId;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }
}