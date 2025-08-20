package com.datn.user_service.service.parentStudent;

import com.datn.user_service.dto.response.ParentResponse;
import com.datn.user_service.dto.response.RelationshipResponse;
import com.datn.user_service.dto.response.StudentResponse;
import com.datn.user_service.exception.AppException;
import com.datn.user_service.exception.ErrorCode;
import com.datn.user_service.model.Parent;
import com.datn.user_service.model.ParentStudent;
import com.datn.user_service.model.Student;
import com.datn.user_service.repository.ParentRepository;
import com.datn.user_service.repository.ParentStudentRepository;
import com.datn.user_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentStudentServiceImpl implements ParentStudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllChildrenOfParent(String parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        return parent.getStudents().stream()
                .map(ParentStudent::getStudent)
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParentResponse> getAllParentsOfChild(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        return student.getParents().stream()
                .map(ParentStudent::getParent)
                .map(this::mapToParentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addParentStudentRelation(String parentId, String studentId, ParentStudent.ParentType parentType) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));

        boolean relationExists = parentStudentRepository.existsByParentAndStudent(parent, student);
        if (relationExists) {
            throw new AppException(ErrorCode.PARENT_STUDENT_RELATION_EXISTED);
        }

        ParentStudent parentStudent = ParentStudent.builder()
                .parent(parent)
                .student(student)
                .parentType(parentType)
                .build();
        parentStudentRepository.save(parentStudent);
    }

    @Transactional
    public void removeParentStudentRelation(String parentId, String studentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));

        ParentStudent parentStudent = parentStudentRepository.findByParentAndStudent(parent, student)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_STUDENT_RELATION_NOT_EXISTED));
        parentStudentRepository.delete(parentStudent);
    }

    @Override
    public RelationshipResponse getRelationship(String parentId, String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        ParentStudent parentStudent = parentStudentRepository.findByParentAndStudent(parent, student).orElseThrow(
                () -> new AppException(ErrorCode.PARENT_STUDENT_RELATION_NOT_EXISTED));

        return RelationshipResponse.builder()
                .parentType(parentStudent.getParentType())
                .parent(mapToParentResponse(parent))
                .student(mapToStudentResponse(student))
                .build();
    }

    @Transactional
    public void updateParentType(String parentId, String studentId, ParentStudent.ParentType parentType) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));

        ParentStudent parentStudent = parentStudentRepository.findByParentAndStudent(parent, student)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_STUDENT_RELATION_NOT_EXISTED));
        parentStudent.setParentType(parentType);
        parentStudentRepository.save(parentStudent);
    }

    @Transactional(readOnly = true)
    public List<ParentResponse> getParentsByTypeForChild(String studentId, ParentStudent.ParentType type) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        return student.getParents().stream()
                .filter(ps -> ps.getParentType() == type)
                .map(ParentStudent::getParent)
                .map(this::mapToParentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean checkParentStudentRelation(String parentId, String studentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        return parentStudentRepository.existsByParentAndStudent(parent, student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getChildrenByParentType(String parentId, ParentStudent.ParentType type) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_EXISTED));
        return parent.getStudents().stream()
                .filter(ps -> ps.getParentType() == type)
                .map(ParentStudent::getStudent)
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void notifyParentsOfChild(String studentId, String message) {
        List<ParentResponse> parents = getAllParentsOfChild(studentId);
        for (ParentResponse parent : parents) {
            if (parent.getIsNotificationOn() != null && parent.getIsNotificationOn()) {
                // TODO: Implement actual notification logic
                System.out.println("Sending notification to " + parent.getEmail() + ": " + message);
            }
        }
    }

    private StudentResponse mapToStudentResponse(Student student) {
        return StudentResponse.builder()
                .userId(student.getUserId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .address(student.getAddress())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .image(student.getImage())
                .createdAt(student.getCreateAt())
                .enrollmentYear(student.getEnrollmentYear())
                .status(student.getStatus())
                .build();
    }

    private ParentResponse mapToParentResponse(Parent parent) {
        List<ParentStudent> parentStudents = parent.getStudents();
        List<StudentResponse> students = parentStudents.stream()
                .map(ParentStudent::getStudent)
                .map(this::mapToStudentResponse)
                .toList();
        return ParentResponse.builder()
                .userId(parent.getUserId())
                .fullName(parent.getFullName())
                .email(parent.getEmail())
                .phoneNumber(parent.getPhoneNumber())
                .address(parent.getAddress())
                .gender(parent.getGender())
                .dateOfBirth(parent.getDateOfBirth())
                .image(parent.getImage())
                .createdAt(parent.getCreateAt())
                .isNotificationOn(parent.getIsNotificationOn())
                .students(students)
                .build();
    }
}