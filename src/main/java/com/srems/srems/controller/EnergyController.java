package com.srems.srems.controller;

import com.srems.srems.model.Device;
import com.srems.srems.model.User;
import com.srems.srems.service.DeviceService;
import com.srems.srems.repository.DeviceRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin(origins = "*")
public class EnergyController {

    @Autowired private DeviceService deviceService;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private com.srems.srems.repository.DeviceEnergySampleRepository sampleRepository;

    @Autowired private com.srems.srems.service.RecommendationService recommendationService;

    // User-level: list devices with energy info (only devices the user can access)
    @GetMapping("/devices")
    public List<Device> getDevicesWithEnergy(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        return deviceService.getDevicesForUser(user);
    }

    @GetMapping("/device/{id}")
    public Device getDevice(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        Device d = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        // Access checks
        if (d.getFlatId() != null) {
            // flat device: only users of same flat (owner/member/guest) can access
            if (user.getFlat() == null || !d.getFlatId().equals(user.getFlat().getFlatId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
            }
        } else {
            // common device: only secretary, security, staff allowed or admin
            if (user.getRole().name().equals("ADMIN")) return d;
            if (!(user.getRole().name().equals("SECRETARY") || user.getRole().name().equals("SECURITY") || user.getRole().name().equals("STAFF"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
            }
        }

        return d;
    }

    @GetMapping("/device/{id}/timeseries")
    public List<java.util.Map<String,Object>> deviceTimeseries(@PathVariable Long id,
                                                               @RequestParam(defaultValue = "60") int limit,
                                                               HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        // access check - reuse getDevice logic
        Device d = getDevice(id, session);

        var samples = sampleRepository.findByDeviceIdOrderByTimestampDesc(id, org.springframework.data.domain.PageRequest.of(0, limit));

        // convert to ascending order
        java.util.Collections.reverse(samples);

        java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
        Double prev = null;
        for (var s : samples) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("timestamp", s.getTimestamp());
            m.put("totalConsumption", s.getTotalConsumption());
            if (prev == null) m.put("delta", 0.0);
            else m.put("delta", s.getTotalConsumption() - prev);
            prev = s.getTotalConsumption();
            out.add(m);
        }

        return out;
    }

    @GetMapping("/recommendations")
    public java.util.List<com.srems.srems.model.RecommendationDTO> recommendations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Session expired");

        return recommendationService.generateForUser(user);
    }
}