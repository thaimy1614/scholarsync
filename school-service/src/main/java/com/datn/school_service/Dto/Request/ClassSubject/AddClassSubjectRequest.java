package com.datn.school_service.Dto.Request.ClassSubject;

import jakarta.persistence.Column;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import java.util.Set;
import java.util.HashSet;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AddClassSubjectRequest {
    private Long subjectId;

    private Long classId;
}
