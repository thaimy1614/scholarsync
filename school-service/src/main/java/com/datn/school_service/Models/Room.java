package com.datn.school_service.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", unique = true, length = 100)
    private String roomName;

    @Column(name = "room_floor")
    private Long roomFloor;

    @Column(name = "number_of_chalkboard")
    private Long numberOfChalkboard;

    @Column(name = "number_of_device")
    private Long numberOfDevice;

    @JoinColumn(name = "room_type_id")
    @ManyToOne
    private RoomType roomType;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "room")
    private Set<Class> clazz = new HashSet<>();
}
