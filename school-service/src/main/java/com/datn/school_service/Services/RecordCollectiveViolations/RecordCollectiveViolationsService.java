package com.datn.school_service.Services.RecordCollectiveViolations;

import com.datn.school_service.Dto.Request.RecordCollectiveViolations.AddRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.StartEndDayRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.UpdateRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Request.RecordPersonalViolations.AddRecordPersonalViolationsRequest;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.AddRecordCollectiveViolationsResponse;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.WeeklyViolationPointResponse;
import com.datn.school_service.Dto.Respone.RecordPersonalViolations.AddRecordPersonalViolationsResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.RecordCollectiveViolationsMapper;
import com.datn.school_service.Mapper.RecordPersonalViolationsMapper;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.ViolationType;
import com.datn.school_service.Models.RecordCollectiveViolations;
import com.datn.school_service.Models.RecordPersonalViolations;
import com.datn.school_service.Repository.*;
import com.datn.school_service.Services.UserService.GetUserService;
import com.datn.school_service.Services.ViolationType.ViolationTypeServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordCollectiveViolationsService implements RecordCollectiveViolationsInterface {
    private final RecordCollectiveViolationsRepository recordCollectiveViolationsRepository;
    private final RecordCollectiveViolationsMapper recordCollectiveViolationsMapper;
    private final RecordPersonalViolationsRepository recordPersonalViolationsRepository;
    private final RecordPersonalViolationsMapper recordPersonalViolationsMapper;
    private final ClassRepository classRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final ViolationTypeRepository violationTypeRepository;
    private final GetUserService getUserService;
    private final ViolationTypeServiceInterface violationTypeService;


    @Override
    public AddRecordCollectiveViolationsResponse addRecordCollectiveViolations(AddRecordCollectiveViolationsRequest request) {
        Long classId = request.getClassId();
        String redFlagId = request.getRedFlagId();
        Integer absentCount = request.getAbsentCount();

        if (classId == null || redFlagId == null || absentCount == null || absentCount < 0) {
            throw new AppException(ErrorCode.INPUT_INVALID, "AddRecordCollectiveViolationsRequest");
        }
        if (recordCollectiveViolationsRepository.existsByClassIdAndCreatedToday(classId)) {
            throw new AppException(ErrorCode.RECORD_VIOLATIONS_ALREADY_EXISTS, " Collective today");
        }

        GetUserNameResponse redFlagInfo = getUserService.getSingleUserInfo(redFlagId, "MONITOR");
        if (redFlagInfo == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Red flag not found");
        }

        List<ViolationType> validGroupViolations = violationTypeService
                .filterValidViolationTypesOrThrow(request.getViolationGroupId(), "GROUP");

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", classId));

        RecordCollectiveViolations record = RecordCollectiveViolations.builder()
                .absentCount(absentCount)
                .redFlagId(redFlagId)
                .clazz(clazz)
                .violationTypes(validGroupViolations)
                .build();

        record = recordCollectiveViolationsRepository.save(record);

        List<AddRecordPersonalViolationsResponse> personalResponses = new ArrayList<>();

        if (request.getListStudentViolation() != null && !request.getListStudentViolation().isEmpty()) {
            for (AddRecordPersonalViolationsRequest studentReq : request.getListStudentViolation()) {
                String studentId = studentReq.getStudentId();
                if (!classRepository.existsByClassIdAndStudentIdContaining(clazz.getClassId(), studentId)) {
                    continue;
                }
                if (studentId == null) continue;

                GetUserNameResponse studentInfo = getUserService.getSingleUserInfo(studentId, "STUDENT");
                if (studentInfo == null) continue;

                List<Long> individualViolationIds = studentReq.getViolationTypeId();
                List<ViolationType> validIndividualViolations;
                try {
                    validIndividualViolations = violationTypeService
                            .filterValidViolationTypesOrThrow(individualViolationIds, "INDIVIDUAL");
                } catch (AppException e) {
                    continue;
                }

                RecordPersonalViolations rp = RecordPersonalViolations.builder()
                        .violationStudentId(studentId)
                        .violationTypes(validIndividualViolations)
                        .recordCollectiveViolations(record)
                        .build();

                recordPersonalViolationsRepository.save(rp);

                AddRecordPersonalViolationsResponse responseItem = recordPersonalViolationsMapper
                        .toAddRecordPersonalViolationsResponse(rp);
                responseItem.setStudentInfo(studentInfo);
                personalResponses.add(responseItem);

            }

        }
        AddRecordCollectiveViolationsResponse response = recordCollectiveViolationsMapper
                .toAddRecordCollectiveViolationsResponse(record);
        response.setRedFlagInfo(redFlagInfo);
        response.setViolationPoint(sumViolationInOneRecordCollectiveViolations(record.getRecordCollectiveViolationsId()));
        response.setAddRecordPersonalViolations(personalResponses);
        return response;
    }

    @Override
    public AddRecordCollectiveViolationsResponse getByRecordCollectiveViolationsId(Long id) {
        RecordCollectiveViolations record = recordCollectiveViolationsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "recordCollectiveViolations", id));

        GetUserNameResponse redFlagInfo = getUserService.getSingleUserInfo(
                record.getRedFlagId(), "MONITOR"
        );
        GetUserNameResponse principal = null;
        if (record.getPrincipalId() != null) {
            principal = getUserService.getSingleUserInfo(
                    record.getPrincipalId(), "PRINCIPAL"
            );
        }

        Class clazz = record.getClazz();

        List<AddRecordPersonalViolationsResponse> personalResponses = new ArrayList<>();
        if (record.getPersonalViolations() != null && !record.getPersonalViolations().isEmpty()) {
            for (var personalViolation : record.getPersonalViolations()) {
                AddRecordPersonalViolationsResponse responseItem =
                        recordPersonalViolationsMapper.toAddRecordPersonalViolationsResponse(personalViolation);

                GetUserNameResponse studentInfo = getUserService.getSingleUserInfo(
                        personalViolation.getViolationStudentId(), "STUDENT"
                );
                responseItem.setStudentInfo(studentInfo);

                personalResponses.add(responseItem);
            }
        }

        AddRecordCollectiveViolationsResponse response =
                recordCollectiveViolationsMapper.toAddRecordCollectiveViolationsResponse(record);
        response.setRedFlagInfo(redFlagInfo);
        response.setPrincipalInfo(principal);
        response.setViolationPoint(sumViolationInOneRecordCollectiveViolations(record.getRecordCollectiveViolationsId()));
