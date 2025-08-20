package com.datn.school_service.Dto.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassRequest {

    private String className;

    private String teacherId;

    private Long schoolYearId;

    private Long roomId;

    private Long gradeId;

    private String mainSession;

    private String classMonitorId;

    private boolean classActive;

    private List<String> studentId;

}
