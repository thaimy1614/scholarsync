package com.datn.timetable_service.dto.UserService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherResponse {
    private String userId;
    private String fullName;
    private String image;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private Instant createdAt;
    private int enrollmentYear;
}
