package com.datn.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "students")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {
    @OneToMany(mappedBy = "student")
    private List<ParentStudent> parents;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        STUDYING,
        GRADUATED,
        DROPPED_OUT
    }
}