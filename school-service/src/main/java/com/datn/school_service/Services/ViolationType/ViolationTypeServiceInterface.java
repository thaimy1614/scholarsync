package com.datn.school_service.Services.ViolationType;

import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Request.ViolationType.SearchViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Models.ViolationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ViolationTypeServiceInterface {
    Page<ViolationTypeResponse> getAll(Pageable pageable, boolean active);

    ViolationTypeResponse getViolationTypeById(Long id);

    void createIndividualViolations(AddViolationTypeRequest addViolationTypeRequest);

    void createCollectiveViolations(AddViolationTypeRequest addViolationTypeRequest);

    void createViolationType(AddViolationTypeRequest addViolationTypeRequest);

    void createViolationTypeForIndividual(AddViolationTypeRequest addViolationTypeRequest);
    void createViolationTypeForGroup(AddViolationTypeRequest addViolationTypeRequest);

    void updateViolationType(Long id, AddViolationTypeRequest addViolationTypeRequest );

    void deleteViolationType(Long id);

    void restoreViolationType(Long id);

    List<ViolationTypeResponse> searchViolationType(SearchViolationTypeRequest keyword, boolean active);

    List<ViolationType> filterValidViolationTypesOrThrow(List<Long> violationTypeIds, String violationCategoryStr);
}
