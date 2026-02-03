package com.srems.srems.model;

public class RecommendationDTO {

    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private String status;

    private double consumption; // kWh
    private double activeHours;
    private String reason;
    private String suggestion;
    private int score;

    public RecommendationDTO() {}

    public RecommendationDTO(Long deviceId, String deviceName, String deviceType, String status, double consumption, double activeHours, String reason, String suggestion, int score) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.status = status;
        this.consumption = consumption;
        this.activeHours = activeHours;
        this.reason = reason;
        this.suggestion = suggestion;
        this.score = score;
    }

    public Long getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getDeviceType() { return deviceType; }
    public String getStatus() { return status; }
    public double getConsumption() { return consumption; }
    public double getActiveHours() { return activeHours; }
    public String getReason() { return reason; }
    public String getSuggestion() { return suggestion; }
    public int getScore() { return score; }

    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public void setStatus(String status) { this.status = status; }
    public void setConsumption(double consumption) { this.consumption = consumption; }
    public void setActiveHours(double activeHours) { this.activeHours = activeHours; }
    public void setReason(String reason) { this.reason = reason; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public void setScore(int score) { this.score = score; }
}
