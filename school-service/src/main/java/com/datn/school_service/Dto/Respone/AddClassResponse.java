package com.datn.school_service.Dto.Respone;

import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddClassResponse {
    private Long classId;

    private String className;

    private String teacherId;

   private String teacherName;

   private String mainSession;

    private Long schoolYearId;

    private String schoolYear;

    private RoomResponse roomResponse;

    private GradeResponse gradeResponse;

}
