package com.datn.school_service.Repository;

import com.datn.school_service.Models.News;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByNewsContent(String name);

    boolean existsByNewsTitle(String title);

    List<News> findAllByIsActiveFalseAndNewsContentContainingIgnoreCaseOrIsActiveFalseAndNewsTitleContainingIgnoreCaseOrIsActiveFalseAndNewsOwnerContainingIgnoreCase(
            String keyword1, String keyword2, String keyword3);
    List<News> findAllByIsActiveTrueAndNewsContentContainingIgnoreCaseOrIsActiveTrueAndNewsTitleContainingIgnoreCaseOrIsActiveTrueAndNewsOwnerContainingIgnoreCase(
            String keyword1, String keyword2, String keyword3);


    Page<News> findAllByIsActiveTrue(Pageable pageable);

    Page<News> findAllByIsActiveFalse(Pageable pageable);

}
