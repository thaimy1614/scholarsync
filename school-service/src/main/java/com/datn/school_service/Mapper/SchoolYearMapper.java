package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.SchoolYear.AddSchoolYearRequest;
import com.datn.school_service.Dto.Respone.SchoolYear.SchoolYearResponse;
import com.datn.school_service.Models.SchoolYear;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SchoolYearMapper {
    SchoolYearResponse toSchoolYearResponse(SchoolYear schoolyear);

    SchoolYear toSchoolYear(AddSchoolYearRequest addSchoolYearRequest);

    void updateSchoolYear(@MappingTarget SchoolYear schoolyear, AddSchoolYearRequest addSchoolYearRequest);
}
