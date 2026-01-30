package com.srems.srems.service;

import com.srems.srems.model.Device;
import com.srems.srems.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.srems.srems.model.DeviceEnergySample;
import com.srems.srems.repository.DeviceEnergySampleRepository;

@Service
public class EnergyService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceEnergySampleRepository sampleRepository;

    // Run every 5 seconds and account for simulated time: 1 second == 10 minutes
    @Scheduled(fixedRate = 5000)
    public void accumulateEnergyForOnDevices() {
        List<Device> onDevices = deviceRepository.findByStatus("ON");
        if (onDevices == null || onDevices.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        long ts = now.toEpochSecond(java.time.ZoneOffset.UTC);

        for (Device d : onDevices) {
            // Simulated period: 5 seconds
            long seconds = 5L;

            // Convert seconds to hours with mapping: 1 sec = 10 minutes => hours = seconds * (10/60) = seconds/6
            double hours = seconds / 6.0;
            double powerKW = (d.getPowerRating() == null ? 0.0 : d.getPowerRating()) / 1000.0;

            double added = powerKW * hours; // kWh

            d.setConsumption((d.getConsumption() == null ? 0.0 : d.getConsumption()) + added);
            d.setTotalActiveSeconds((d.getTotalActiveSeconds() == null ? 0L : d.getTotalActiveSeconds()) + seconds);
            d.setLastSeen(now);

            // Move lastOnTime forward as heartbeat so toggling OFF won't double-count much
            d.setLastOnTime(now);

            // Persist a sample (cumulative)
            try {
                DeviceEnergySample s = new DeviceEnergySample(d.getId(), ts, d.getConsumption());
                sampleRepository.save(s);
            } catch (Exception ignored) { }
        }

        deviceRepository.saveAll(onDevices);
    }
}