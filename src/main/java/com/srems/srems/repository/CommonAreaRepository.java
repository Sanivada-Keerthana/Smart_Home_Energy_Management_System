package com.srems.srems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srems.srems.model.CommonArea;
import java.util.Optional;

@Repository
public interface CommonAreaRepository extends JpaRepository<CommonArea, Long> {

    boolean existsByAreaName(String areaName);
    Optional<CommonArea> findByAreaNameIgnoreCase(String areaName);
}
