package com.datn.user_service.repository;

import com.datn.user_service.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);

    Page<Student> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<Student> findByEmailContainingIgnoreCase(String username, Pageable pageable);

}
