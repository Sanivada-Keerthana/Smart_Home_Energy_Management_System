package com.srems.srems.dto;

public class AuthResponse {

    private boolean success;
    private String message;

    private String username;
    private String email;
    private String role;

    private boolean approved;
    private Long commonAreaId;
    private String dashboardType;
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(
            boolean success,
            String message,
            String username,
            String email,
            String role,
            boolean approved,
            String dashboardType
    ) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.email = email;
        this.role = role;
        this.approved = approved;
        this.dashboardType = dashboardType;
    }

    /* ---------- GETTERS & SETTERS ---------- */

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public String getRole() {
        return role;
    }
 
    public void setRole(String role) {
        this.role = role;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getDashboardType() {
        return dashboardType;
    }

    public void setDashboardType(String dashboardType) {
        this.dashboardType = dashboardType;
    }
}