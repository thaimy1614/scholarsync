package com.datn.user_service.repository;

import com.datn.user_service.model.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<Parent, String> {
    Page<Parent> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<Parent> findByEmailContainingIgnoreCase(String username, Pageable pageable);
}
