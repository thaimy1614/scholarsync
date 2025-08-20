package com.datn.school_service.Repository;
import com.datn.school_service.Models.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomName(String roomName);

    Page<Room> findAllByActiveTrue(Pageable pageable);

    Page<Room> findAllByActiveFalse(Pageable pageable);

    List<Room> findAllByActiveTrue();

    List<Room> findAllByActiveFalse();

    List<Room> findByActiveTrueAndRoomNameContainingIgnoreCaseOrRoomFloor(String roomName, Long roomFloor);

    List<Room> findByRoomNameStartingWithIgnoreCaseAndActiveTrue(String prefix);

    List<Room> findByRoomFloorAndActiveTrue(Long roomFloor);
}

