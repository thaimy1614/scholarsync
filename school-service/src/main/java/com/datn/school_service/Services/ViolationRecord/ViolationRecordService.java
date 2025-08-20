package com.datn.school_service.Services.ViolationRecord;

import com.datn.school_service.Dto.Request.ViolationRecord.AddViolationRecordRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.ViolationRecord.ViolationRecordResponse;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.datn.school_service.Mapper.ViolationRecordMapper;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.ViolationRecord;
import com.datn.school_service.Models.ViolationRecord;
import com.datn.school_service.Models.ViolationType;
import com.datn.school_service.Repository.ClassRepository;
import com.datn.school_service.Repository.ViolationRecordRepository;
import com.datn.school_service.Repository.ViolationRecordRepository;
import com.datn.school_service.Repository.ViolationTypeRepository;
import com.datn.school_service.Services.UserService.GetUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ViolationRecordService implements ViolationRecordServiceInterface {
    final ClassRepository classRepository;
    final GetUserService getUserService;
    final ViolationRecordRepository violationRecordRepository;
    private final ViolationTypeRepository violationTypeRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public ViolationRecordResponse getViolationRecordById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        ViolationRecordResponse violationRecordResponse = new ViolationRecordResponse();

        ViolationRecord violationRecord = violationRecordRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_RECORD_NOT_FOUND));
        ViolationType violationType = violationRecord.getViolationType();
        ViolationTypeResponse violationTypeResponse = new ViolationTypeResponse();
        violationTypeResponse.setViolationPoint(violationType.getViolationPoint());
        violationTypeResponse.setViolationTypeName(violationType.getViolationTypeName());
        violationRecordResponse.setViolationTypeResponse(violationTypeResponse);
        if (violationRecord.getClazz() != null) {
         violationRecordResponse.setClassName(violationRecord.getClazz().getClassName());
        }
//        if(violationRecord.getViolationStudentId() !=null){
//            violationRecordResponse.setViolationStudent(getUserService.getSingleUserInfo(violationRecord.getViolationStudentId(),"STUDENT"));
//        }
        violationRecordResponse.setRedFlag(getUserService.getSingleUserInfo(violationRecord.getRedFlagId(),"MONITOR"));
        violationRecordResponse.setAbsentCount(violationRecord.getAbsentCount());

        return violationRecordResponse;
        }

    @Override
    public void createViolationRecord(AddViolationRecordRequest addViolationRecordRequest) {
        ViolationRecord violationRecord = new ViolationRecord();
    //  if(!addViolationRecordRequest.getViolationStudentId().isEmpty() && addViolationRecordRequest.getViolationTypeId() != null)
//        {
//            ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(
//                    Collections.singletonList(addViolationRecordRequest.getViolationStudentId()), "STUDENT");
//
//            if (roleCheck == null || roleCheck.getResult() == null ||
//                    !roleCheck.getResult().getOrDefault(addViolationRecordRequest.getViolationStudentId(), false)) {
//                throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "STUDENT", addViolationRecordRequest.getViolationStudentId());
//            }
//            String a = addViolationRecordRequest.getViolationStudentId();
//            violationRecord.setViolationStudentId(addViolationRecordRequest.getViolationStudentId());
//        }
        ApiResponse<Map<String, Boolean>> roleCheck1 = userServiceClient.checkUserRole(
                Collections.singletonList(addViolationRecordRequest.getRedFlagId()), "MONITOR");

        if (roleCheck1 == null || roleCheck1.getResult() == null ||
                !roleCheck1.getResult().getOrDefault(addViolationRecordRequest.getRedFlagId(), false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "MONITOR", addViolationRecordRequest.getRedFlagId());
        }

        if(addViolationRecordRequest.getClassId() != null) {
            Class clazz = classRepository.findById(addViolationRecordRequest.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", addViolationRecordRequest.getClassId()));
        violationRecord.setClazz(clazz);
        }
        ViolationType violationType = violationTypeRepository.findById(addViolationRecordRequest.getViolationTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));

        violationRecord.setViolationType(violationType);
        violationRecord.setAbsentCount(addViolationRecordRequest.getAbsentCount());
        violationRecord.setRedFlagId(addViolationRecordRequest.getRedFlagId());
        violationRecordRepository.save(violationRecord);
    }

    @Override
    public void updateViolationRecord(Long id, AddViolationRecordRequest request) {
        ViolationRecord violationRecord = violationRecordRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_RECORD_NOT_FOUND));


