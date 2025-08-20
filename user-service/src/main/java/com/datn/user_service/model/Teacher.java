package com.datn.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "teachers")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends User {
    private String specialization;

    @Column(name = "years_of_experience", nullable = true)
    private int yearsOfExperience;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Degree degree;

    public enum Degree {
        BACHELOR,
        MASTER,
        DOCTORATE,
        ASSOCIATE_PROFESSOR,
        PROFESSOR
    }

    public enum Status {
        WORKING,
        ON_LEAVE,
        RETIRED
    }
}
