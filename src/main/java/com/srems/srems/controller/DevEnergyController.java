package com.srems.srems.controller;

import com.srems.srems.model.DeviceEnergySample;
import com.srems.srems.repository.DeviceEnergySampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dev/energy")
public class DevEnergyController {

    @Autowired
    private DeviceEnergySampleRepository sampleRepository;

    // Dev helper: return timeseries for a device (no auth) for debugging only
    @GetMapping("/device/{deviceId}/timeseries")
    public List<DeviceEnergySample> deviceTimeseries(@PathVariable Long deviceId) {
        return sampleRepository.findByDeviceIdOrderByTimestampDesc(deviceId, org.springframework.data.domain.PageRequest.of(0, 200));
    }
}
