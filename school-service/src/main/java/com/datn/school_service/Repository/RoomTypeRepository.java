package com.datn.school_service.Repository;

import com.datn.school_service.Models.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType,Long> {

    boolean existsByRoomTypeName(String name);

    List<RoomType> findAllByIsActiveTrueAndRoomTypeNameContainingIgnoreCase(String keyword);

    List<RoomType> findAllByIsActiveFalseAndRoomTypeNameContainingIgnoreCase(String keyword);

    Page<RoomType> findAllByIsActiveTrue(Pageable pageable);

    Page<RoomType> findAllByIsActiveFalse(Pageable pageable);
}
