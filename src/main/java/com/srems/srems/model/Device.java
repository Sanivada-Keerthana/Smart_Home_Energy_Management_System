package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long id;

    /* ---------- BASIC DEVICE INFO ---------- */

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String deviceType;   // Light, Fan, AC, etc.

    /* ---------- LOCATION MAPPING ---------- */

    @Column
    private Long flatId;          // NULL → Common area device

    /* ---------- DEVICE STATE ---------- */

    @Column(nullable = false)
    private String status = "OFF";   // ON / OFF

    @Column(nullable = false)
    private Double powerRating;      // Watts (e.g., 60W, 1.5kW)

    @Column(nullable = false)
    private Double consumption = 0.0; // kWh (calculated)

    // total accumulated active time in seconds (simulated time)
    @Column(nullable = false)
    private Long totalActiveSeconds = 0L;

    /* ---------- DEVICE METADATA ---------- */

    private LocalDateTime lastOnTime;
    private LocalDateTime lastSeen;

    private String icon;          // UI icon
    private Integer intensity;    // For lights
    private Integer temperature;  // For AC

    /* ---------- AUDIT ---------- */

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* ---------- LIFECYCLE CALLBACKS ---------- */

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
        if (this.totalActiveSeconds == null) this.totalActiveSeconds = 0L;
        if (this.consumption == null) this.consumption = 0.0;
        if (this.powerRating == null) this.powerRating = 100.0;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastSeen = LocalDateTime.now();
    }

    /* ---------- GETTERS & SETTERS ---------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getFlatId() {
        return flatId;
    }

    public void setFlatId(Long flatId) {
        this.flatId = flatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;

        if ("ON".equalsIgnoreCase(status)) {
            this.lastOnTime = LocalDateTime.now();
        }
    }

    public Double getPowerRating() {
        return powerRating;
    }

    public void setPowerRating(Double powerRating) {
        this.powerRating = powerRating;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public Long getTotalActiveSeconds() {
        return totalActiveSeconds;
    }

    public void setTotalActiveSeconds(Long totalActiveSeconds) {
        this.totalActiveSeconds = totalActiveSeconds;
    }

    public LocalDateTime getLastOnTime() {
        return lastOnTime;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setLastOnTime(LocalDateTime lastOnTime) {
        this.lastOnTime = lastOnTime;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}