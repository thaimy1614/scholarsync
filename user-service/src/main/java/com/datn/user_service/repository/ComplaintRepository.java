package com.datn.user_service.repository;

import com.datn.user_service.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findAllBySenderId(String senderId, Pageable pageable);

    Page<Complaint> findByContentContainingOrResponseContentContainingOrSenderIdContaining(String content, String responseContent, String senderId, Pageable pageable);
}
