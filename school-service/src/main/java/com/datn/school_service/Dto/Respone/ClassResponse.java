package com.datn.school_service.Dto.Respone;

import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.datn.school_service.Dto.Respone.User.GetStudentInfo;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Models.Grade;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassResponse {
    private Long classId;
    private String className;
    private GetUserNameResponse teacher;
    private String schoolYear;
    private Long schoolYearId;
    private String mainSession;
    private RoomResponse roomResponse;
    private GetStudentInfo classMonitor;
    List<GetStudentInfo> listStudent;
    private int numberStudent;
    private boolean classActive;
    private GradeResponse gradeResponse;

}

