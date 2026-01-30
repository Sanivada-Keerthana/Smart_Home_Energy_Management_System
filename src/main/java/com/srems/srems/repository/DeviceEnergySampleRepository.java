package com.srems.srems.repository;

import com.srems.srems.model.DeviceEnergySample;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceEnergySampleRepository extends JpaRepository<DeviceEnergySample, Long> {

    List<DeviceEnergySample> findByDeviceIdOrderByTimestampDesc(Long deviceId, Pageable pageable);

    List<DeviceEnergySample> findByTimestampOrderByDeviceIdAsc(Long timestamp);

    @Query("SELECT DISTINCT s.timestamp FROM DeviceEnergySample s ORDER BY s.timestamp DESC")
    List<Long> findDistinctTimestamps(Pageable pageable);

    List<DeviceEnergySample> findByTimestamp(Long timestamp);
}
