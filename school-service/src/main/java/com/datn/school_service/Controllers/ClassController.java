package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.Answer.AddAnswerRequest;
import com.datn.school_service.Dto.Request.Class.AddClassRequest;
import com.datn.school_service.Dto.Request.Class.SearchClassRequest;
import com.datn.school_service.Dto.Request.ClassRequest;
import com.datn.school_service.Dto.Request.HeadTeacherClassUpdate;
import com.datn.school_service.Dto.Respone.*;
import com.datn.school_service.Dto.Respone.User.GetStudentInfo;
import com.datn.school_service.Dto.Respone.User.UserIdResponse;
import com.datn.school_service.Services.InterfaceService.ClassServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.datn.school_service.Utils.ExcelTemplateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("${application.api.prefix}/class")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ClassController {

    final ClassServiceInterface classService;

    @GetMapping("/getAllClassesDelete")
    public ApiResponse<Page<ClassResponse>> getAllClassesDelete(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                                @RequestParam(value = "sort", defaultValue = "className") String sort,
                                                                @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<ClassResponse>>builder()
                .result(classService.getAllClassesDelete(pageable))
                .build();
    }

    @GetMapping("/getAllClassesActive")
    public ApiResponse<Page<ClassResponse>> getAllClassesActive(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                                @RequestParam(value = "sort", defaultValue = "className") String sort,
                                                                @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<ClassResponse>>builder()
                .result(classService.getAllClassesActive(pageable))
                .build();
    }

    @PostMapping("/add-class")
    public ApiResponse<AddClassResponse> addClass(@RequestBody AddClassRequest addClassRequest) {
        return ApiResponse.<AddClassResponse>builder()
                .result(classService.addClass(addClassRequest))
                .build();
    }

    @PutMapping("/set-head-teacher/{id}")
    public ApiResponse<ClassResponse> setHeadTeacher(@PathVariable Long id, @RequestBody HeadTeacherClassUpdate headTeacherClassUpdate) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.setHeadTeacher(id, headTeacherClassUpdate))
                .build();
    }

    @PutMapping("/add-student-to-class/{id}")
    public ApiResponse<AddStudentResponse> addStudentToClass(@PathVariable Long id, @RequestBody List<String> studentID) {
        return ApiResponse.<AddStudentResponse>builder()
                .result(classService.addStudentToClass(id, studentID))
                .build();
    }

    @GetMapping("/get-class-by-id/{id}")
    public ApiResponse<ClassResponse> getClassById(@PathVariable Long id) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.mapRespone(id))
             // .result(classService.getClassById(id))
                .build();
    }

    @PutMapping("/set-monitor/{classId}")
    public ApiResponse<ClassResponse> setMonitor(@PathVariable Long classId, @RequestParam String studentId) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.setMonitor(classId, studentId))
                .build();
    }

    @GetMapping("/get-class-by-school-year-id/{id}")
    public ApiResponse<List<ClassResponse>> getClassBySchoolYear(@PathVariable Long id) {
        return ApiResponse.<List<ClassResponse>>builder()
                .result(classService.getStudentClassesBySchoolYear(id))
                .build();
    }

    @PutMapping("/updateClass/{id}")
    public ApiResponse<ClassResponse> updateClass(@PathVariable Long id, @RequestBody ClassRequest classRequest) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.updateClasses(id, classRequest))
                .build();
    }
    @GetMapping("get-home-teacherid-in-class")
    public ApiResponse<Object> getHomeTeacherIdInClass(@RequestParam(value = "classId") Long classId) {
        return ApiResponse.<Object>builder()
                .result(classService.getHomeTeacher(classId))
                .build();
    }

    @PostMapping("/search-class-by-class-name")
    public ApiResponse<List<ClassResponse>> getClassByClassName(
            @RequestBody @Valid SearchClassRequest searchClassRequest,
            @RequestParam boolean active) {
        return ApiResponse.<List<ClassResponse>>builder()
                .result(classService.findAllClassesByClassName(searchClassRequest, active))
                .build();
    }


    @GetMapping("/get-list-student-by-class-id/{id}")
    public ApiResponse<List<GetStudentInfo>> getStudentByClassId(@PathVariable Long id) {
        return ApiResponse.<List<GetStudentInfo>>builder()
                .result(classService.getStudentByClassId(id))
                .build();
    }

    @PostMapping(
            path = "/add-student-to-class-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<AddStudentResponse> addStudentToClassByExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("classId") Long classId
    ) {
        AddStudentResponse result = classService.addStudentToClassByExcel(file, classId);
        return ApiResponse.<AddStudentResponse>builder()
                .message("Students added to class successfully.")
                .result(result)
                .build();
    }

    @GetMapping("/get-class-id-by-class-name-school-year")
    public ApiResponse<Object> getClassIdByClassNameAndSchoolYear(
            @RequestParam String className,
            @RequestParam String schoolYear) {
        return ApiResponse.builder()
                .result(classService.getClassIdByClassNameAndSchoolYear(className, schoolYear))
                .message("Lấy classId thành công")
                .build();
    }

    @GetMapping("/download-student-template")
    public void downloadStudentTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=student_email_template.xlsx");

            ByteArrayOutputStream out = ExcelTemplateUtils.generateStudentEmailTemplate();
            response.getOutputStream().write(out.toByteArray());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Error generating Excel template: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-current-class-by-student-id/{studentId}")
    public ApiResponse<ClassResponse> getClassByStudentId(@PathVariable String studentId) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.getClassByStudentId(studentId))
                .build();
    }

    @GetMapping("/get-class-by-ids")
    public ApiResponse<List<BasicClassResponse>> getClassByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<BasicClassResponse>>builder()
                .result(classService.getClassByIds(ids))
                .build();
    }

    @GetMapping("/get-class-of-homeroom")
    public ApiResponse<ClassResponse> getClassOfHomeroomTeacher(
            @RequestParam String teacherId,
            @RequestParam Long schoolYearId
    ) {
        return ApiResponse.<ClassResponse>builder()
                .result(classService.getClassByHomeroomAndSchoolYear(teacherId, schoolYearId))
                .build();
    }
}