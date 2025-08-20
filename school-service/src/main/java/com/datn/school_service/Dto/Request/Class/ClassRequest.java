package com.datn.school_service.Dto.Request.Class;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ClassRequest {


    private String className;

    private String teacherId;

    private Long schoolYearId;

    private String classMonitorId;

    private boolean classActive;

    private List<String> studentId;
}
