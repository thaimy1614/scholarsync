package com.datn.school_service.Services.TeacherClassification;

import com.datn.school_service.Dto.Request.Teacherlassification.AddTeacherlassificationRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.DescriptionRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.SearchTeacherlassification;
import com.datn.school_service.Dto.Request.Teacherlassification.TeacherClassificationInSemesterRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.SemesterResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.ClassResponseTeacherlassification;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherClassificationInSemesterResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherlassificationResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.datn.school_service.Mapper.SemesterMapper;
import com.datn.school_service.Mapper.StudentReportMapper;
import com.datn.school_service.Models.TeacherClassification;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Repository.*;
import com.datn.school_service.Services.InterfaceService.ClassServiceInterface;
import com.datn.school_service.Services.SubjectService.SubjectService;
import com.datn.school_service.Services.UserService.GetUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherClassificationService implements TeacherClassificationInterface {
    private final UserServiceClient userServiceClient;
    private final GetUserService getUserService;
    private final SemesterRepository semesterRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final TeacherClassificationRepository teacherClassificationRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final StudentReportMapper studentReportMapper;
    private final ClassRepository classRepository;
    private final SubjectService subjectService;
    private final SemesterMapper semesterMapper;

    private final ClassServiceInterface classService;
//    @Override
//    public TeacherlassificationResponse createTeacherlassification(AddTeacherlassificationRequest request) {
//        var teacherId = request.getTeacherId();
//        var semesterId = request.getSemesterId();
//        var classId = request.getClassId();
//
//        Class clazz = classRepository.findClassByClassId(classId);
//        if (clazz == null) {
//            throw new AppException(ErrorCode.CLASS_NOT_FOUND, classId);
//        }
//
//        var schoolYearId = clazz.getSchoolYearId();
//        if (schoolYearId == null || !schoolYearRepository.existsById(schoolYearId)) {
//            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
//        }
//
//        if (!semesterRepository.existsById(semesterId)) {
//            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT);
//        }
//
//        ApiResponse<Map<String, Boolean>> resultTeacher = userServiceClient.checkUserRole(
//                Collections.singletonList(teacherId), "TEACHER");
//
//        if (resultTeacher == null || resultTeacher.getResult() == null) {
//            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check teacher role failed");
//        }
//        if (!resultTeacher.getResult().getOrDefault(teacherId, false)) {
//            throw new AppException(ErrorCode.TEACHER_NOT_EXIT, teacherId);
//        }
//
//        if (teacherClassificationRepository.findByTeacherIdAndSemester_SemesterIdAndClazz_ClassId(
//                teacherId, semesterId, classId).isPresent()) {
//            throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT, "Teacher classification", teacherId);
//        }
//
//        var rawResult = userServiceClient.getUsersByIds(List.of(teacherId)).getResult();
//        ObjectMapper mapper = new ObjectMapper();
//        GetUserNameResponse userInfo = mapper.convertValue(rawResult.get(0), GetUserNameResponse.class);
//
//        int point = request.getTeacherlassificationPoint();
//        String classificationName = checkteacherClassificationName(point);
//
//        TeacherClassification entity = TeacherClassification.builder()
//                .teacherId(teacherId)
//                .teacherClassificationPoint(point)
//                .teacherClassificationName(classificationName)
//                .semester(semesterRepository.findById(semesterId).get())
//                .clazz(clazz)
//                .isActive(true)
//                .build();
//        teacherClassificationRepository.save(entity);
//
//        return TeacherlassificationResponse.builder()
//                .teacherClassificationId(entity.getTeacherlassificationId())
//                .teacherId(teacherId)
//                .teacherName(userInfo.getFullName())
//                .teacherClassificationPoint(point)
//                .teacherClassificationName(classificationName)
//                .image(userInfo.getImage())
//                .semesterResponse(SemesterResponse.builder()
//                        .semesterId(semesterId)
//                        .semesterName(entity.getSemester().getSemesterName())
//                        .build())
//                .classResponse(ClassResponseTeacherlassification.builder()
//                        .classId(classId)
//                        .className(clazz.getClassName())
//                        .build())
//                .build();
//    }


    @Override
    public TeacherlassificationResponse updateTeacherlassification(Long id, AddTeacherlassificationRequest request) {
        TeacherClassification entity = teacherClassificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS));

        if (!entity.isActive()) {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_IS_DELETED);
        }

        var teacherId = request.getTeacherId();
        var semesterId = request.getSemesterId();
        var classId = request.getClassId();
        var subjectId = request.getSubjectId();

        Class clazz = classRepository.findClassByClassId(classId);
        if (clazz == null) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, classId);
        }

        if(!subjectService.checkTeacherTeachesSubjectInClass(teacherId, request.getSubjectId(), request.getClassId()))
        {
            throw new AppException(ErrorCode.TEACHER_NOT_TEACH_IN_CLASS);
        }
        var schoolYearId = clazz.getSchoolYearId();
        if (schoolYearId == null || !schoolYearRepository.existsById(schoolYearId)) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }

        if (!semesterRepository.existsById(semesterId)) {
            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT);
        }

        ApiResponse<Map<String, Boolean>> resultTeacher = userServiceClient.checkUserRole(
                Collections.singletonList(teacherId), "TEACHER");

        if (resultTeacher == null || resultTeacher.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check teacher role failed");
        }
        if (!resultTeacher.getResult().getOrDefault(teacherId, false)) {
            throw new AppException(ErrorCode.TEACHER_NOT_EXIT, teacherId);
        }

        teacherClassificationRepository.findByTeacherIdAndSubjectIdAndSemester_SemesterIdAndClazz_ClassId(
                teacherId,subjectId, semesterId, classId).ifPresent(existing -> {
            if (!existing.getTeacherClassificationId().equals(id)) {
                throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT, "Teacher classification", teacherId);
            }
        });

        var rawResult = userServiceClient.getUsersByIds(List.of(teacherId)).getResult();
        ObjectMapper mapper = new ObjectMapper();
        GetUserNameResponse userInfo = mapper.convertValue(rawResult.get(0), GetUserNameResponse.class);

        int point = request.getTeacherClassificationPoint();
        String classificationName = checkteacherClassificationName((double) point);

        entity.setTeacherId(teacherId);
        entity.setClazz(clazz);
        entity.setSemester(semesterRepository.findById(semesterId).get());
        entity.setTeacherClassificationPoint(point);
        entity.setTeacherClassificationName(classificationName);

        teacherClassificationRepository.save(entity);

        return TeacherlassificationResponse.builder()
                .teacherClassificationId(entity.getTeacherClassificationId())
                .teacherId(teacherId)
                .teacherName(userInfo.getFullName())
                .teacherClassificationPoint(point)
                .teacherClassificationName(classificationName)
                .image(userInfo.getImage())
                .semesterResponse(SemesterResponse.builder()
                        .semesterId(semesterId)
                        .semesterName(entity.getSemester().getSemesterName())
                        .build())
                .classResponse(ClassResponseTeacherlassification.builder()
                        .classId(classId)
                        .className(clazz.getClassName())
                        .build())
                .build();
    }



    @Override
    public boolean deleteTeacherlassification(Long id) {
        TeacherClassification entity = teacherClassificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS));

        if (!entity.isActive()) {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_IS_DELETED);
        }

        entity.setActive(false);
        teacherClassificationRepository.save(entity);
        return true;
    }

    @Override
    public List<TeacherlassificationResponse> searchTeacherlassification(SearchTeacherlassification search) {
        List<TeacherClassification> entities = teacherClassificationRepository
                .findAllByTeacherClassificationName(search.getClassificationName().toLowerCase())
                .stream().filter(TeacherClassification::isActive)
                .collect(Collectors.toList());

        List<String> teacherIds = entities.stream().map(TeacherClassification::getTeacherId).distinct().toList();
        var rawUserData = userServiceClient.getUsersByIds(teacherIds).getResult();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, GetUserNameResponse> userMap = rawUserData.stream()
                .map(obj -> mapper.convertValue(obj, GetUserNameResponse.class))
                .collect(Collectors.toMap(GetUserNameResponse::getUserId, u -> u));

        return entities.stream().map(entity -> {
            GetUserNameResponse user = userMap.get(entity.getTeacherId());
            return TeacherlassificationResponse.builder()
                    .teacherClassificationId(entity.getTeacherClassificationId())
                    .teacherId(user.getUserId())
                    .teacherName(user.getFullName())
                    .image(user.getImage())
                    .teacherClassificationName(entity.getTeacherClassificationName())
                    .teacherClassificationPoint(entity.getTeacherClassificationPoint())
                    .semesterResponse(SemesterResponse.builder()
                            .semesterId(entity.getSemester().getSemesterId())
                            .semesterName(entity.getSemester().getSemesterName())
                            .build())
                    .classResponse(ClassResponseTeacherlassification.builder()
                            .classId(entity.getClazz().getClassId())
                            .className(entity.getClazz().getClassName())
                            .build())
                    .build();
        }).toList();
    }


    @Override
    public TeacherlassificationResponse getTeacherlassificationById(Long id) {
        TeacherClassification entity = teacherClassificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS));

        if (!entity.isActive()) {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_IS_DELETED);
        }

        var teacherId = entity.getTeacherId();
        var userResult = userServiceClient.getUsersByIds(List.of(teacherId)).getResult();
        ObjectMapper mapper = new ObjectMapper();
        GetUserNameResponse user = mapper.convertValue(userResult.get(0), GetUserNameResponse.class);

        return TeacherlassificationResponse.builder()
                .teacherClassificationId(entity.getTeacherClassificationId())
                .teacherId(user.getUserId())
                .teacherName(user.getFullName())
                .image(user.getImage())
                .teacherClassificationName(entity.getTeacherClassificationName())
                .teacherClassificationPoint(entity.getTeacherClassificationPoint())
                .semesterResponse(SemesterResponse.builder()
                        .semesterId(entity.getSemester().getSemesterId())
                        .semesterName(entity.getSemester().getSemesterName())
                        .build())
                .classResponse(ClassResponseTeacherlassification.builder()
                        .classId(entity.getClazz().getClassId())
                        .className(entity.getClazz().getClassName())
                        .build())
                .build();
    }

    @Override
    public Page<TeacherlassificationResponse> getAll(Pageable pageable, boolean active) {
        Page<TeacherClassification> page = active
                ? teacherClassificationRepository.findAllByIsActiveTrue(pageable)
                : teacherClassificationRepository.findAllByIsActiveFalse(pageable);

        List<String> teacherIds = page.stream().map(TeacherClassification::getTeacherId).distinct().toList();
        var rawUserData = userServiceClient.getUsersByIds(teacherIds).getResult();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, GetUserNameResponse> userMap = rawUserData.stream()
                .map(obj -> mapper.convertValue(obj, GetUserNameResponse.class))
                .collect(Collectors.toMap(GetUserNameResponse::getUserId, u -> u));

        return page.map(entity -> {
            GetUserNameResponse user = userMap.get(entity.getTeacherId());
            return TeacherlassificationResponse.builder()
                    .teacherClassificationId(entity.getTeacherClassificationId())
                    .teacherId(user.getUserId())
                    .teacherName(user.getFullName())
                    .image(user.getImage())
                    .teacherClassificationName(entity.getTeacherClassificationName())
                    .teacherClassificationPoint(entity.getTeacherClassificationPoint())
                    .semesterResponse(SemesterResponse.builder()
                            .semesterId(entity.getSemester().getSemesterId())
                            .semesterName(entity.getSemester().getSemesterName())
                            .build())
                    .classResponse(ClassResponseTeacherlassification.builder()
                            .classId(entity.getClazz().getClassId())
                            .className(entity.getClazz().getClassName())
                            .build())
                    .build();
        });
    }


    @Override
    public String checkteacherClassificationName(double point) {
        if (point < 3.5) return "weak";
        else if (point < 5) return "normal";
        else if (point < 6.5) return "average";
        else if (point < 8) return "good";
        else return "excellent";
    }

    private final double[] promotionLevels = {3.5, 5.0, 6.5, 8.0};

    @Override
    public void promoteTeacherClassification(Long id, DescriptionRequest request) {
        TeacherClassification entity = teacherClassificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS));

        if (!entity.isActive()) {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_IS_DELETED);
        }

        double currentPoint = entity.getTeacherClassificationPoint();
        double newPoint = -1;

        for (double level : promotionLevels) {
            if (currentPoint < level) {
                newPoint = level;
                break;
            }
        }

        if (newPoint == -1) {
            throw new AppException(ErrorCode.INPUT_INVALID, "Teacher already has highest classification");
        }

        entity.setTeacherClassificationPoint(newPoint);
        entity.setTeacherClassificationName(checkteacherClassificationName(newPoint));
        entity.setDescription((request.getDescription() != null && !request.getDescription().isEmpty())
                ? request.getDescription() : "This teacher was promoted");

        teacherClassificationRepository.save(entity);
    }

    private final double[] demotionLevels = {8.0, 6.5, 5.0, 3.5};

    @Override
    public void demoteTeacherClassification(Long id, DescriptionRequest request) {
        TeacherClassification entity = teacherClassificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS));

        if (!entity.isActive()) {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_IS_DELETED);
        }

        double currentPoint = entity.getTeacherClassificationPoint();
        double newPoint = -1;

        for (double level : demotionLevels) {
            if (currentPoint > level) {
                newPoint = level;
                break;
            }
        }

        if (newPoint == -1) {
            throw new AppException(ErrorCode.INPUT_INVALID, "Teacher already at lowest classification level");
        }

        entity.setTeacherClassificationPoint(newPoint);
        entity.setTeacherClassificationName(checkteacherClassificationName(newPoint));
        entity.setDescription((request.getDescription() != null && !request.getDescription().isEmpty())
                ? request.getDescription() : "This teacher was demoted");

        teacherClassificationRepository.save(entity);
    }


    @Override
    public TeacherClassificationInSemesterResponse getTeacherClassificationInSemester(TeacherClassificationInSemesterRequest teacherClassificationInSemesterRequest) {
        TeacherClassificationInSemesterResponse teacherClassificationInSemesterResponse = new TeacherClassificationInSemesterResponse();
        Long semesterId = teacherClassificationInSemesterRequest.getSemesterId();
        String teacherId = teacherClassificationInSemesterRequest.getTeacherId();
        if (!semesterRepository.existsById(semesterId)) {
            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT);
        }

        ApiResponse<Map<String, Boolean>> resultTeacher = userServiceClient.checkUserRole(
                Collections.singletonList(teacherId), "TEACHER");
        if (resultTeacher == null || resultTeacher.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check teacher role failed");
        }
        boolean check = teacherClassificationRepository.existsByTeacherIdAndSemester_SemesterId(teacherId, semesterId);
        if (check)
        {
            double point = teacherClassificationRepository.getEvargaByTeacherAndSemester(teacherId, semesterId);
            String classificationName = checkteacherClassificationName(point);

            var semester = semesterRepository.findById(semesterId).get();
            GetUserNameResponse getUserNameResponse = new GetUserNameResponse();
            getUserNameResponse = getUserService.getSingleUserInfo(teacherId,"TEACHER");

            SemesterResponse semesterResponse = new SemesterResponse();
            semesterResponse.setSemesterName(semester.getSemesterName());
            semesterResponse.setSemesterId(semester.getSemesterId());
            teacherClassificationInSemesterResponse.setSemesterResponse(semesterResponse);
            teacherClassificationInSemesterResponse.setTeacherClassificationPoint(point);
            teacherClassificationInSemesterResponse.setTeacherClassificationName(classificationName);
            teacherClassificationInSemesterResponse.setTeacherInfo(getUserNameResponse);
            return teacherClassificationInSemesterResponse;
        }
        else
        {
            throw new AppException(ErrorCode.TEACHER_LASSIFICATION_NOT_EXITS);

        }

    }

}
