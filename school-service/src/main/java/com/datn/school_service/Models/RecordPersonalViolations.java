package com.datn.school_service.Models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Entity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.List;


@Getter
@Setter
//@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordPersonalViolations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_personal_violations_id")
    private Long recordPersonalViolationsId;

    @Column(name = "violation_student_id",nullable = true)
    private String violationStudentId;

    @ManyToMany
    @JoinTable(
            name = "record_personal_violation_type",
            joinColumns = @JoinColumn(name = "record_personal_violations_id"),
            inverseJoinColumns = @JoinColumn(name = "violation_type_id")
    )
    private List<ViolationType> violationTypes;


    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "record_collective_violations_id")
    private RecordCollectiveViolations recordCollectiveViolations;



}
