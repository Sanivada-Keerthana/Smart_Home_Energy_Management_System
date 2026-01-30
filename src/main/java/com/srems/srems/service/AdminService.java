package com.srems.srems.service;

import com.srems.srems.model.*;
import com.srems.srems.repository.*;
import com.srems.srems.dto.DashboardStats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired private ApprovalRepository approvalRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BlockRepository blockRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private SystemSettingsRepository systemSettingsRepository;
    @Autowired private CommonAreaRepository commonAreaRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private com.srems.srems.repository.DeviceRepository deviceRepository;

    public java.util.List<com.srems.srems.dto.EnergySummary> getEnergySummary() {
        var flats = flatRepository.findAll();
        var devices = deviceRepository.findAll();

        java.util.List<com.srems.srems.dto.EnergySummary> out = new java.util.ArrayList<>();

        for (var f : flats) {
            double total = devices.stream()
                    .filter(d -> d.getFlatId() != null && d.getFlatId().equals(f.getFlatId()))
                    .mapToDouble(d -> d.getConsumption() == null ? 0.0 : d.getConsumption())
                    .sum();

            out.add(new com.srems.srems.dto.EnergySummary(
                    f.getBlock().getBlockName(),
                    f.getFlatNumber(),
                    total
            ));
        }

        return out;
    }

    public java.util.List<com.srems.srems.dto.EnergySummary> getBlockEnergySummary() {
        var devices = deviceRepository.findAll();
        var blocks = blockRepository.findAll();

        java.util.List<com.srems.srems.dto.EnergySummary> out = new java.util.ArrayList<>();

        for (var b : blocks) {
            double total = devices.stream()
                    .filter(d -> d.getFlatId() != null)
                    .filter(d -> {
                        // map device -> flat -> block
                        return flatRepository.findById(d.getFlatId())
                                .map(f -> f.getBlock().getBlockId().equals(b.getBlockId()))
                                .orElse(false);
                    })
                    .mapToDouble(d -> d.getConsumption() == null ? 0.0 : d.getConsumption())
                    .sum();

            out.add(new com.srems.srems.dto.EnergySummary(
                    b.getBlockName(),
                    null,
                    total
            ));
        }

        return out;
    }

    public double getCommonAreaConsumption() {
        return deviceRepository.findByFlatIdIsNull()
                .stream()
                .mapToDouble(d -> d.getConsumption() == null ? 0.0 : d.getConsumption())
                .sum();
    }

    /* ================= DASHBOARD ================= */

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.blocks = blockRepository.count();
        stats.flats = flatRepository.count();
        stats.users = userRepository.count();
        stats.pending = approvalRepository.countByStatus(ApprovalStatus.PENDING);
        stats.security = userRepository.countByRole(Role.SECURITY);
        return stats;
    }

    /* ================= APPROVALS ================= */

    public List<Approval> getPendingApprovals() {
        return approvalRepository.findByStatus(ApprovalStatus.PENDING);
    }

    public void approveUser(Long approvalId) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        User user = approval.getRequestedUser();

        user.setApproved(true);
        user.setActive(true);
        userRepository.save(user);

        approval.approve();
        approvalRepository.save(approval);
    }

    public void rejectUser(Long approvalId) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        User user = approval.getRequestedUser();

        user.setApproved(false);
        user.setActive(false);
        userRepository.save(user);

        approval.reject();
        approvalRepository.save(approval);
    }

    /* ================= BLOCK ================= */

    public void createBlock(String blockName) {
        if (blockRepository.existsByBlockName(blockName))
            throw new RuntimeException("Block already exists");
        blockRepository.save(new Block(blockName));
    }

    public List<Block> getAllBlocks() {
        return blockRepository.findAll();
    }

    /* ================= FLAT ================= */

    public void createFlat(Long blockId, String flatNumber) {

        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new RuntimeException("Block not found"));

        if (flatRepository.existsByBlock_BlockIdAndFlatNumber(blockId, flatNumber))
            throw new RuntimeException("Flat already exists");

        flatRepository.save(new Flat(block, flatNumber));
    }

    public List<Flat> getAllFlats() {
        return flatRepository.findAll();
    }

    /* ================= COMMON AREA ================= */

    public void createCommonArea(String areaName) {
        if (commonAreaRepository.existsByAreaName(areaName))
            throw new RuntimeException("Common area already exists");
        commonAreaRepository.save(new CommonArea(areaName));
    }

    public List<CommonArea> getAllCommonAreas() {
        return commonAreaRepository.findAll();
    }

    /* ================= USERS ================= */

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    /* ================= SETTINGS ================= */

    public SystemSettings getSettings() {
        return systemSettingsRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> systemSettingsRepository.save(new SystemSettings()));
    }

    public void saveSettings(SystemSettings settings) {
        systemSettingsRepository.save(settings);
    }

    /* ================= PROFILE ================= */

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateProfile(String oldUsername,
                              String newUsername,
                              String email,
                              String password,
                              String confirmPassword) {

        User user = getUserByUsername(oldUsername);

        if (!oldUsername.equals(newUsername) &&
            userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already exists");
        }

        user.setUsername(newUsername);
        user.setEmail(email);

        if (password != null && !password.isEmpty()) {
            if (!password.equals(confirmPassword))
                throw new RuntimeException("Passwords do not match");
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }
}