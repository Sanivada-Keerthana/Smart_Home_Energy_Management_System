package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    /* ================= PRIMARY KEY ================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /* ================= BASIC DETAILS ================= */

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /* ================= ROLE ================= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /* ================= STRUCTURE ================= */

    @Column(nullable = true)
    private Long blockId; // null for admin

    @ManyToOne
    @JoinColumn(name = "flat_id")
    private Flat flat;   // NULL for admin / staff

    /* ================= STATUS ================= */

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = false)
    private boolean active;

    /* ================= AUDIT ================= */

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    /* ================= RESET ================= */

    private String resetToken;
    private LocalDateTime tokenExpiry;

    /* ================= CONSTRUCTORS ================= */

    public User() {
        // required by JPA
    }

    // Used during normal registration
    public User(String username,
                String email,
                String password,
                Role role,
                Long blockId,
                Flat flat) {

        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.blockId = blockId;
        this.flat = flat;

        this.active = true;
        this.approved = false;
    }

    /* ================= JPA LIFECYCLE ================= */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ================= GETTERS ================= */

    public Long getUserId() { return userId; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public Role getRole() { return role; }

    public Long getBlockId() { return blockId; }

    public Flat getFlat() { return flat; }

    public Long getFlatId() { return flat != null ? flat.getFlatId() : null; }

    public boolean isApproved() { return approved; }

    public boolean isActive() { return active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getResetToken() { return resetToken; }

    public LocalDateTime getTokenExpiry() { return tokenExpiry; }

    /* ================= SETTERS ================= */

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    /* ===== SYSTEM / ADMIN SETTERS ===== */

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

}