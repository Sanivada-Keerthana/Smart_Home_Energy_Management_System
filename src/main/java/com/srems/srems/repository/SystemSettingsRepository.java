package com.srems.srems.repository;

import com.srems.srems.model.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository
        extends JpaRepository<SystemSettings, Long> {
}