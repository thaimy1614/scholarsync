package com.datn.school_service.Services.Grade;





import com.datn.school_service.Dto.Request.Grade.AddGradeRequest;
import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GradeServiceInterface {
    Page<GradeResponse> getAll(Pageable pageable, boolean active);

    GradeResponse getGradeById(Long id);

    void createGrade(AddGradeRequest addNewTypeRequest);

    void updateGrade(Long id, AddGradeRequest updateNewTypeRequest);

    void deleteGrade(Long id);

    List<GradeResponse> searchGrade(AddGradeRequest keyword, boolean active);

    List<GradeResponse> getGradesByIds(List<Long> gradeIds);
}
