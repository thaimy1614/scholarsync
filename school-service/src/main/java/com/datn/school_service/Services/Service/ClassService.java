package com.datn.school_service.Services.Service;

import com.datn.school_service.Dto.Request.Class.AddClassRequest;
import com.datn.school_service.Dto.Request.Class.SearchClassRequest;
import com.datn.school_service.Dto.Request.ClassRequest;
import com.datn.school_service.Dto.Request.HeadTeacherClassUpdate;
import com.datn.school_service.Dto.Respone.*;
import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.datn.school_service.Dto.Respone.User.GetStudentInfo;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Dto.Respone.User.UserIdResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.datn.school_service.Mapper.ClassMapper;
import com.datn.school_service.Mapper.GradeMapper;
import com.datn.school_service.Mapper.RoomMapper;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.Grade;
import com.datn.school_service.Models.Room;
import com.datn.school_service.Models.SchoolYear;
import com.datn.school_service.Repository.*;
import com.datn.school_service.Services.InterfaceService.ClassServiceInterface;
import com.datn.school_service.Services.UserService.GetUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService implements ClassServiceInterface {

    final ClassRepository classRepository;
    final ClassMapper classMapper;
    final SchoolYearRepository schoolYearRepository;
    final RoomRepository roomRepository;
    final GradeMapper gradeMapper;
    final GradeRepository gradeRepository;
    final RoomMapper roomMapper;
    private final UserServiceClient userServiceClient;
    private final SchoolRepository schoolRepository;
    private final GetUserService getUserService;

    private final ObjectMapper objectMapper;


    @Override
    public Page<ClassResponse> getAllClassesDelete(Pageable pageable) {
        Page<Class> classes = classRepository.findAllByClassActiveFalse(pageable);
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class");
        }
        return classes.map(clazz -> mapRespone(clazz.getClassId()));
    }

    @Override
    public Page<ClassResponse> getAllClassesActive(Pageable pageable) {
        Page<Class> classes = classRepository.findAllByClassActiveTrue(pageable);
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class");
        }
        return classes.map(clazz -> mapRespone(clazz.getClassId()));
    }

    @Override
    public ClassResponse updateClasses(Long ID, ClassRequest classRequest) {
        try {
            if (!classRepository.existsById(ID)) {
                throw new AppException(ErrorCode.ENTITYS_NOT_FOUND);
            }
            Class clazz = classMapper.toClass(classRequest);

            clazz.setClassId(ID);
            classRepository.save(clazz);
            return mapRespone(clazz.getClassId());
            // return classMapper.toClassRespone(classRepository.save(clazz));
        } catch (DataAccessException e) {
            throw new AppException(ErrorCode.FAILED_SAVE_ENTITY, "class");
        }
    }

    @Override
    public List<ClassResponse> getStudentClassesBySchoolYear(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "SchoolYearId is null");
        }
        List<Class> classes = classRepository.findAllBySchoolYearId(id);
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class");
        }
        return classes.stream()
                .map(clazz -> {
                    return mapRespone(clazz.getClassId());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateClass(Long id, ClassRequest classRequest) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "ClassId is null");
        }
        Class existingClass = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "class not found with id: " + id));
        if (classRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "ClassRequest is null");
        }
        if (!existingClass.getClassName().equals(classRequest.getClassName()) && classRepository.existsByClassNameAndSchoolYearId(classRequest.getClassName(), classRequest.getSchoolYearId())) {
            throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT, "Class name " + classRequest.getClassName(), "class");
        }
        else
        {
            if(classRepository.existsByClassNameAndSchoolYearId(classRequest.getClassName(), classRequest.getSchoolYearId()))
            {
                throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT, classRequest.getClassName(), "class with same schoolyear and semester");
            }
            ApiResponse<Map<String, Boolean>> result;
            try {
                result = userServiceClient.checkUserRole(Collections.singletonList(classRequest.getTeacherId()), "TEACHER");
            } catch (Exception ex) {
                throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: " + ex.getMessage());
            }

            if (result == null || result.getResult() == null) {
                throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: empty response");
            }

        }
        classMapper.updateClass(existingClass, classRequest);
        classRepository.save(existingClass);
    }

    @Override
    public ClassResponse mapRespone(Long id) {

        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", id));

        ClassResponse classResponse = classMapper.toClassRespone(clazz);
        //   RoomResponse roomResponse= roomMapper.toRoomResponse(clazz.getRoom());
        if(clazz.getClassMonitorId() != null)
        {
            List<String> monitorId = new ArrayList<>();
            monitorId.add(clazz.getClassMonitorId());
            List<GetStudentInfo> classMonitor = getUserService.getStudentInfo(monitorId,"STUDENT", clazz.getClassName());
            classResponse.setClassMonitor(classMonitor.get(0));
        }
        if(clazz.getHomeroomTeacherId() != null) {

            GetUserNameResponse teacher = getUserService.getSingleUserInfo(clazz.getHomeroomTeacherId(), "TEACHER");

            classResponse.setTeacher(teacher);
        }
        List<String> studentId = clazz.getStudentId();
        if(studentId != null && !studentId.isEmpty()) {

            List<GetStudentInfo> listStudent = getUserService.getStudentInfo(studentId, "STUDENT",clazz.getClassName());

            classResponse.setListStudent(listStudent);
        }
        if (classResponse.getSchoolYearId() != null) {
            schoolYearRepository.findById(classResponse.getSchoolYearId())
                    .ifPresent(schoolYear -> classResponse.setSchoolYear(schoolYear.getSchoolYear()));
        }

        //   classResponse.setGradeResponse(gradeMapper.toGradeRespone(clazz.getGrade()));
        //   classResponse.setRoomResponse(roomResponse);

        return classResponse;
    }

    @Override
    public Object getHomeTeacher(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", classId));

        String teacherId = clazz.getHomeroomTeacherId();
        if (teacherId == null || teacherId.isBlank()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "homeroomTeacherId");
        }

        return teacherId;
    }

    @Override
    public List<ClassResponse> findAllClassesByClassName(SearchClassRequest searchClassRequest, boolean active) {
        List<Class> classes = new ArrayList<>();
        String className = searchClassRequest.getClassName();
        if(className == null || className.isBlank())
        {
            throw new AppException(ErrorCode.INPUT_NULL, "className");
        }
        if(active) {

            classes = classRepository.findAllByClassActiveTrueAndClassNameContainingIgnoreCase(className);
        }
        else
        {
            classes = classRepository.findAllByClassActiveFalseAndClassNameContainingIgnoreCase(className);

        }
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class");
        }

        return classes.stream()
                .map(clazz -> {
                    return mapRespone(clazz.getClassId());
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<GetStudentInfo> getStudentByClassId(Long classId) {

        ClassResponse classResponse = getClassById(classId);
        if(classResponse == null || classResponse.getListStudent().isEmpty())
        {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class or student not found");
        }
        return classResponse.getListStudent();
    }

    @Override
    public ClassResponse getClassByStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "StudentId is null or empty");
        }
        LocalDate today = LocalDate.now();
        SchoolYear schoolYear = schoolYearRepository.findByDateBetween(today)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_EMPTY, "No active SchoolYear found for today"));

        List<Class> classes = classRepository.findByStudentIdAndSchoolYearId(studentId, schoolYear.getSchoolYearId());
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class for student with ID: " + studentId);
        }
        return classes.stream()
                .map(clazz -> mapRespone(clazz.getClassId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_EMPTY, "Class for student with ID: " + studentId));
    }

    @Override
    public List<BasicClassResponse> getClassByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Class IDs are null or empty");
        }
        List<Class> classes = classRepository.findAllById(ids);
        if (classes.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Classes not found for the provided IDs");
        }
        return classes.stream()
                .map(clazz -> BasicClassResponse.builder()
                        .classId(clazz.getClassId())
                        .className(clazz.getClassName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ClassResponse getClassByHomeroomAndSchoolYear(String teacherId, Long schoolYearId) {
        Class clazz = classRepository.findByHomeroomTeacherIdAndSchoolYearId(teacherId, schoolYearId);

        if (clazz == null) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Class for homeroom teacher with ID: " + teacherId + " in school year ID: " + schoolYearId);
        }

        return ClassResponse.builder()
                .classId(clazz.getClassId())
                .className(clazz.getClassName())
                .build();
    }


    @Override
    public ClassResponse setHeadTeacher(Long id, HeadTeacherClassUpdate headTeacherClassUpdate) {
        if (headTeacherClassUpdate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "headTeacherId is null");
        }
        List<String> headTeacherIdList = List.of(headTeacherClassUpdate.getHeadTeacherId());
        ApiResponse<Map<String, Boolean>> result = userServiceClient.checkUserRole(headTeacherIdList, "TEACHER");
        if (result == null || !result.getResult().getOrDefault(headTeacherClassUpdate.getHeadTeacherId(), false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Teacher ID " + headTeacherClassUpdate.getHeadTeacherId() + " not found in user_service");
        }
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "class", id));
        boolean exists = classRepository.existsByHomeroomTeacherIdAndSchoolYearIdAndNotClassId(
                headTeacherClassUpdate.getHeadTeacherId(),
                clazz.getSchoolYearId(),
                clazz.getClassId()
        );
        if (exists) {

            throw new AppException(ErrorCode.DUPLICATE_ENTITY, "Teacher", "in this school year.");
        }
        clazz.setHomeroomTeacherId(headTeacherClassUpdate.getHeadTeacherId());
        classRepository.save(clazz);
        return mapRespone(clazz.getClassId());
    }


    @Override
    public ClassResponse setMonitor(Long classId, String studentId) {
        if (studentId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "StudentId is null");
        }
        List<String> studentIdList = List.of(studentId);
        ApiResponse<Map<String, Boolean>> result = userServiceClient.checkUserRole(studentIdList, "STUDENT");
        if (result == null || !result.getResult().getOrDefault(studentId, false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Student ID " + studentId + " not found in user_service");
        }
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Class", classId));
        if (classRepository.existsByClassIdAndStudentIdContaining(classId, studentId)) {
            clazz.setClassMonitorId(studentId);
        } else {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "student in class", classId);
        }
        return mapRespone(clazz.getClassId());
    }

    @Override
    public AddStudentResponse addStudentToClass(Long classId, List<String> studentIds) {
        List<String> inValidStudentId = new ArrayList<>();


        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Class", classId));
        Long schoolYearId = clazz.getSchoolYearId();
        if (clazz.getStudentId() == null) {
            clazz.setStudentId(new ArrayList<>());
        }

        ApiResponse<Map<String, Boolean>> result;
        try {
            result = userServiceClient.checkUserRole(studentIds, "STUDENT");
        } catch (Exception ex) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: " + ex.getMessage());
        }

        if (result == null || result.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: empty response");
        }

        for (String studentId : studentIds) {

            if (!result.getResult().getOrDefault(studentId, false)) {
                inValidStudentId.add(studentId);
            } else {
                if (!classRepository.existsByClassIdAndStudentIdContaining(classId, studentId)) {
                    if(!classRepository.existsClassByStudentIdAndSchoolYearId(studentId, schoolYearId))
                    {
                        clazz.getStudentId().add(studentId);
                    }
                    else {
                        inValidStudentId.add(studentId);
                    }

                } else {
                    inValidStudentId.add(studentId);
                }
            }
        }

        classRepository.save(clazz);
        return AddStudentResponse.builder()
                .invalidStudentIds(inValidStudentId)
                .className(clazz.getClassName())
                .build();
    }

    @Override
    public ClassResponse getClassById(Long id) {
        ClassResponse classResponse = mapRespone(id);
        return classResponse;
    }

    @Override
    public Object getClassIdByClassNameAndSchoolYear(String className, String schoolYear) {
        Long schoolYearId = schoolYearRepository.findSchoolYearIdBySchoolYear(schoolYear);
        Long classId;
        if(schoolYearId == null)
        {
            throw new AppException(ErrorCode.ENTITY_EMPTY,"school year id ");
        }
        else {
            Class clazz  = (classRepository.findClassIdByClassNameContainingIgnoreCaseAndSchoolYearId(className, schoolYearId));
            if( clazz == null)
            {
                throw new AppException(ErrorCode.ENTITY_EMPTY,"class id");
            }
            else
            {
                classId = clazz.getClassId();
            }
        }
        return classId;
    }

    @Override
    public AddClassResponse addClass(AddClassRequest classRequest) {
        classRequest.setMainSession(classRequest.getMainSession().toUpperCase());
//
        if(classRepository.existsByClassNameAndSchoolYearId(classRequest.getClassName(), classRequest.getSchoolYearId()))
        {
            throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT, classRequest.getClassName(), "class with same schoolyear and semester");
        }
        try {
      {
                Room room = roomRepository.findById(classRequest.getRoomId()).orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "room", classRequest.getRoomId()));
                Grade grade = gradeRepository.findById(classRequest.getGradeId()).orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "grade", classRequest.getGradeId()));
                Class clazz = classMapper.toClass(classRequest);
                clazz.setRoom(room);
                clazz.setGrade(grade);
                Long schoolYearId = clazz.getSchoolYearId();
                clazz.setClassActive(true);
                clazz = classRepository.save(clazz);

                SchoolYear schoolYear = schoolYearRepository.findById(clazz.getSchoolYearId()).orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "schoolyear", schoolYearId));
                String schoolYearName = schoolYear.getSchoolYear();
                GradeResponse gradeResponse = gradeMapper.toGradeResponse(grade);
                RoomResponse roomResponse = roomMapper.toRoomResponse(room);
                AddClassResponse addClassResponse = classMapper.toAddClassResponse(clazz);
                addClassResponse.setSchoolYear(schoolYearName);
                addClassResponse.setRoomResponse(roomResponse);
                addClassResponse.setGradeResponse(gradeResponse);
                return addClassResponse;
            }

        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.FAILED_SAVE_ENTITY, "class");
        }

    }

