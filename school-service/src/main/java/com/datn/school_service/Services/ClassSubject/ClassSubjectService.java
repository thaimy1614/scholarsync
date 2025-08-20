package com.datn.school_service.Services.ClassSubject;

import com.datn.school_service.Dto.Request.ClassSubject.AddClassSubjectRequest;
import com.datn.school_service.Dto.Request.ClassSubject.SearchClassSubjectRequest;
import com.datn.school_service.Dto.Respone.ClassSubjectResponse.ClassInClassSubjectResponse;
import com.datn.school_service.Dto.Respone.ClassSubjectResponse.ClassSubjectResponse;
import com.datn.school_service.Dto.Respone.ClassSubjectResponse.SubjectResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;

import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.ClassSubject;
import com.datn.school_service.Repository.ClassRepository;
import com.datn.school_service.Repository.ClassSubjectRepository;
import com.datn.school_service.Repository.RoomRepository;
import com.datn.school_service.Repository.SchoolYearRepository;
import com.datn.school_service.Services.UserService.GetUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassSubjectService implements ClassSubjectInterface{
    private final ClassSubjectRepository classSubjectRepository;
    private final ClassRepository classRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final RoomRepository roomRepository;
    private final GetUserService getUserService;


    public ClassSubjectResponse addClassSubject(AddClassSubjectRequest request) {
        Long classId = request.getClassId();
        Long subjectId = request.getSubjectId();

        if (classSubjectRepository.existsClassSubjectByClassIdAndSubjectId(classId, subjectId)) {
            throw new AppException(ErrorCode.CLASS_AND_SUBJECT_ALREADY_EXITS);
        }

        var clazzOpt = classRepository.findById(classId);
        if (clazzOpt.isEmpty()) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }

        var clazz = clazzOpt.get();
        if (!clazz.isClassActive()) {
            throw new AppException(ErrorCode.CLASS_IS_DELETED);
        }

        if (!schoolYearRepository.existsById(clazz.getSchoolYearId())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }
        var schoolYear = schoolYearRepository.findById(clazz.getSchoolYearId());

        GetUserNameResponse teacher = getUserService.getSingleUserInfo(clazz.getHomeroomTeacherId(), "TEACHER");

        ClassSubject entity = ClassSubject.builder()
                .classId(classId)
                .subjectId(subjectId)
                .build();
        classSubjectRepository.save(entity);

        return ClassSubjectResponse.builder()
                .subjectResponse(SubjectResponse.builder().subject("Subject #" + subjectId).build())
                .classInClassSubjectResponse(
                        ClassInClassSubjectResponse.builder()
                                .ClassName(clazz.getClassName())
                                .schoolYear(schoolYear.get().getSchoolYear())
                                .teacher(teacher)
                                .build()
                ).build();
    }

    @Override
    public void deleteClassSubject(Long id) {
        ClassSubject entity = classSubjectRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CLASS_SUBJECT_NOT_FOUND));
        if(!entity.isActive())
        {
            throw new AppException(ErrorCode.CLASS_AND_SUBJECT_ALREADY_DELETE);
        }
        entity.setActive(false);
        classSubjectRepository.save(entity);

    }

    @Override
    public void restoreClassSubject(Long id) {
        ClassSubject entity = classSubjectRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CLASS_SUBJECT_NOT_FOUND));
        if(entity.isActive())
        {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE, entity.getClasssubjectId());
        }
        entity.setActive(true);
        classSubjectRepository.save(entity);
    }

    @Override
    public ClassSubjectResponse updateClassSubject(Long id,AddClassSubjectRequest request) {
        Long classId = request.getClassId();
        Long subjectId = request.getSubjectId();
        ClassSubject entity = classSubjectRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CLASS_SUBJECT_NOT_FOUND));
        if(!entity.getClassId().equals(classId) && !entity.getSubjectId().equals(subjectId)) {
            if (classSubjectRepository.existsClassSubjectByClassIdAndSubjectId(classId, subjectId)) {
                throw new AppException(ErrorCode.CLASS_AND_SUBJECT_ALREADY_EXITS);
            }
        }

        var clazzOpt = classRepository.findById(classId);
        if (clazzOpt.isEmpty()) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }

        var clazz = clazzOpt.get();
        if (!clazz.isClassActive()) {
            throw new AppException(ErrorCode.CLASS_IS_DELETED);
        }

        if (!schoolYearRepository.existsById(clazz.getSchoolYearId())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }
        var schoolYear = schoolYearRepository.findById(clazz.getSchoolYearId());

        GetUserNameResponse teacher = getUserService.getSingleUserInfo(clazz.getHomeroomTeacherId(), "TEACHER");

        entity.setClassId(classId);
        entity.setSubjectId(subjectId);
        classSubjectRepository.save(entity);

        return ClassSubjectResponse.builder()
                .subjectResponse(SubjectResponse.builder().subject("Subject #" + subjectId).build())
                .classInClassSubjectResponse(
                        ClassInClassSubjectResponse.builder()
                                .ClassName(clazz.getClassName())
                                .schoolYear(schoolYear.get().getSchoolYear())
                                .teacher(teacher)
                                .build()
                ).build();
    }

    @Override
    public ClassSubjectResponse getClassSubjectById(Long id) {
        ClassSubject entity = classSubjectRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CLASS_SUBJECT_NOT_FOUND));
        var clazzOpt = classRepository.findById(entity.getClassId());
        if (clazzOpt.isEmpty()) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }

        var clazz = clazzOpt.get();
        if (!clazz.isClassActive()) {
            throw new AppException(ErrorCode.CLASS_IS_DELETED);
        }

        if (!schoolYearRepository.existsById(clazz.getSchoolYearId())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }
        var schoolYear = schoolYearRepository.findById(clazz.getSchoolYearId());

        GetUserNameResponse teacher = getUserService.getSingleUserInfo(clazz.getHomeroomTeacherId(), "TEACHER");

        return ClassSubjectResponse.builder()
                .subjectResponse(SubjectResponse.builder().subject("Subject #" + entity.getSubjectId()).build())
                .classInClassSubjectResponse(
                        ClassInClassSubjectResponse.builder()
                                .ClassName(clazz.getClassName())
                                .schoolYear(schoolYear.get().getSchoolYear())
                                .teacher(teacher)
                                .build()
                ).build();
    }

    @Override
    public Page<ClassSubjectResponse> getAllClassSubjects(Pageable pageable,boolean Active) {
        return null;
    }

    @Override
    public List<ClassSubjectResponse> searchClassSubjectsByClassName(SearchClassSubjectRequest searchClassSubjectRequest) {
        return List.of();
    }
}
