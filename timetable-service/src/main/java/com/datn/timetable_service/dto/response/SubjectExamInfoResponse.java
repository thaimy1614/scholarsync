package com.datn.timetable_service.dto.response;

import com.datn.timetable_service.model.SubjectExamInfo.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectExamInfoResponse {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Integer duration;
    private ExamType type;
}