//    @Override
//    public List<AddStudentResponse> addStudentToClassByExcel(MultipartFile file) {
//        Map<Long, List<String>> classToStudentIds = new HashMap<>();
//        List<AddStudentResponse> resultList = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
//            XSSFSheet sheet = workbook.getSheetAt(0);
//
//            String schoolYear = sheet.getRow(0).getCell(1).getStringCellValue().trim();
//
//            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
//                XSSFRow row = sheet.getRow(i);
//                if (row == null) continue;
//
//                try {
//                    String className = row.getCell(0).getStringCellValue().trim();
//                    String studentEmail = row.getCell(1).getStringCellValue().trim();
//
//                    if (className.isBlank() || studentEmail.isBlank()) continue;
//
//                    Long classId = (Long) getClassIdByClassNameAndSchoolYear(className, schoolYear);
//
//                        UserIdResponse response = userServiceClient.getUserIdByEmail(studentEmail).getResult();
//
//
//                    if(response == null)
//                    {
//                        throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "USER ID");
//                    }
//                    String studentId = response.getUserId();
//                    if(studentId == null)
//                    {
//                        throw new AppException(ErrorCode.FAILED_SAVE_ENTITY, "student");
//                    }
//
//
//                    if (studentId != null) {
//                        classToStudentIds.computeIfAbsent(classId, k -> new ArrayList<>()).add(studentId);
//                    }
//
//                } catch (Exception e) {
//                    log.error("Lỗi khi xử lý dòng {}: {}", i + 1, e.getMessage());
//                }
//            }
//
//            for (Map.Entry<Long, List<String>> entry : classToStudentIds.entrySet()) {
//                Long classId = entry.getKey();
//                List<String> studentIds = entry.getValue();
//
//                try {
//                    AddStudentResponse response = addStudentToClass(classId, studentIds);
//                    resultList.add(response);
//                } catch (Exception e) {
//                    log.error("Lỗi khi add students vào classId {}: {}", classId, e.getMessage());
//                }
//            }
//
//        } catch (IOException e) {
//            throw new AppException(ErrorCode.FILE_ERROR, "Không thể đọc file Excel");
//        }
//
//        return resultList;
//    }

    @Override
    public AddStudentResponse addStudentToClassByExcel(MultipartFile file, Long classId) {
        List<String> emailList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 5; i <= sheet.getLastRowNum(); i++) { // bắt đầu từ dòng 6 (index 5)
                XSSFRow row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String email = row.getCell(1).getStringCellValue().trim(); // cột B (index 1)
                    if (!email.isBlank()) {
                        emailList.add(email);
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi đọc email ở dòng {}: {}", i + 1, e.getMessage());
                }
            }

            if (emailList.isEmpty()) {
                throw new AppException(ErrorCode.FILE_ERROR, "Danh sách email rỗng");
            }

            List<Object> rawUserIdResponses = userServiceClient.getUserIdsByEmails(emailList).getResult();
            if (rawUserIdResponses == null || rawUserIdResponses.isEmpty()) {
                throw new AppException(ErrorCode.CALL_SERVICE_FALL);
            }

            // Convert từ Object → UserIdResponse
            ObjectMapper mapper = new ObjectMapper();
            List<String> studentIds = rawUserIdResponses.stream()
                    .map(obj -> mapper.convertValue(obj, UserIdResponse.class))
                    .map(UserIdResponse::getUserId)
                    .filter(Objects::nonNull)
                    .toList();

            if (studentIds.isEmpty()) {
                throw new AppException(ErrorCode.ENTITY_EMPTY, "student");
            }

            return addStudentToClass(classId, studentIds);

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_ERROR, "Không thể đọc file Excel");
        }
    }

    @Override
    public void promotedClass(Long gradeId, String schoolYear) {
        if(schoolYear.trim() == "" || schoolYear.trim() == null)
        {}
        Long schoolYearId = schoolYearRepository.findSchoolYearIdBySchoolYear(schoolYear.trim());
        if(schoolYearId == null)
        {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "school year");
        }
        List<Class> classes = classRepository.findAllBySchoolYearIdAndGrade_GradeId(schoolYearId,gradeId);

    }

}
