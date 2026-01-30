package com.srems.srems.service;

import com.srems.srems.model.Device;
import com.srems.srems.model.Role;
import com.srems.srems.model.User;
import com.srems.srems.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository repository;

    /* ================= ADD DEVICE ================= */
    public Device addDevice(Device device, String role, Long flatId) {

        Role userRole = Role.valueOf(role);

        if (device.getPowerRating() == null) {
            // Set sensible defaults based on device type
            switch ((device.getDeviceType() == null ? "" : device.getDeviceType().toLowerCase())) {
                case "light", "bulb" -> device.setPowerRating(60.0);
                case "fan" -> device.setPowerRating(75.0);
                case "ac", "air conditioner" -> device.setPowerRating(1500.0);
                case "heater" -> device.setPowerRating(2000.0);
                case "fridge", "refrigerator" -> device.setPowerRating(150.0);
                case "tv", "television" -> device.setPowerRating(100.0);
                case "pump" -> device.setPowerRating(500.0);
                default -> device.setPowerRating(100.0);
            }
        }

        device.setConsumption(0.0);
        device.setLastOnTime(null);
        device.setStatus("OFF");

        if (userRole == Role.FLAT_OWNER) {
            device.setFlatId(flatId);
            return repository.save(device);
        }

        if (userRole == Role.SECRETARY) {
            device.setFlatId(null);
            return repository.save(device);
        }

        throw new RuntimeException("Access denied");
    }

    /* ================= VIEW DEVICES ================= */
    public List<Device> getDevicesForUser(User user) {

        Long flatId = user.getFlat() != null
                ? user.getFlat().getFlatId()
                : null;

        return switch (user.getRole()) {
            case FLAT_OWNER, FLAT_MEMBER, FLAT_GUEST ->
                    repository.findByFlatId(flatId);

            case SECRETARY, SECURITY, STAFF ->
                    repository.findByFlatIdIsNull();

            default -> List.of();
        };
    }

    /* ================= TOGGLE DEVICE ================= */
    public Device toggleDevice(Long id, String status, String role, Long flatId) {

        Role userRole = Role.valueOf(role);

        if (userRole == Role.FLAT_GUEST) {
            throw new RuntimeException("Guests cannot control devices");
        }

        Device device = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (device.getFlatId() != null &&
                (userRole == Role.FLAT_OWNER || userRole == Role.FLAT_MEMBER)) {

            if (!device.getFlatId().equals(flatId)) {
                throw new RuntimeException("Unauthorized flat access");
            }
        }

        LocalDateTime now = LocalDateTime.now();

        if ("OFF".equals(status) && "ON".equals(device.getStatus())
                && device.getLastOnTime() != null) {

            long seconds = java.time.Duration
                    .between(device.getLastOnTime(), now)
                    .toSeconds();

            // 1 second => 10 minutes => hours = seconds * (10/60) = seconds / 6
            double hours = seconds / 6.0;
            double powerKW = device.getPowerRating() / 1000.0;

            device.setConsumption(
                    device.getConsumption() + (powerKW * hours));
        }

        if ("ON".equals(status)) {
            device.setLastOnTime(now);
        } else {
            device.setLastOnTime(null);
        }

        device.setStatus(status);
        device.setLastSeen(now);

        return repository.save(device);
    }

    /* ================= UPDATE DEVICE ================= */
    public Device updateDevice(Long id, Device updated, String role, Long flatId) {

        if (Role.valueOf(role) != Role.FLAT_OWNER) {
            throw new RuntimeException("Only flat owner can update devices");
        }

        Device device = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (!device.getFlatId().equals(flatId)) {
            throw new RuntimeException("Unauthorized flat access");
        }

        device.setDeviceName(updated.getDeviceName());
        device.setDeviceType(updated.getDeviceType());
        device.setPowerRating(updated.getPowerRating());

        return repository.save(device);
    }

    /* ================= DELETE DEVICE ================= */
    public void deleteDevice(Long id, String role, Long flatId) {

        Role userRole = Role.valueOf(role);

        Device device = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (device.getFlatId() != null) {
            if (userRole != Role.FLAT_OWNER ||
                    !device.getFlatId().equals(flatId)) {
                throw new RuntimeException("Access denied");
            }
        }

        if (device.getFlatId() == null && userRole != Role.SECRETARY) {
            throw new RuntimeException("Access denied");
        }

        repository.deleteById(id);
    }
}