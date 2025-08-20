package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.RecordCollectiveViolations.AddRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.AddRecordCollectiveViolationsResponse;
import com.datn.school_service.Models.RecordCollectiveViolations;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {RecordPersonalViolationsMapper.class, ViolationRecordMapper.class})
public interface RecordCollectiveViolationsMapper {
    @Mapping(source = "violationTypes", target = "violationTypes")
    @Mapping(source = "personalViolations", target = "addRecordPersonalViolations")
    @Mapping(source = "clazz.classId", target = "classId")
    @Mapping(source = "clazz.className", target = "className")
    AddRecordCollectiveViolationsResponse toAddRecordCollectiveViolationsResponse(RecordCollectiveViolations recordCollectiveViolations);


}
