package com.datn.user_service.repository;

import com.datn.user_service.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Page<Teacher> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<Teacher> findByEmailContainingIgnoreCase(String username, Pageable pageable);

}
