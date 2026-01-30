package com.srems.srems.controller;

import com.srems.srems.model.Device;
import com.srems.srems.model.Role;
import com.srems.srems.model.User;
import com.srems.srems.repository.DeviceRepository;
import com.srems.srems.repository.FlatRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired private DeviceRepository deviceRepository;
    @Autowired private FlatRepository flatRepository;

    @GetMapping
    public List<Device> topDevices(HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        Role role = user.getRole();

        List<Device> devices = deviceRepository.findAll();

        if (role == Role.FLAT_OWNER || role == Role.FLAT_MEMBER || role == Role.FLAT_GUEST) {
            Long flatId = user.getFlat() != null ? user.getFlat().getFlatId() : null;
            return devices.stream()
                    .filter(d -> d.getFlatId() != null && d.getFlatId().equals(flatId))
                    .sorted(Comparator.comparingDouble(d -> - (d.getConsumption() == null ? 0.0 : d.getConsumption())))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        if (role == Role.SECRETARY) {
            // Devices belonging to flats in this block
            var flats = flatRepository.findAll().stream().filter(f -> f.getBlock().getBlockId().equals(user.getBlockId())).map(f->f.getFlatId()).collect(Collectors.toSet());
            return devices.stream()
                    .filter(d -> d.getFlatId() != null && flats.contains(d.getFlatId()))
                    .sorted(Comparator.comparingDouble(d -> - (d.getConsumption() == null ? 0.0 : d.getConsumption())))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Recommendations only available to owners/secretary");
    }
}
