package com.datn.school_service.Repository;

import com.datn.school_service.Models.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    boolean existsClassByClassName(String classname);
    boolean existsByClassIdAndStudentIdContaining(Long classId, String studentId);
    int countClassByStudentId(List<String> studentId);
    @Query("SELECT SIZE(c.studentId) FROM Class c WHERE c.classId = :classId")
    int countStudentsInClass(@Param("classId") Long classId);
    List<Class> findAllBySchoolYearId(long schoolYearId);

    Class findClassByClassId(Long classId);
    Page<Class> findAllBySchoolYearId(long schoolYearId, Pageable pageable);

    Page<Class> findAllByClassActiveTrue(Pageable pageable);

    Page<Class> findAllByClassActiveFalse(Pageable pageable);

    boolean existsClassByStudentIdAndSchoolYearId(String studentId,Long schoolYearId);

    boolean existsByClassNameAndSchoolYearId(String className, Long schoolYearId);

    Class findClassIdByClassNameContainingIgnoreCaseAndSchoolYearId(String className,Long schoolYearId);

    List<Class> findAllByClassActiveTrueAndClassNameContainingIgnoreCase(String className);
    List<Class> findAllByClassActiveFalseAndClassNameContainingIgnoreCase(String className);

    Class getClassByClassIdAndClassActive(Long classId, boolean classActive);

    // Kiểm tra xem giáo viên đã là GVCN của lớp nào trong cùng schoolYear chưa
    @Query("SELECT COUNT(c) > 0 FROM Class c WHERE c.homeroomTeacherId = :teacherId AND c.schoolYearId = :schoolYearId AND c.classId <> :classId")
    boolean existsByHomeroomTeacherIdAndSchoolYearIdAndNotClassId(
            @Param("teacherId") String teacherId,
            @Param("schoolYearId") Long schoolYearId,
            @Param("classId") Long classId
    );

    @Query("""
      SELECT c
      FROM Class c
      JOIN SchoolYear sy ON sy.schoolYearId = c.schoolYearId
      WHERE LOWER(c.className) LIKE LOWER(CONCAT('%', :className, '%'))
        AND sy.schoolYear = :schoolYear
    """)
    Class findByClassNameAndSchoolYear(
            @Param("className") String className,
            @Param("schoolYear") String schoolYear
    );

    List<Class> findAllBySchoolYearIdAndGrade_GradeId(Long schoolYearId, Long gradeId);

    @Query("""
    SELECT DISTINCT c FROM Class c
    WHERE :studentId MEMBER OF c.studentId
    AND c.schoolYearId = :schoolYearId
""")
    List<Class> findByStudentIdAndSchoolYearId(
            @Param("studentId") String studentId,
            @Param("schoolYearId") Long schoolYearId
    );

    Class findByHomeroomTeacherIdAndSchoolYearId(String teacherId, Long schoolYearId);
}
