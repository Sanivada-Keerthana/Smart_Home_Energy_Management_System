package com.shems.shems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String deviceType; // Light, AC, Fan

    @Column(nullable = false)
    private String status = "OFF"; // ON / OFF

    @Column(nullable = false)
    private Double consumption = 0.0; // kWh

    private LocalDateTime lastOnTime;
    private LocalDateTime lastSeen;

    private String icon;
    private Integer intensity;
    private Integer temperature;

    @PrePersist
    public void onCreate() {
        lastSeen = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        lastSeen = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getConsumption() { return consumption; }
    public void setConsumption(Double consumption) { this.consumption = consumption; }

    public LocalDateTime getLastOnTime() { return lastOnTime; }
    public void setLastOnTime(LocalDateTime lastOnTime) { this.lastOnTime = lastOnTime; }

    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }


    public LocalDateTime getLastSeen() { return lastSeen; }
}
