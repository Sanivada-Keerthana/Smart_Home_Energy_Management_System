package com.srems.srems.service;

import com.srems.srems.model.Device;
import com.srems.srems.model.RecommendationDTO;
import com.srems.srems.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class RecommendationService {

    @Autowired private DeviceService deviceService;

    public List<RecommendationDTO> generateForUser(User user) {
        List<Device> devices = deviceService.getDevicesForUser(user);
        if (devices == null || devices.isEmpty()) return new ArrayList<>();

        // compute simple average consumption across user's devices
        OptionalDouble avgOpt = devices.stream().mapToDouble(d -> d.getConsumption() == null ? 0.0 : d.getConsumption()).average();
        double avg = avgOpt.isPresent() ? avgOpt.getAsDouble() : 0.0;

        List<RecommendationDTO> out = new ArrayList<>();

        int currentHour = LocalDateTime.now().getHour();

        for (Device d : devices) {
            double consumption = d.getConsumption() == null ? 0.0 : d.getConsumption();
            double activeHours = (d.getTotalActiveSeconds() == null ? 0.0 : d.getTotalActiveSeconds()) / 6.0; // simulation mapping
            double powerKW = (d.getPowerRating() == null ? 0.0 : d.getPowerRating()) / 1000.0;
            double expected = powerKW * Math.max(0.0001, activeHours);

            StringBuilder reasons = new StringBuilder();
            StringBuilder suggestions = new StringBuilder();
            int score = 0;

            // Rule: High consumption vs average
            if (avg > 0 && consumption > Math.max(1.0, avg * 1.5)) {
                reasons.append("Consumes significantly more than your other devices.");
                suggestions.append("Consider reducing run time or replacing with an energy-efficient model.");
                score += 3;
            }

            // Rule: long active duration
            if (activeHours > 3.0) {
                if (reasons.length() > 0) reasons.append(" ");
                reasons.append(String.format("Active for %.1f hours.", activeHours));
                if (suggestions.length() > 0) suggestions.append(" ");
                suggestions.append("Schedule to reduce continuous run time when possible.");
                score += 2;
            }

            // Rule: running during peak hours (18-22)
            if ("ON".equalsIgnoreCase(d.getStatus()) && currentHour >= 18 && currentHour <= 22) {
                if (reasons.length() > 0) reasons.append(" ");
                reasons.append("Running during peak hours.");
                if (suggestions.length() > 0) suggestions.append(" ");
                suggestions.append("Reschedule to off-peak hours to save cost.");
                score += 2;
            }

            // Rule: consumption higher than expected
            if (expected > 0 && consumption > expected * 1.2) {
                if (reasons.length() > 0) reasons.append(" ");
                reasons.append("Measured consumption higher than expected for its power rating.");
                if (suggestions.length() > 0) suggestions.append(" ");
                suggestions.append("Check device for faults or update its power rating if inaccurate.");
                score += 3;
            }

            // If we have any reason, add recommendation
            if (reasons.length() > 0) {
                RecommendationDTO r = new RecommendationDTO();
                r.setDeviceId(d.getId());
                r.setDeviceName(d.getDeviceName());
                r.setDeviceType(d.getDeviceType());
                r.setStatus(d.getStatus());
                r.setConsumption(consumption);
                r.setActiveHours(activeHours);
                r.setReason(reasons.toString());
                r.setSuggestion(suggestions.toString());
                r.setScore(score);
                out.add(r);
            }
        }

        // sort by score desc
        out.sort((a,b) -> Integer.compare(b.getScore(), a.getScore()));
        return out;
    }
}
