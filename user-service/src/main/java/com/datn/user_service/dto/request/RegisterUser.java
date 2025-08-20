package com.datn.user_service.dto.request;

import com.datn.user_service.model.User;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "role", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterStudentRequest.class, name = "student"),
        @JsonSubTypes.Type(value = RegisterTeacherRequest.class, name = "teacher"),
        @JsonSubTypes.Type(value = RegisterParentRequest.class, name = "parent")
})
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUser {

    @Schema(description = "User's email", example = "duongthai@example.com")
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "User's full name", example = "Dương Quốc Thái")
    @NotEmpty(message = "Full name is required")
    private String fullName;

    @Schema(description = "User's phone number", example = "0987654321")
    @NotEmpty(message = "Phone number is required")
    private String phoneNumber;

    @Schema(description = "User's address", example = "123 Nguyễn Văn Cừ")
    @NotEmpty(message = "Address is required")
    private String address;

    @Schema(description = "User's avatar", example = "https://google.com")
    private String image;

    @Schema(description = "User's gender", example = "MALE/FEMALE")
    @NotNull(message = "Gender is required")
    private User.Gender gender;

    @Schema(description = "User's date of birth", example = "2000-01-31")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Schema(description = "User's enrollment year", example = "2025")
    private Integer enrollmentYear;

    @Schema(description = "User's role, this will be set by the 'role' query parameter", example = "student/teacher/parent")
    @NotEmpty(message = "Role is required")
    @NotNull(message = "Role is required")
    private String role;
}
