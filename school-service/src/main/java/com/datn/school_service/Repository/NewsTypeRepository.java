package com.datn.school_service.Repository;

import com.datn.school_service.Models.NewsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NewsTypeRepository extends JpaRepository<NewsType,Long> {
    Page<NewsType> findAllByIsActiveTrue(Pageable pageable);

    Page<NewsType> findAllByIsActiveFalse(Pageable pageable);

    boolean existsByNewsTypeIdAndIsActiveTrue(Long id);

    boolean existsByNewsTypeName(String name);

    List<NewsType> findAllByIsActiveTrueAndNewsTypeNameContainingIgnoreCase(String keyword);

    List<NewsType> findAllByIsActiveFalseAndNewsTypeNameContainingIgnoreCase(String keyword);
}
