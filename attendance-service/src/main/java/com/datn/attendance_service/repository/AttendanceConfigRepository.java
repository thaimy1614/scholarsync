package com.datn.attendance_service.repository;

import com.datn.attendance_service.model.AttendanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceConfigRepository extends JpaRepository<AttendanceConfig, Long> {
    Optional<AttendanceConfig> findByConfigKey(String configKey);
}
