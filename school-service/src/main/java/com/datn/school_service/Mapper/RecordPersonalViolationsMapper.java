package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Respone.RecordPersonalViolations.AddRecordPersonalViolationsResponse;
import com.datn.school_service.Models.RecordPersonalViolations;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {ViolationTypeMapper.class})
public interface RecordPersonalViolationsMapper {
    @Mapping(source = "violationTypes", target = "listViolationType")
    AddRecordPersonalViolationsResponse toAddRecordPersonalViolationsResponse(RecordPersonalViolations recordPersonalViolations);



}
