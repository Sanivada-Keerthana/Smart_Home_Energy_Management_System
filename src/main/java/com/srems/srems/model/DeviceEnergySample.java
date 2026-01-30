package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_energy_samples")
public class DeviceEnergySample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long deviceId;

    @Column(nullable = false)
    private Long timestamp; // epoch seconds

    @Column(nullable = false)
    private Double totalConsumption; // cumulative kWh

    public DeviceEnergySample() {}

    public DeviceEnergySample(Long deviceId, Long timestamp, Double totalConsumption) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.totalConsumption = totalConsumption;
    }

    public Long getId() { return id; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public Double getTotalConsumption() { return totalConsumption; }
    public void setTotalConsumption(Double totalConsumption) { this.totalConsumption = totalConsumption; }
}
