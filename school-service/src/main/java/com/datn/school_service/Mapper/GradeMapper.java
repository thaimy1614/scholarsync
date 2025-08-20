package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.Grade.AddGradeRequest;
import com.datn.school_service.Dto.Request.GradeRequest;
import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.datn.school_service.Models.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GradeMapper {
    GradeResponse toGradeResponse(Grade grade);

    Grade toGrade(AddGradeRequest gradeRequest);

    void updateGrade(@MappingTarget Grade grade, AddGradeRequest addGradeRequest);


}
