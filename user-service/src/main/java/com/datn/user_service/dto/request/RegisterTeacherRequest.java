package com.datn.user_service.dto.request;

import com.datn.user_service.model.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Register request for teachers")
public class RegisterTeacherRequest extends RegisterUser {
    private String specialization;
    private int yearsOfExperience;
    private Teacher.Degree degree;

    public void setRole(String role) {
        super.setRole("teacher");
    }
}
