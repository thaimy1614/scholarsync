package com.datn.school_service.Services.studentReportService;

import com.datn.school_service.Dto.Request.StudentReport.AddStudentReportRequest;
import com.datn.school_service.Dto.Request.StudentReport.PointOneTeacherWasReportOneClassRequest;
import com.datn.school_service.Dto.Request.StudentReport.QuestionAnswerRequest;
import com.datn.school_service.Dto.Request.StudentReport.TotalPointTeacherWasReportedRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.StudentReport.*;
import com.datn.school_service.Dto.Respone.Subject.SubjectNameResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.datn.school_service.Mapper.EvaluationSessionMapper;
import com.datn.school_service.Mapper.StudentReportMapper;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.*;
import com.datn.school_service.Repository.*;
import com.datn.school_service.Services.SubjectService.SubjectService;
import com.datn.school_service.Services.TeacherClassification.TeacherClassificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.datn.school_service.Exceptions.ErrorCode.EVALUATION_SESSION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StudentReportService implements StudentReportServiceInterface {
    private final UserServiceClient userServiceClient;
    private final SemesterRepository semesterRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final EvaluationSessionRepository evaluationSessionRepository;
    private final StudentReportDetailRepository studentReportDetailRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final StudentReportMapper studentReportMapper;
    private final ClassRepository classRepository;
    private final SubjectService subjectService;
    private final TeacherClassificationRepository teacherClassificationRepository;
    private final TeacherClassificationService teacherClassificationService;
    private final EvaluationSessionMapper evaluationSessionMapper;

    @Override
    public AddStudentReportResponse addStudentReport(AddStudentReportRequest request) {
        double averagePoint = 0;
        double totalAveragePoint = 0;
        int check_session = 0;
        var studentId = request.getStudentId();
        var teacherId = request.getTeacherId();
        var semesterId = request.getSemesterId();
        var subjectId = request.getSubjectId();

        int scale_limit = 10; // thang report tính theo 4 max như gpa sv

       if(!subjectService.checkTeacherTeachesSubjectInClass(teacherId, request.getSubjectId(), request.getClassId()))
        {
            throw new AppException(ErrorCode.TEACHER_NOT_TEACH_IN_CLASS);
        }

        SubjectNameResponse subjectNameResponse = subjectService.getSubjectName(subjectId);
//        String hihi = "Montoan";  // gan tam do api subject dang loi

        Class clazz = classRepository.findClassByClassId(request.getClassId());
        if (clazz == null) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, request.getClassId());
        }
        var schoolYearId = clazz.getSchoolYearId();
        if (schoolYearId == null) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }
        List<QuestionAnswerRequest> listQuestionAnswer = request.getQuestionAnswer();
        List<InvalidQuestionAnswerResponse> invalidQuestionAnswerResponses = new ArrayList<>();
        List<StudentReportDetail> studentReportDetails = new ArrayList<>();
        List<QuestionAnswerResponse> questionAnswerResponses = new ArrayList<>();

        if (listQuestionAnswer == null || listQuestionAnswer.isEmpty()) {
            throw new AppException(ErrorCode.INPUT_NULL, "questionAnswer");
        }
        if (!classRepository.existsByClassIdAndStudentIdContaining(clazz.getClassId(), studentId)) {
            throw new AppException(ErrorCode.STUDENT_NOT_STUDY_IN_CLASS);
        }
        if (!semesterRepository.existsById(semesterId)) {
            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT);
        }
        if (!schoolYearRepository.existsById(schoolYearId)) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }
        ApiResponse<Map<String, Boolean>> resultTeacher = userServiceClient.checkUserRole(
                Collections.singletonList(teacherId), "TEACHER");
        ApiResponse<Map<String, Boolean>> resultStudent = userServiceClient.checkUserRole(
                Collections.singletonList(studentId), "STUDENT");

        if (resultTeacher == null || resultTeacher.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check teacher role failed");
        } else if (!resultTeacher.getResult().getOrDefault(teacherId, false)) {
            throw new AppException(ErrorCode.TEACHER_NOT_EXIT, teacherId);
        } else if (resultStudent == null || resultStudent.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check student role failed");
        } else if (!resultStudent.getResult().getOrDefault(studentId, false)) {
            throw new AppException(ErrorCode.STUDENT_NOT_EXIT, studentId);
        }

        var semester = semesterRepository.findById(semesterId).get();
        var schoolYear = schoolYearRepository.findById(schoolYearId).get();


        List<String> ids = List.of(studentId, teacherId);
        var rawResult = userServiceClient.getUsersByIds(ids).getResult();
        ObjectMapper mapper = new ObjectMapper();
        String studentName = "";
        String teacherName = "";
        for (Object obj : rawResult) {
            GetUserNameResponse user = mapper.convertValue(obj, GetUserNameResponse.class);
            if (user.getUserId().equals(studentId)) {
                studentName = user.getFullName();
            } else if (user.getUserId().equals(teacherId)) {
                teacherName = user.getFullName();
            }
        }
        EvaluationSession session = evaluationSessionRepository
                .findByStudentIdAndTeacherIdAndSemester_SemesterIdAndClazz_ClassIdAndSubjectId(
                        studentId, teacherId, semesterId, clazz.getClassId(), subjectId
                )
                .orElseGet(() -> evaluationSessionRepository.save(EvaluationSession.builder()
                        .subjectId(subjectId)
                        .studentId(studentId)
                        .teacherId(teacherId)
                        .semester(semester)
                        .clazz(clazz)
                        .build()
                ));

        for (QuestionAnswerRequest obj : listQuestionAnswer) {

            Long questionId = obj.getQuestionId();
            Long answerId = obj.getAnswerID();

            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isEmpty() || !questionOpt.get().isActive()) {
                invalidQuestionAnswerResponses.add(new InvalidQuestionAnswerResponse(questionId, answerId));
                continue;
            }

            Optional<Answer> answerOpt = answerRepository.findById(answerId);
            if (answerOpt.isEmpty() || !answerOpt.get().isActive()) {
                invalidQuestionAnswerResponses.add(new InvalidQuestionAnswerResponse(questionId, answerId));
                continue;
            }

            Question question = questionOpt.get();
            Answer answer = answerOpt.get();
            boolean exitquestionanswer = studentReportDetailRepository.existsByEvaluationSession_EvaluationSessionIdAndQuestion_QuestionId(session.getEvaluationSessionId(), questionId);
            boolean valid = question.getAnswers().contains(answer);
            if (!valid) {
                invalidQuestionAnswerResponses.add(new InvalidQuestionAnswerResponse(questionId, answerId));
                continue;
            }
            if (exitquestionanswer) {
                invalidQuestionAnswerResponses.add(new InvalidQuestionAnswerResponse(questionId, answerId));
                continue;
            }
            int maxPoint = answerRepository.findMaxAnswerPointByQuestionId(questionId);
            if (maxPoint == 0) {
                averagePoint = 0;
            } else {
                averagePoint = (double) (answer.getAnswerPoint() * scale_limit) / maxPoint;
            }
            StudentReportDetail detail = StudentReportDetail.builder()
                    .question(question)
                    .answer(answer)
                    .averagePoint(averagePoint)
                    .evaluationSession(session)
                    .build();
            studentReportDetails.add(detail);
            if (!studentReportDetails.isEmpty()) {

                studentReportDetailRepository.saveAll(studentReportDetails);
            }
            questionAnswerResponses.add(new QuestionAnswerResponse(detail.getStudentReportDetailId(), question.getQuestionId(), question.getQuestion(), answer.getAnswerId(), answer.getAnswer(), averagePoint));

        }

        session.setAverageReportPoint(studentReportDetailRepository.averageAnswerPointByEvaluationSession(session.getEvaluationSessionId()));
        evaluationSessionRepository.save(session);
        Optional<TeacherClassification> classificationOpt =
                teacherClassificationRepository.findByTeacherIdAndSubjectIdAndSemester_SemesterIdAndClazz_ClassId(
                        teacherId, subjectId, semesterId, clazz.getClassId()
                );

        double thisReportPoint = session.getAverageReportPoint();
        String classificationName = teacherClassificationService.checkteacherClassificationName(thisReportPoint);
        int a = questionAnswerResponses.size();
        TeacherClassification classification;
        if (classificationOpt.isPresent()) {
            classification = classificationOpt.get();
            int prevReports = classification.getNumberReport();
            double prevTotal = classification.getTeacherClassificationPoint() * prevReports;

            int newReports = prevReports + questionAnswerResponses.size();
            double newAvg = (prevTotal + thisReportPoint) / newReports;
            classificationName = teacherClassificationService.checkteacherClassificationName(newAvg);
            classification.setTeacherClassificationName(classificationName);
            classification.setNumberReport(newReports);
            classification.setTeacherClassificationPoint(newAvg);
        } else {
            classification = TeacherClassification.builder()

                    .teacherClassificationName(classificationName)
                    .teacherId(teacherId)
                    .subjectId(subjectId)
                    .clazz(clazz)
                    .semester(semester)
                    .numberReport(questionAnswerResponses.size())
                    .teacherClassificationPoint(thisReportPoint)
                    .build();
        }

        teacherClassificationRepository.save(classification);


        return AddStudentReportResponse.builder()
                .evaluationSessionId(session.getEvaluationSessionId())
                .subjectName(subjectNameResponse.getName())
                .averagePoint(session.getAverageReportPoint())
                .classId(request.getClassId())
                .className(clazz.getClassName())
                .schoolYearId(clazz.getSchoolYearId())
                .studentId(studentId)
                .semesterId(semesterId)
                .teacherId(teacherId)
                .questionAnswerResponses(questionAnswerResponses)
                .studentName(studentName)
                .teacherName(teacherName)
                .semesterName(semester.getSemesterName())
                .schoolYear(schoolYear.getSchoolYear())
                .invalidQuestionAnswer(invalidQuestionAnswerResponses)
                .build();
    }


    @Override
    public TotalPointTeacherWasReportedResponse getSumPointTeacherWasReportByStudentInClass(PointOneTeacherWasReportOneClassRequest pointOneTeacherWasReportOneClassRequest) {
        String teacherId = pointOneTeacherWasReportOneClassRequest.getTeacherId();
        Long semesterId = pointOneTeacherWasReportOneClassRequest.getSemesterId();
        Long classId = pointOneTeacherWasReportOneClassRequest.getClassId();
        List<String> studentId = evaluationSessionRepository.findStudentIdsByClassSemesterTeacher(classId, semesterId, teacherId);
        TotalPointTeacherWasReportedRequest totalPointTeacherWasReportedRequest = TotalPointTeacherWasReportedRequest.builder()
                .semesterId(semesterId)
                .teacherId(teacherId)
                .studentId(studentId)
                .classId(classId)
                .build();
        TotalPointTeacherWasReportedResponse totalPointTeacherWasReportedResponse = new TotalPointTeacherWasReportedResponse();
        totalPointTeacherWasReportedResponse = totalPointTeacherWasReported(totalPointTeacherWasReportedRequest);
        // return totalPointTeacherWasReported(totalPointTeacherWasReportedRequest);
        return totalPointTeacherWasReportedResponse;
    }


    @Override
    public TotalPointTeacherWasReportedResponse totalPointTeacherWasReported(TotalPointTeacherWasReportedRequest request)  // total theo list student ko cần check in class ko
    {

        var studentIds = request.getStudentId();
        var teacherId = request.getTeacherId();
        var semesterId = request.getSemesterId();
        Class clazz = classRepository.findClassByClassId(request.getClassId());
        if (clazz == null) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, request.getClassId());
        }
        var schoolYearId = clazz.getSchoolYearId();
        if (schoolYearId == null) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }

        ApiResponse<Map<String, Boolean>> resultTeacher = userServiceClient.checkUserRole(List.of(teacherId), "TEACHER");
        if (resultTeacher == null || !resultTeacher.getResult().getOrDefault(teacherId, false)) {
            throw new AppException(ErrorCode.TEACHER_NOT_EXIT, teacherId);
        }

        ApiResponse<Map<String, Boolean>> resultStudent = userServiceClient.checkUserRole(studentIds, "STUDENT");
        if (resultStudent == null || resultStudent.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Call check student role failed");
        }
        if (!semesterRepository.existsById(semesterId)) {
            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT);
        }

        if (!schoolYearRepository.existsById(schoolYearId)) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT);
        }

        List<String> validStudentIds = studentIds.stream()
                .filter(id -> resultStudent.getResult().getOrDefault(id, false))
                .toList();

        List<String> idsToFetchInfo = new ArrayList<>(validStudentIds);
        idsToFetchInfo.add(teacherId);

        var rawUserInfos = userServiceClient.getUsersByIds(idsToFetchInfo).getResult();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, GetUserNameResponse> userMap = new HashMap<>();
        for (Object obj : rawUserInfos) {
            GetUserNameResponse user = mapper.convertValue(obj, GetUserNameResponse.class);
            userMap.put(user.getUserId(), user);
        }

        int totalPointAll = 0;
        int checkValidEvaluation = 0;

        List<TotalPointOneStudentResponse> studentResponses = new ArrayList<>();
        Optional<EvaluationSession> sessionOpt;
        for (String studentId : validStudentIds) {
            sessionOpt = evaluationSessionRepository
                    .findByStudentIdAndTeacherIdAndSemester_SemesterIdAndClazz_ClassId(studentId, teacherId, semesterId, clazz.getClassId());

            if (sessionOpt.isPresent()) {
                checkValidEvaluation++;
                Long sessionId = sessionOpt.get().getEvaluationSessionId();
                Integer point = studentReportDetailRepository.sumAnswerPointsByEvaluationSession(sessionId);
                int total = point != null ? point : 0;
                totalPointAll += total;

                var studentInfo = userMap.get(studentId);
                studentResponses.add(
                        studentReportMapper.toTotalPointOneStudentResponse(studentInfo, total)

                );
            }
        }
        if (checkValidEvaluation == 0) {
            throw new AppException(EVALUATION_SESSION_NOT_FOUND);
        } else {
            checkValidEvaluation = 0; // đặt lại bằng ko
        }
        var teacherInfo = userMap.get(teacherId);
        return TotalPointTeacherWasReportedResponse.builder()
                .teacherId(teacherId)
                .schoolYearId(schoolYearId)
                .semesterId(semesterId)
                .totalPointTeacherWasReported(totalPointAll)
                .teacherName(teacherInfo.getFullName())
                .image(teacherInfo.getImage())
                .semester(semesterRepository.findById(semesterId).get().getSemesterName())
                .schoolYear(schoolYearRepository.findById(schoolYearId).get().getSchoolYear())
                .totalPointOneStudentResponses(studentResponses)
                .build();
    }


    @Override
    public AddStudentReportResponse updateStudentReport(Long Evaluasionid, AddStudentReportRequest addStudentReportRequest, Long studentDetailId) {
        return null;
    }

    @Override
    public AddStudentReportResponse getStudentReportById(Long evaluationSessionId) {
        EvaluationSession session = evaluationSessionRepository.findById(evaluationSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_SESSION_NOT_FOUND));

        List<StudentReportDetail> details = new ArrayList<>(session.getStudentReports());
        List<QuestionAnswerResponse> questionAnswerResponses = evaluationSessionMapper.toDtoList(details);

        AddStudentReportResponse response = evaluationSessionMapper.toDto(session);
        response.setQuestionAnswerResponses(questionAnswerResponses);
        response.setInvalidQuestionAnswer(null);

        // Lấy tên môn học
        String subjectName = "N/A";
        try {
            SubjectNameResponse subject = subjectService.getSubjectName(session.getSubjectId());
            subjectName = subject.getName();
        } catch (Exception ignored) {
        }
        response.setSubjectName(subjectName);

        String studentId = session.getStudentId();
        String teacherId = session.getTeacherId();
        List<String> ids = List.of(studentId, teacherId);
        var rawResult = userServiceClient.getUsersByIds(ids).getResult();
        ObjectMapper mapper = new ObjectMapper();
        String studentName = "";
        String teacherName = "";

        for (Object obj : rawResult) {
            GetUserNameResponse user = mapper.convertValue(obj, GetUserNameResponse.class);
            if (user.getUserId().equals(studentId)) {
                studentName = user.getFullName();
            } else if (user.getUserId().equals(teacherId)) {
                teacherName = user.getFullName();
            }
        }
        response.setStudentName(studentName);
        response.setTeacherName(teacherName);

        // School year
        if (session.getClazz() != null) {
            Long schoolYearId = session.getClazz().getSchoolYearId();
            response.setSchoolYearId(schoolYearId);
            String schoolYear = schoolYearRepository.findById(schoolYearId)
                    .map(SchoolYear::getSchoolYear)
                    .orElse(null);
            response.setSchoolYear(schoolYear);
        }

        return response;
    }


    @Override
    public List<AddStudentReportResponse> getStudentReportByClass(PointOneTeacherWasReportOneClassRequest req) {
        Long classId = req.getClassId();
        String teacherId = req.getTeacherId();
        Long semesterId = req.getSemesterId();

        Class clazz = classRepository.findClassByClassId(classId);
        if (clazz == null) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, classId);
        }

        if (!semesterRepository.existsById(semesterId)) {
            throw new AppException(ErrorCode.SEMESTER_NOT_EXIT, semesterId);
        }

        ApiResponse<Map<String, Boolean>> teacherRole = userServiceClient.checkUserRole(List.of(teacherId), "TEACHER");
        if (teacherRole == null || teacherRole.getResult() == null || !teacherRole.getResult().getOrDefault(teacherId, false)) {
            throw new AppException(ErrorCode.TEACHER_NOT_EXIT, teacherId);
        }

        List<EvaluationSession> sessions = evaluationSessionRepository
                .findAllByClazz_ClassIdAndTeacherIdAndSemester_SemesterId(classId, teacherId, semesterId);

        if (sessions.isEmpty()) {
            throw new AppException(ErrorCode.EVALUATION_SESSION_NOT_FOUND);
        }

        Set<String> allStudentIds = sessions.stream()
                .map(EvaluationSession::getStudentId)
                .collect(Collectors.toSet());
        List<String> allIds = new ArrayList<>(allStudentIds);
        allIds.add(teacherId);

        Map<String, GetUserNameResponse> userMap = new HashMap<>();
        try {
            var rawResult = userServiceClient.getUsersByIds(allIds).getResult();
            ObjectMapper mapper = new ObjectMapper();
            for (Object obj : rawResult) {
                GetUserNameResponse user = mapper.convertValue(obj, GetUserNameResponse.class);
                userMap.put(user.getUserId(), user);
            }
        } catch (Exception ignored) {
        }

        return sessions.stream().map(session -> {
            List<StudentReportDetail> details = new ArrayList<>(session.getStudentReports());
            List<QuestionAnswerResponse> qaResponses = evaluationSessionMapper.toDtoList(details);

            String subjectName = "N/A";
            try {
                SubjectNameResponse subject = subjectService.getSubjectName(session.getSubjectId());
                subjectName = subject.getName();
            } catch (Exception ignored) {
            }

            Long schoolYearId = session.getClazz() != null ? session.getClazz().getSchoolYearId() : null;
            String schoolYear = (schoolYearId != null)
                    ? schoolYearRepository.findById(schoolYearId).map(SchoolYear::getSchoolYear).orElse(null)
                    : null;

            return AddStudentReportResponse.builder()
                    .studentId(session.getStudentId())
                    .studentName(userMap.getOrDefault(session.getStudentId(), new GetUserNameResponse()).getFullName())
                    .teacherId(session.getTeacherId())
                    .teacherName(userMap.getOrDefault(session.getTeacherId(), new GetUserNameResponse()).getFullName())
                    .classId(classId)
                    .className(session.getClazz() != null ? session.getClazz().getClassName() : null)
                    .semesterId(semesterId)
                    .semesterName(session.getSemester().getSemesterName())
                    .schoolYearId(schoolYearId)
                    .schoolYear(schoolYear)
                    .subjectName(subjectName)
                    .averagePoint(session.getAverageReportPoint())
                    .questionAnswerResponses(qaResponses)
                    .invalidQuestionAnswer(null)
                    .build();
        }).toList();
    }

    @Override
    public AddStudentReportResponse updateStudentReport(Long id, AddStudentReportRequest addStudentReportRequest) {
        return null;
    }


}

