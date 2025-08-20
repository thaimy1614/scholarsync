package com.datn.user_service.dto.response;

import com.datn.user_service.model.Teacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TeacherResponse extends UserResponse {
    private String specialization;
    private int yearsOfExperience;
    private Teacher.Degree degree;
    private Teacher.Status status;
}
