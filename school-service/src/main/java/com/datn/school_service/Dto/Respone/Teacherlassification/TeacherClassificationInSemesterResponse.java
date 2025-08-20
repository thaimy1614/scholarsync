package com.datn.school_service.Dto.Respone.Teacherlassification;

import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherClassificationInSemesterResponse {

    private GetUserNameResponse teacherInfo;

    private SemesterResponse semesterResponse;

    private String teacherClassificationName;

    private double teacherClassificationPoint;
}
