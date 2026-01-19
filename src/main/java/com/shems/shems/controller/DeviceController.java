package com.shems.shems.controller;

import com.shems.shems.model.Device;
import com.shems.shems.service.DeviceService;
import jakarta.servlet.http.HttpSession;   // ✅ REQUIRED IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@CrossOrigin
public class DeviceController {

    @Autowired
    private DeviceService service;

    // ✅ ALL roles can VIEW devices
    @GetMapping
    public List<Device> getAll(HttpSession session) {
        return service.getAllDevices();
    }

    // 🔒 ONLY OWNER can ADD device
    @PostMapping
    public Device add(@RequestBody Device device, HttpSession session) {
        String role = (String) session.getAttribute("role");

        if (!"OWNER".equals(role)) {
            throw new RuntimeException("Access denied: Only OWNER can add devices");
        }
        return service.addDevice(device);
    }

    // 🔒 OWNER & FAMILY_MEMBER can TOGGLE
    @PutMapping("/{id}/toggle")
    public Device toggleStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session) {

        String role = (String) session.getAttribute("role");

        if (!("OWNER".equals(role) || "FAMILY_MEMBER".equals(role))) {
            throw new RuntimeException("Access denied: Cannot toggle device");
        }

        return service.updateStatus(id, status);
    }

    // 🔒 ONLY OWNER can UPDATE DEVICE DETAILS
    @PutMapping("/{id}")
    public Device updateDevice(
            @PathVariable Long id,
            @RequestBody Device updated,
            HttpSession session) {

        String role = (String) session.getAttribute("role");

        if (!"OWNER".equals(role)) {
            throw new RuntimeException("Access denied: Only OWNER can update device details");
        }

        return service.updateDevice(id, updated);
    }

    // 🔒 ONLY OWNER can DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpSession session) {

        String role = (String) session.getAttribute("role");

        if (!"OWNER".equals(role)) {
            throw new RuntimeException("Access denied: Only OWNER can delete devices");
        }

        service.deleteDevice(id);
    }
}
