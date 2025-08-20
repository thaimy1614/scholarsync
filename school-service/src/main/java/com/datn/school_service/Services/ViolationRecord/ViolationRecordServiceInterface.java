package com.datn.school_service.Services.ViolationRecord;

import com.datn.school_service.Dto.Request.ViolationRecord.AddViolationRecordRequest;
import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ViolationRecord.ViolationRecordResponse;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Models.ViolationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ViolationRecordServiceInterface {
    ViolationRecordResponse getViolationRecordById(Long id);

    void createViolationRecord(AddViolationRecordRequest addViolationRecordRequest);

    void updateViolationRecord(Long id, AddViolationRecordRequest addViolationRecordRequest );

    void deleteViolationRecord(Long id);

    void restoreViolationRecord(Long id);

    Page<ViolationRecordResponse> getAll(Pageable pageable, boolean active);

    List<ViolationRecordResponse> getViolationRecordByClassId(Long classId);
}
