package com.datn.school_service.Services.Grade;

import com.datn.school_service.Dto.Request.Grade.AddGradeRequest;
import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.GradeMapper;
import com.datn.school_service.Models.Grade;
import com.datn.school_service.Repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService implements GradeServiceInterface {
    final GradeRepository gradeRepository;
    final GradeMapper gradeMapper;

    @Override
    public Page<GradeResponse> getAll(Pageable pageable, boolean active) {
        Page<Grade> gradePage;
        if (active) {
            gradePage = gradeRepository.findAllByIsActiveTrue(pageable);
        } else {
            gradePage = gradeRepository.findAllByIsActiveFalse(pageable);
        }
        return gradePage.map(gradeMapper::toGradeResponse);
    }

    @Override
    public GradeResponse getGradeById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));

        return gradeMapper.toGradeResponse(grade);
    }

    @Override
    public void createGrade(AddGradeRequest addNewTypeRequest) {
        if (addNewTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean exists = gradeRepository.existsByGradeName(addNewTypeRequest.getGradeName());
        if (exists) {
            throw new AppException(ErrorCode.GRADE_NAME_ALREADY_EXIT);
        }
        Grade grade = gradeMapper.toGrade(addNewTypeRequest);

        gradeRepository.save(grade);

    }

    @Override
    public void updateGrade(Long id, AddGradeRequest updateNewTypeRequest) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Grade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));

        if (updateNewTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (!existingGrade.getGradeName().equals(updateNewTypeRequest.getGradeName()) &&
                gradeRepository.existsByGradeName(updateNewTypeRequest.getGradeName())) {
            throw new AppException(ErrorCode.GRADE_NAME_ALREADY_EXIT);
        }

        gradeMapper.updateGrade(existingGrade, updateNewTypeRequest);
        gradeRepository.save(existingGrade);
    }

    @Override
    public void deleteGrade(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Grade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));
        if(!existingGrade.isActive())
        {
            throw new AppException(ErrorCode.GRADE_IS_DELETED);
        }
        existingGrade.setActive(false);
        gradeRepository.save(existingGrade);
    }

    @Override
    public List<GradeResponse> searchGrade(AddGradeRequest keyword, boolean active) {
        if (keyword == null || keyword.getGradeName() == null || keyword.getGradeName().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getGradeName().trim();
        List<Grade> found;
        if (active) {
            found = gradeRepository.findAllByIsActiveTrueAndGradeNameContainingIgnoreCase(newKeyword);
        } else {
            found = gradeRepository.findAllByIsActiveFalseAndGradeNameContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.GRADE_NOT_FOUND);
        }
        return found.stream()
                .map(gradeMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeResponse> getGradesByIds(List<Long> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        List<Grade> grades = gradeRepository.findAllByGradeIdInAndIsActiveTrue(gradeIds);
        if (grades.isEmpty()) {
            return List.of();
        }
        return grades.stream()
                .map(gradeMapper::toGradeResponse)
                .collect(Collectors.toList());
    }
}
