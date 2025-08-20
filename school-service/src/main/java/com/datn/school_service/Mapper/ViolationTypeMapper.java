package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Models.ViolationType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ViolationTypeMapper {
    ViolationType toViolationType(AddViolationTypeRequest addViolationTypeRequest);

    ViolationTypeResponse toViolationTypeResponse(ViolationType answer);


}
