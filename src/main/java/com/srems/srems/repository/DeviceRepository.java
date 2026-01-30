package com.srems.srems.repository;

import com.srems.srems.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByFlatId(Long flatId);

    Optional<Device> findByIdAndFlatId(Long id, Long flatId);

    List<Device> findByFlatIdIsNull();

    // devices currently ON
    List<Device> findByStatus(String status);
}