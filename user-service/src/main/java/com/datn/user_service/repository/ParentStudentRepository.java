package com.datn.user_service.repository;

import com.datn.user_service.model.Parent;
import com.datn.user_service.model.ParentStudent;
import com.datn.user_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    boolean existsByParentAndStudent(Parent parent, Student student);

    Optional<ParentStudent> findByParentAndStudent(Parent parent, Student student);

    List<ParentStudent> findAllByStudent(Student parent);
}