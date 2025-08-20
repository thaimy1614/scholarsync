package com.datn.school_service.Dto.Request.Teacherlassification;

import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.Semester;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTeacherlassificationRequest {

private String teacherId;

   private Long subjectId;

    private int teacherClassificationPoint;

    private Long semesterId;

    private Long classId;

}
