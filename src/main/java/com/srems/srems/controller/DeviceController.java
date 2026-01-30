package com.srems.srems.controller;

import com.srems.srems.model.Device;
import com.srems.srems.model.User;
import com.srems.srems.service.DeviceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping
    public Device addDevice(@RequestBody Device device, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        return deviceService.addDevice(
                device,
                user.getRole().name(),
                user.getFlat() != null ? user.getFlat().getFlatId() : null
        );
    }

    @GetMapping
    public List<Device> getDevices(HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        return deviceService.getDevicesForUser(user);
    }

    @PutMapping("/{id}/toggle")
    public Device toggleDevice(@PathVariable Long id,
                               @RequestParam String status,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        return deviceService.toggleDevice(
                id,
                status,
                user.getRole().name(),
                user.getFlat() != null ? user.getFlat().getFlatId() : null
        );
    }

    @PutMapping("/{id}")
    public Device updateDevice(@PathVariable Long id,
                               @RequestBody Device updated,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        return deviceService.updateDevice(
                id,
                updated,
                user.getRole().name(),
                user.getFlat() != null ? user.getFlat().getFlatId() : null
        );
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id,
                             HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");

        deviceService.deleteDevice(
                id,
                user.getRole().name(),
                user.getFlat() != null ? user.getFlat().getFlatId() : null
        );
    }




}