//        if (request.getViolationStudentId() != null && !request.getViolationStudentId().isEmpty()) {
//            ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(
//                    Collections.singletonList(request.getViolationStudentId()), "STUDENT");
//
//            if (roleCheck == null || roleCheck.getResult() == null ||
//                    !roleCheck.getResult().getOrDefault(request.getViolationStudentId(), false)) {
//                throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "STUDENT", request.getViolationStudentId());
//            }
//
//            violationRecord.setViolationStudentId(request.getViolationStudentId());
//        }

        // Cập nhật người báo cáo
        if (request.getRedFlagId() != null) {
            ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(
                    Collections.singletonList(request.getRedFlagId()), "MONITOR");

            if (roleCheck == null || roleCheck.getResult() == null ||
                    !roleCheck.getResult().getOrDefault(request.getRedFlagId(), false)) {
                throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "MONITOR", request.getRedFlagId());
            }

            violationRecord.setRedFlagId(request.getRedFlagId());
        }


        if (request.getClassId() != null) {
            Class clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", request.getClassId()));
            violationRecord.setClazz(clazz);
        }

        if (request.getViolationTypeId() != null) {
            ViolationType violationType = violationTypeRepository.findById(request.getViolationTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));
            violationRecord.setViolationType(violationType);
        }

        violationRecord.setAbsentCount(request.getAbsentCount());

        violationRecordRepository.save(violationRecord);
    }


    @Override
    public void deleteViolationRecord(Long id) {
        ViolationRecord violationRecord = violationRecordRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_RECORD_NOT_FOUND));

        if (!violationRecord.isActive()) {
            throw new AppException(ErrorCode.VIOLATION_RECORD_IS_DELETED);
        }

        violationRecord.setActive(false);
        violationRecordRepository.save(violationRecord);
    }

    @Override
    public void restoreViolationRecord(Long id) {
        ViolationRecord violationRecord = violationRecordRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_RECORD_NOT_FOUND));

        if (violationRecord.isActive()) {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE,"Violation Record");
        }

        violationRecord.setActive(false);
        violationRecordRepository.save(violationRecord);
    }


    @Override
    public Page<ViolationRecordResponse> getAll(Pageable pageable, boolean active) {
        Page<ViolationRecord> records = violationRecordRepository.findAllByIsActive(active, pageable);

        Page<ViolationRecordResponse> responsePage = records.map(record -> {
            ViolationRecordResponse response = new ViolationRecordResponse();

            if (record.getViolationType() != null) {
                ViolationTypeResponse typeRes = new ViolationTypeResponse();
                typeRes.setViolationTypeName(record.getViolationType().getViolationTypeName());
                typeRes.setViolationPoint(record.getViolationType().getViolationPoint());
                response.setViolationTypeResponse(typeRes);
            }

            response.setAbsentCount(record.getAbsentCount());

            if (record.getClazz() != null) {
                response.setClassName(record.getClazz().getClassName());
            }

//            if (record.getViolationStudentId() != null) {
//                response.setViolationStudent(getUserService.getSingleUserInfo(record.getViolationStudentId(), "STUDENT"));
//            }

            if (record.getRedFlagId() != null) {
                response.setRedFlag(getUserService.getSingleUserInfo(record.getRedFlagId(), "MONITOR"));
            }

            return response;
        });

        return responsePage;
    }

    @Override
    public List<ViolationRecordResponse> getViolationRecordByClassId(Long classId) {
        List<ViolationRecord> records = violationRecordRepository.findAllByClazz_ClassIdAndIsActiveTrue(classId);

        return records.stream().map(record -> {
            ViolationRecordResponse response = new ViolationRecordResponse();

            // Gán loại vi phạm
            if (record.getViolationType() != null) {
                ViolationTypeResponse typeRes = new ViolationTypeResponse();
                typeRes.setViolationTypeName(record.getViolationType().getViolationTypeName());
                typeRes.setViolationPoint(record.getViolationType().getViolationPoint());
                response.setViolationTypeResponse(typeRes);
            }

            response.setAbsentCount(record.getAbsentCount());

            if (record.getClazz() != null) {
                response.setClassName(record.getClazz().getClassName());
            }

//            if (record.getViolationStudentId() != null) {
//                response.setViolationStudent(getUserService.getSingleUserInfo(record.getViolationStudentId(), "STUDENT"));
//            }

            if (record.getRedFlagId() != null) {
                response.setRedFlag(getUserService.getSingleUserInfo(record.getRedFlagId(), "MONITOR"));
            }

            return response;
        }).toList();
    }


}
