package com.datn.school_service.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordCollectiveViolations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_collective_violations_id")
    private Long recordCollectiveViolationsId;

    @Column(name = "redflag_id")
    private String redFlagId;

    @Column(name = "absent_count",nullable = true)
    private int absentCount;

    @ManyToMany
    @JoinTable(
            name = "record_collective_violation_type",
            joinColumns = @JoinColumn(name = "record_collective_violations_id"),
            inverseJoinColumns = @JoinColumn(name = "violation_type_id")
    )
    private List<ViolationType> violationTypes;

    private String principalId;

    @ManyToOne
    @JoinColumn(name = "class_id",nullable = true)
    private Class clazz;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recordCollectiveViolations")
    @JsonIgnore
    private List<RecordPersonalViolations> personalViolations;

}
