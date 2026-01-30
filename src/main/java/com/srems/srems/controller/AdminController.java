package com.srems.srems.controller;

import com.srems.srems.model.*;
import com.srems.srems.dto.DashboardStats;
import com.srems.srems.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.srems.srems.repository.DeviceEnergySampleRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DeviceEnergySampleRepository deviceEnergySampleRepository;

    @Autowired
    private com.srems.srems.repository.DeviceRepository deviceRepository;

    @Autowired
    private com.srems.srems.repository.BlockRepository blockRepository;

    @Autowired
    private com.srems.srems.repository.FlatRepository flatRepository;

    /* ================= DASHBOARD ================= */

    // 📊 Dashboard summary cards
    @GetMapping("/dashboard")
    public DashboardStats getDashboardStats() {
        return adminService.getDashboardStats();
    }

    /* ================= APPROVALS ================= */

    // 🔎 Get all pending approvals
    @GetMapping("/approvals")
    public List<Approval> getPendingApprovals() {
        return adminService.getPendingApprovals();
    }

    // ✅ Approve a user
    @PostMapping("/approve/{approvalId}")
    public void approve(@PathVariable Long approvalId) {
        adminService.approveUser(approvalId);
    }

    // 📊 Energy summary (flat wise)
    @GetMapping("/energy-summary")
    public List<com.srems.srems.dto.EnergySummary> getEnergySummary() {
        return adminService.getEnergySummary();
    }

    // 📊 Energy summary (block wise)
    @GetMapping("/energy-block-summary")
    public List<com.srems.srems.dto.EnergySummary> getBlockEnergySummary() {
        return adminService.getBlockEnergySummary();
    }

    // 📊 Common area consumption total
    @GetMapping("/energy-common")
    public double getCommonAreaConsumption() {
        return adminService.getCommonAreaConsumption();
    }

    // 📊 Block timeseries (timestamps + per-block series)
    @GetMapping("/energy/block-timeseries")
    public java.util.Map<String,Object> getBlockTimeseries(@RequestParam(defaultValue = "60") int limit) {

        // 1) get distinct timestamps (most recent first)
        var timestamps = deviceEnergySampleRepository.findDistinctTimestamps(org.springframework.data.domain.PageRequest.of(0, limit));
        java.util.Collections.reverse(timestamps); // oldest -> newest

        // load devices map
        var samples = deviceEnergySampleRepository.findAllById(java.util.List.of()); // placeholder to ensure repo bean available

        java.util.Map<Long, com.srems.srems.model.Device> deviceMap = new java.util.HashMap<>();
        deviceRepository.findAll().forEach(d -> deviceMap.put(d.getId(), d));

        var blocks = blockRepository.findAll();

        java.util.List<java.util.Map<String,Object>> series = new java.util.ArrayList<>();

        for (var b : blocks) {
            java.util.Map<String,Object> row = new java.util.HashMap<>();
            row.put("blockName", b.getBlockName());
            java.util.List<Double> data = new java.util.ArrayList<>();

            for (var ts : timestamps) {
                double sum = 0.0;
                var sList = deviceEnergySampleRepository.findByTimestamp(ts);
                for (var s : sList) {
                    var dev = deviceMap.get(s.getDeviceId());
                    if (dev != null && dev.getFlatId() != null) {
                        var flatOpt = flatRepository.findById(dev.getFlatId());
                        if (flatOpt.isPresent() && flatOpt.get().getBlock().getBlockId().equals(b.getBlockId())) {
                            sum += s.getTotalConsumption() == null ? 0.0 : s.getTotalConsumption();
                        }
                    }
                }
                data.add(sum);
            }

            row.put("data", data);
            series.add(row);
        }

        java.util.Map<String,Object> out = new java.util.HashMap<>();
        out.put("timestamps", timestamps);
        out.put("series", series);
        return out;
    }

    // ❌ Reject a user
    @PostMapping("/reject/{approvalId}")
    public void reject(@PathVariable Long approvalId) {
        adminService.rejectUser(approvalId);
    }

    /* ================= BLOCK MANAGEMENT ================= */

    // 🏢 Add new block
    @PostMapping("/block")
    public void addBlock(@RequestParam String blockName) {
        adminService.createBlock(blockName);
    }

    // 📋 Get all blocks (for dropdowns)
    @GetMapping("/blocks")
    public List<Block> getAllBlocks() {
        return adminService.getAllBlocks();
    }

    @PostMapping("/admin/block")
    public String addBlock(@RequestParam String blockName,
                        RedirectAttributes redirectAttributes) {

        try {
            adminService.createBlock(blockName);
            redirectAttributes.addFlashAttribute(
                    "success", "Block added successfully"
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Block already exists"
            );
        }

        return "redirect:/admin/aptsetup";
    }

    /* ================= FLAT MANAGEMENT ================= */

    // 🏠 Add new flat
    @PostMapping("/flat")
    public void addFlat(
            @RequestParam Long blockId,
            @RequestParam String flatNumber) {

        adminService.createFlat(blockId, flatNumber);
    }

    // 📋 Get all flats
    @GetMapping("/flats")
    public List<Flat> getAllFlats() {
        return adminService.getAllFlats();
    }

    @PostMapping("/admin/flat")
    public String addFlat(@RequestParam Long blockId,
                        @RequestParam String flatNumber,
                        RedirectAttributes redirectAttributes) {

        try {
            adminService.createFlat(blockId, flatNumber);
            redirectAttributes.addFlashAttribute(
                    "success", "Flat added successfully"
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Flat already exists in this block"
            );
        }

        return "redirect:/admin/aptsetup";
    }



    /* ================= USER MANAGEMENT ================= */

    // 👤 Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    // 🚫 Disable user
    @PostMapping("/users/disable/{userId}")
    public void disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
    }

    // ✅ Enable user
    @PostMapping("/users/enable/{userId}")
    public void enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
    }


    
}