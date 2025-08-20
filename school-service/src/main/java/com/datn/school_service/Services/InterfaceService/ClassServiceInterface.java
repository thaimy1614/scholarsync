package com.datn.school_service.Services.InterfaceService;

import com.datn.school_service.Dto.Request.Class.AddClassRequest;
import com.datn.school_service.Dto.Request.Class.SearchClassRequest;
import com.datn.school_service.Dto.Request.ClassRequest;
import com.datn.school_service.Dto.Request.HeadTeacherClassUpdate;
import com.datn.school_service.Dto.Respone.AddClassResponse;
import com.datn.school_service.Dto.Respone.AddStudentResponse;
import com.datn.school_service.Dto.Respone.BasicClassResponse;
import com.datn.school_service.Dto.Respone.ClassResponse;
import com.datn.school_service.Dto.Respone.User.GetStudentInfo;
import com.datn.school_service.Dto.Respone.User.UserIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ClassServiceInterface {


    Page<ClassResponse> getAllClassesDelete(Pageable pageable);

    Page<ClassResponse> getAllClassesActive(Pageable pageable);

    ClassResponse updateClasses(Long ID, ClassRequest classRequest);

    AddClassResponse addClass(AddClassRequest classRequest);

   // List<AddStudentResponse> addStudentToClassByExcel(MultipartFile file);

    ClassResponse setHeadTeacher(Long id, HeadTeacherClassUpdate headTeacherClassUpdate);

    AddStudentResponse addStudentToClass(Long classId, List<String> studentIds);

    ClassResponse getClassById(Long id);

    Object getClassIdByClassNameAndSchoolYear(String className, String schoolYear);

    ClassResponse setMonitor(Long id, String studentId);

    List<ClassResponse> getStudentClassesBySchoolYear(Long id);

    void updateClass(Long id, ClassRequest classRequest);

    ClassResponse mapRespone(Long id);

    Object getHomeTeacher(Long classId);

    List<ClassResponse> findAllClassesByClassName(SearchClassRequest searchClassRequest, boolean active);

    List<GetStudentInfo> getStudentByClassId(Long classId);


    AddStudentResponse addStudentToClassByExcel(MultipartFile file, Long classId);

    //List<Object> test(List<String> emails);

    void promotedClass(Long gradeId,String schoolYear);

    ClassResponse getClassByStudentId(String studentId);

    List<BasicClassResponse> getClassByIds(List<Long> ids);

    ClassResponse getClassByHomeroomAndSchoolYear(String teacherId, Long schoolYearId);

//    void
}
