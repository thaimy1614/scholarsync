package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.Class.AddClassRequest;
import com.datn.school_service.Dto.Request.ClassRequest;
import com.datn.school_service.Dto.Request.HeadTeacherClassUpdate;
import com.datn.school_service.Dto.Respone.AddClassResponse;
import com.datn.school_service.Dto.Respone.ClassResponse;
import com.datn.school_service.Models.Class;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {RoomMapper.class,GradeMapper.class,RoomTypeMapper.class})
public interface ClassMapper {

    @Mapping(target = "numberStudent", expression = "java(clazz.getStudentId() != null ? clazz.getStudentId().size() : 0)")
    @Mapping(source = "grade", target = "gradeResponse")
    @Mapping(source = "room", target = "roomResponse")
    ClassResponse toClassRespone(Class clazz);

    @Mapping(target = "studentId", source = "studentId") // Thêm ánh xạ danh sách studentId
    Class toClass(ClassRequest classRequest);
    @Mapping(target = "studentId", source = "studentId")
    void updateClass(@MappingTarget Class clazz, ClassRequest classRequest);

    Class toClasses(HeadTeacherClassUpdate headTeacherClassUpdate);

    void setHeadTeacher(@MappingTarget Class classs, HeadTeacherClassUpdate headTeacherClassUpdate);

    Class toClass(AddClassRequest addClassRequest);

    AddClassResponse toAddClassResponse(Class clazz);

}
