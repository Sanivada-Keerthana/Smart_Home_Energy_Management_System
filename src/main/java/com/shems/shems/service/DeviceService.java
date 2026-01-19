package com.shems.shems.service;

import com.shems.shems.model.Device;
import com.shems.shems.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository repository;

    // POWER RATINGS (Watts)
    private double getPower(String type) {
        return switch (type.toUpperCase()) {
            case "AC" -> 1500;
            case "FAN" -> 150;
            case "LIGHT" -> 50;
            default -> 100;
        };
    }

    // VIEW
    public List<Device> getAllDevices() {
        return repository.findAll();
    }

    // ADD (OWNER)
    public Device addDevice(Device device) {
		device.setConsumption(0.0);
		device.setLastOnTime(null);
		return repository.save(device);
	}


    // TOGGLE STATUS
	public Device updateStatus(Long id, String newStatus) {

		Device device = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found"));

		LocalDateTime now = LocalDateTime.now();

		// 🔴 Turning OFF → calculate energy
		if ("OFF".equals(newStatus) && "ON".equals(device.getStatus())) {

			LocalDateTime lastOn = device.getLastOnTime();

			if (lastOn != null) {
				long minutes = java.time.Duration.between(lastOn, now).toMinutes();

				double hours = minutes / 60.0;

				double powerKW = getPowerRating(device.getDeviceType()) / 1000.0;

				double energyUsed = powerKW * hours;

				device.setConsumption(device.getConsumption() + energyUsed);
			}

			device.setLastOnTime(null);
		}

		// 🟢 Turning ON → store start time
		if ("ON".equals(newStatus)) {
			device.setLastOnTime(now);
		}

		device.setStatus(newStatus);
		device.setLastSeen(now);

		return repository.save(device);
	}

	private double getPowerRating(String type) {
		if (type == null) return 100;

		type = type.toLowerCase();

		if (type.contains("ac")) return 1500;
		if (type.contains("fan")) return 150;
		if (type.contains("light")) return 50;

		return 100;
	}


    // UPDATE DETAILS (OWNER)
    public Device updateDevice(Long id, Device updated) {
		Device device = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found"));

		device.setDeviceName(updated.getDeviceName());
		device.setDeviceType(updated.getDeviceType());

		return repository.save(device);
	}


    // DELETE (OWNER)
    public void deleteDevice(Long id) {
        repository.deleteById(id);
    }
}
