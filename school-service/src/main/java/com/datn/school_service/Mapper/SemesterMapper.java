package com.datn.school_service.Mapper;


import com.datn.school_service.Dto.Request.Semester.AddSemesterRequest;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Models.Semester;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = SchoolYearMapper.class)
public interface SemesterMapper {
    @Mapping(source = "schoolYear", target = "schoolYearResponse")

    SemesterResponse toSemesterResponse(Semester semester);

    Semester toSemester(AddSemesterRequest addSemesterRequest);

    void updateSemester(@MappingTarget Semester clazz, AddSemesterRequest gradeRequest);
}
