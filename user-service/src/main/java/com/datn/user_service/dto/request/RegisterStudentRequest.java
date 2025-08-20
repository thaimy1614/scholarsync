package com.datn.user_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Register request for students")
public class RegisterStudentRequest extends RegisterUser {
    public void setRole(String role) {
        super.setRole("student");
    }
}