//        response.setClassName(clazz != null ? clazz.getClassName() : null);
//        response.setClassId(clazz != null ? clazz.getClassId() : null);
        response.setAddRecordPersonalViolations(personalResponses);

        return response;
    }

    @Override
    public List<AddRecordCollectiveViolationsResponse> getAllByRecordCollectiveViolationsByClassId(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INPUT_NULL, "recordCollectiveViolations", id);
        }
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", id));


        List<RecordCollectiveViolations> records =
                recordCollectiveViolationsRepository.findAllByClazz_ClassId(id);

        List<AddRecordCollectiveViolationsResponse> responses = new ArrayList<>();

        for (RecordCollectiveViolations record : records) {
            AddRecordCollectiveViolationsResponse response =
                    this.getByRecordCollectiveViolationsId(record.getRecordCollectiveViolationsId());
            responses.add(response);
            response.setViolationPoint(sumViolationInOneRecordCollectiveViolations(record.getRecordCollectiveViolationsId()));
        }

        return responses;
    }

    @Override

    public void deleteByRecordCollectiveViolationsId(Long id) {
        RecordCollectiveViolations record = recordCollectiveViolationsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "recordCollectiveViolations", id));

        if (!record.isActive()) {
            throw new AppException(ErrorCode.RECORD_VIOLATIONS_IS_DELETED, "recordCollectiveViolations with id: "+ id);
        }
        record.setActive(false);

        if (record.getPersonalViolations() != null) {
            for (RecordPersonalViolations personalViolation : record.getPersonalViolations()) {
                personalViolation.setActive(false);
            }
        }
        recordCollectiveViolationsRepository.save(record);
    }


    @Override
    public void updateByRecordCollectiveViolations(Long id, UpdateRecordCollectiveViolationsRequest request) {
        RecordCollectiveViolations record = recordCollectiveViolationsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_VIOLATIONS_NOT_FOUND, id.toString()));

        if (recordCollectiveViolationsRepository.isOverdue(id)) {
            throw new AppException(ErrorCode.RECORD_VIOLATIONS_OVERDUE);
        }

        Long classId = request.getClassId();
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", classId));

        String newRedFlagId = request.getRedFlagId();
        String principalId = request.getPrincipalId();

        if (newRedFlagId == null) {
            if (principalId == null) {
                throw new AppException(ErrorCode.INPUT_NULL, "Missing both redFlagId and principalId");
            }
            GetUserNameResponse principalInfo = getUserService.getSingleUserInfo(principalId, "PRINCIPAL");
            if (principalInfo == null) {
                throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Principal not found");
            }
            record.setPrincipalId(principalId);
        } else if (!newRedFlagId.equals(record.getRedFlagId())) {
            GetUserNameResponse principalInfo = getUserService.getSingleUserInfo(newRedFlagId, "MONITOR");
            if (principalInfo == null) {
                throw new AppException(ErrorCode.CALL_SERVICE_FALL, "MONITOR not found");
            }
            record.setRedFlagId(newRedFlagId);
        }

        record.setAbsentCount(request.getAbsentCount());

        if (request.getViolationGroupId() != null && !request.getViolationGroupId().isEmpty()) {
            List<ViolationType> validGroupViolations = violationTypeService
                    .filterValidViolationTypesOrThrow(request.getViolationGroupId(), "GROUP");
            record.setViolationTypes(validGroupViolations);
        }


        Map<String, RecordPersonalViolations> oldMap = new HashMap<>();
        for (RecordPersonalViolations rp : record.getPersonalViolations()) {
            try {
                if (rp == null || rp.getRecordPersonalViolationsId() == null) continue;
                if (!recordPersonalViolationsRepository.existsById(rp.getRecordPersonalViolationsId())) continue;
                oldMap.put(rp.getViolationStudentId(), rp);
            } catch (EntityNotFoundException e) {
                continue;
            }
        }

        List<AddRecordPersonalViolationsResponse> personalResponses = new ArrayList<>();

        for (AddRecordPersonalViolationsRequest studentReq : request.getListStudentViolation()) {
            String studentId = studentReq.getStudentId();
            if (studentId == null || !classRepository.existsByClassIdAndStudentIdContaining(classId, studentId)) {
                continue;
            }

            GetUserNameResponse studentInfo = getUserService.getSingleUserInfo(studentId, "STUDENT");
            if (studentInfo == null) continue;

            List<ViolationType> validIndividualViolations = violationTypeService
                    .filterValidViolationTypesOrThrow(studentReq.getViolationTypeId(), "INDIVIDUAL");

            RecordPersonalViolations rp;
            if (oldMap.containsKey(studentId)) {
                rp = oldMap.get(studentId);
                rp.setViolationTypes(validIndividualViolations);
            } else {
                rp = RecordPersonalViolations.builder()
                        .violationStudentId(studentId)
                        .violationTypes(validIndividualViolations)
                        .recordCollectiveViolations(record)
                        .build();
            }

            recordPersonalViolationsRepository.save(rp);
            AddRecordPersonalViolationsResponse responseItem = recordPersonalViolationsMapper
                    .toAddRecordPersonalViolationsResponse(rp);
            responseItem.setStudentInfo(studentInfo);
            personalResponses.add(responseItem);
            oldMap.remove(studentId);
        }

        for (RecordPersonalViolations rp : oldMap.values()) {
            recordPersonalViolationsRepository.delete(rp);
        }

        record.setClazz(clazz);
        recordCollectiveViolationsRepository.save(record);
    }


    @Override
    public Page<AddRecordCollectiveViolationsResponse> getAll(Pageable pageable, boolean active) {
        Page<RecordCollectiveViolations> recordPage;

        if (active) {
            recordPage = recordCollectiveViolationsRepository.findAllByIsActiveTrue(pageable);
        } else {
            recordPage = recordCollectiveViolationsRepository.findAllByIsActiveFalse(pageable);
        }

        return recordPage.map(record -> {
            AddRecordCollectiveViolationsResponse response =
                    recordCollectiveViolationsMapper.toAddRecordCollectiveViolationsResponse(record);

            response.setViolationPoint(sumViolationInOneRecordCollectiveViolations(record.getRecordCollectiveViolationsId()));
            response.setRedFlagInfo(getUserService.getSingleUserInfo(record.getRedFlagId(), "MONITOR"));
            if(record.getPrincipalId() != null) {
                response.setPrincipalInfo(getUserService.getSingleUserInfo(record.getPrincipalId(), "PRINCIPAL"));
            }
            if (record.getPersonalViolations() != null) {
                List<AddRecordPersonalViolationsResponse> personalList = record.getPersonalViolations().stream().map(rp -> {
                    AddRecordPersonalViolationsResponse res = recordPersonalViolationsMapper.toAddRecordPersonalViolationsResponse(rp);
                    res.setStudentInfo(getUserService.getSingleUserInfo(rp.getViolationStudentId(), "STUDENT"));

                    return res;
                }).toList();

                response.setAddRecordPersonalViolations(personalList);
            }

            return response;
        });
    }

    @Override
    public AddRecordCollectiveViolationsResponse getByRecordCollectiveViolationsForTeacher(Long id, Date dateParam) {

        Class clazz = classRepository.findClassByClassId(id);
        if (clazz == null) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Class");
        }
        List<RecordCollectiveViolations> recordCollectiveViolations = recordCollectiveViolationsRepository.findAllByClazz_ClassId(clazz.getClassId());

        LocalDate date = dateParam.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();


        List<RecordCollectiveViolations> all = recordCollectiveViolationsRepository
                .findAllByClazz_ClassId(clazz.getClassId());


        Optional<RecordCollectiveViolations> opt = all.stream()
                .filter(r -> r.isActive()
                        && r.getCreatedAt().toLocalDate().isEqual(date))
                .findFirst();

        RecordCollectiveViolations record = opt.orElseThrow(() ->
                new AppException(ErrorCode.ENTITYS_NOT_FOUND, "No record for that date"));

        return getByRecordCollectiveViolationsId(record.getRecordCollectiveViolationsId());

    }

    @Override
    public double sumViolationInOneRecordCollectiveViolations(Long id) {
        double personRecord = 0;
        double collectiveRecord = 0;
        if (!recordCollectiveViolationsRepository.existsById(id)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND);
        }
        personRecord = recordPersonalViolationsRepository.sumPersonalPointsByCollectiveId(id);
        collectiveRecord = recordCollectiveViolationsRepository.sumViolationPointsByRecordId(id);

        return personRecord + collectiveRecord;
    }




    @Override
    public List<WeeklyViolationPointResponse> getWeeklyViolationOfClassSimple(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND));

        List<Object[]> rawResults = recordCollectiveViolationsRepository
                .getViolationPointsByWeekForClass(classId);

        return rawResults.stream()
                .map(row -> {
                    int week = ((Number) row[0]).intValue();
                    long totalViolationPoint = ((Number) row[1]).longValue();
                    return new WeeklyViolationPointResponse(week, totalViolationPoint, clazz.getClassName());
                })
                .toList();
    }
    public List<WeeklyViolationPointResponse> getAllClassWeeklyViolationsBySchoolYear(Long schoolYearId) {
        List<Object[]> resultRaw = recordCollectiveViolationsRepository
                .getAllClassWeeklyViolationsBySchoolYear(schoolYearId);
      int a =  resultRaw.size();

        return resultRaw.stream()
                .map(row -> new WeeklyViolationPointResponse(
                        ((Number) row[0]).intValue(),             // week
                        ((Number) row[2]).longValue(),             // totalViolationPoint
                        (String) row[1]                            // className
                ))
                .toList();
    }



    @Override
    public Page<WeeklyViolationPointResponse> getAllClassWeeklyViolationsByWWeek(StartEndDayRequest request, Pageable pageable, String sort, String direction) {
        LocalDate start = request.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate end = request.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<Object[]> raw = recordCollectiveViolationsRepository.getWeeklyViolationPointsByDateRange(start, end);


        List<WeeklyViolationPointResponse> all = raw.stream()
                .map(row -> new WeeklyViolationPointResponse(
                        ((Number) row[0]).intValue(),     // week
                        ((Number) row[2]).longValue(),    // totalViolationPoint
                        (String) row[1]                   // className
                ))
                .collect(Collectors.toList());

        Comparator<WeeklyViolationPointResponse> comparator = switch (sort) {
            case "classPoint" -> Comparator.comparing(WeeklyViolationPointResponse::getClassPoint);
            case "totalViolationPoint" -> Comparator.comparing(WeeklyViolationPointResponse::getTotalViolationPoint);
            case "className" -> Comparator.comparing(WeeklyViolationPointResponse::getClassName);
            default -> Comparator.comparing(WeeklyViolationPointResponse::getWeek);
        };
        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        all.sort(comparator);

        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min(startIdx + pageable.getPageSize(), all.size());
        List<WeeklyViolationPointResponse> pageContent = all.subList(startIdx, endIdx);

        return new PageImpl<>(pageContent, pageable, all.size());
    }

    @Override
    public Page<AddRecordCollectiveViolationsResponse> getAllViolationCollectiveInADay(Date date, Pageable pageable) {
        if (date == null) {
            return Page.empty();
        }

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<RecordCollectiveViolations> records =
                recordCollectiveViolationsRepository.findAll().stream()
                        .filter(r -> r.isActive()
                                && r.getCreatedAt() != null
                                && r.getCreatedAt().toLocalDate().isEqual(localDate))
                        .toList();

        List<AddRecordCollectiveViolationsResponse> responseList = records.stream()
                .map(record -> {
                    AddRecordCollectiveViolationsResponse response =
                            recordCollectiveViolationsMapper.toAddRecordCollectiveViolationsResponse(record);
                    response.setViolationPoint(sumViolationInOneRecordCollectiveViolations(record.getRecordCollectiveViolationsId()));
                    response.setRedFlagInfo(getUserService.getSingleUserInfo(record.getRedFlagId(), "MONITOR"));
                    if(record.getPrincipalId() != null) {
                        response.setPrincipalInfo(getUserService.getSingleUserInfo(record.getPrincipalId(), "PRINCIPAL"));
                    }
                    if (record.getPersonalViolations() != null) {
                        List<AddRecordPersonalViolationsResponse> personalList = record.getPersonalViolations().stream()
                                .map(rp -> {
                                    AddRecordPersonalViolationsResponse res =
                                            recordPersonalViolationsMapper.toAddRecordPersonalViolationsResponse(rp);
                                    res.setStudentInfo(getUserService.getSingleUserInfo(rp.getViolationStudentId(), "STUDENT"));
                                    return res;
                                })
                                .toList();
                        response.setAddRecordPersonalViolations(personalList);
                    }

                    return response;
                })
                .toList();

        // Phân trang thủ công
        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min(startIdx + pageable.getPageSize(), responseList.size());
        List<AddRecordCollectiveViolationsResponse> pageContent = responseList.subList(startIdx, endIdx);

        return new PageImpl<>(pageContent, pageable, responseList.size());
    }



}

