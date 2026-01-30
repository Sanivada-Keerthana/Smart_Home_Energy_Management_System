package com.srems.srems.dto;

public class EnergySummary {
    public String blockName;
    public String flatNumber; // may be null for common area
    public Double totalConsumption;

    public EnergySummary(String blockName, String flatNumber, Double totalConsumption) {
        this.blockName = blockName;
        this.flatNumber = flatNumber;
        this.totalConsumption = totalConsumption;
    }
}
