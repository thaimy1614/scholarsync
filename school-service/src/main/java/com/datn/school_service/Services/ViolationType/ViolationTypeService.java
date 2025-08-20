package com.datn.school_service.Services.ViolationType;

import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Request.ViolationType.SearchViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.ViolationTypeMapper;
import com.datn.school_service.Models.ViolationType;
import com.datn.school_service.Repository.ViolationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.datn.school_service.Models.ViolationType.ViolationCategory.GROUP;
import static com.datn.school_service.Models.ViolationType.ViolationCategory.INDIVIDUAL;

@Service
@RequiredArgsConstructor
public class ViolationTypeService implements ViolationTypeServiceInterface{
    final ViolationTypeRepository violationTypeRepository;

   // final QuestionRepository questionRepository;

    final ViolationTypeMapper violationTypeMapper;

    @Override
    public Page<ViolationTypeResponse> getAll(Pageable pageable, boolean active) {
        Page<ViolationType> violationTypePage;
        if (active) {
            violationTypePage = violationTypeRepository.findAllByIsActiveTrue(pageable);
        } else {
            violationTypePage = violationTypeRepository.findAllByIsActiveFalse(pageable);
        }

        return violationTypePage.map(violationTypeMapper::toViolationTypeResponse);

    }

    @Override
    public ViolationTypeResponse getViolationTypeById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        ViolationType violationType = violationTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));

        return violationTypeMapper.toViolationTypeResponse(violationType);
    }

    @Override
    public void createIndividualViolations(AddViolationTypeRequest addViolationTypeRequest) {

    }

    @Override
    public void createCollectiveViolations(AddViolationTypeRequest addViolationTypeRequest) {

    }

    @Override
    public void createViolationType(AddViolationTypeRequest addViolationTypeRequest) {

        if (addViolationTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean existsViolationType = violationTypeRepository.existsByViolationTypeName(addViolationTypeRequest.getViolationTypeName());
        if (existsViolationType) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_ALREADY_EXIT);
        }

        ViolationType violationType = violationTypeMapper.toViolationType(addViolationTypeRequest);

        violationTypeRepository.save(violationType);

    }

    @Override
    public void createViolationTypeForIndividual(AddViolationTypeRequest addViolationTypeRequest) {
        if (addViolationTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean existsViolationType = violationTypeRepository.existsByViolationTypeName(addViolationTypeRequest.getViolationTypeName());
        if (existsViolationType) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_ALREADY_EXIT);
        }

        ViolationType violationType = violationTypeMapper.toViolationType(addViolationTypeRequest);
        violationType.setViolationCategory(INDIVIDUAL);

        violationTypeRepository.save(violationType);
    }

    @Override
    public void createViolationTypeForGroup(AddViolationTypeRequest addViolationTypeRequest) {
        if (addViolationTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean existsViolationType = violationTypeRepository.existsByViolationTypeName(addViolationTypeRequest.getViolationTypeName());
        if (existsViolationType) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_ALREADY_EXIT);
        }

        ViolationType violationType = violationTypeMapper.toViolationType(addViolationTypeRequest);
        violationType.setViolationCategory(GROUP);

        violationTypeRepository.save(violationType);
    }


    @Override
    public void updateViolationType(Long idQues, AddViolationTypeRequest addViolationTypeRequest) {
        if (idQues == null || addViolationTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        ViolationType existingViolationType = violationTypeRepository.findById(idQues)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));

        if (!existingViolationType.getViolationTypeName().equals(addViolationTypeRequest.getViolationTypeName()) &&
                violationTypeRepository.existsByViolationTypeName(addViolationTypeRequest.getViolationTypeName())) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_ALREADY_EXIT);
        }
        existingViolationType.setViolationTypeName(addViolationTypeRequest.getViolationTypeName());
        existingViolationType.setViolationPoint(addViolationTypeRequest.getViolationPoint());
        try {
            ViolationType.ViolationCategory category = ViolationType.ViolationCategory.valueOf(addViolationTypeRequest.getViolationCategory().toUpperCase());
            existingViolationType.setViolationCategory(category);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INPUT_INVALID, "Loại vi phạm không hợp lệ: " + addViolationTypeRequest.getViolationCategory());
        }

        violationTypeRepository.save(existingViolationType);
    }

    @Override
    public void deleteViolationType(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        ViolationType existingViolationType = violationTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));
        if (!existingViolationType.isActive()) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_IS_DELETED);
        }
        existingViolationType.setActive(false);
        violationTypeRepository.save(existingViolationType);
    }

    @Override
    public void restoreViolationType(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        ViolationType existingViolationType = violationTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND));
        if (existingViolationType.isActive()) {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE,"violationType");
        }
        existingViolationType.setActive(false);
        violationTypeRepository.save(existingViolationType);
    }

    @Override
    public List<ViolationTypeResponse> searchViolationType(SearchViolationTypeRequest keyword, boolean active) {
        if (keyword == null || keyword.getViolationTypeKeyWordName() == null || keyword.getViolationTypeKeyWordName().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getViolationTypeKeyWordName().trim();
        List<ViolationType> found;
        if (active) {
            found = violationTypeRepository.findAllByIsActiveTrueAndViolationTypeNameContainingIgnoreCase(newKeyword);
        } else {
            found = violationTypeRepository.findAllByIsActiveFalseAndViolationTypeNameContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND);
        }
        return found.stream()
                .map(violationTypeMapper::toViolationTypeResponse)
                .collect(Collectors.toList());
    }
    @Override
    public List<ViolationType> filterValidViolationTypesOrThrow(List<Long> violationTypeIds, String violationCategoryStr) {
        List<ViolationType> validViolations = new ArrayList<>();

        ViolationType.ViolationCategory expectedCategory;
        try {
            expectedCategory = ViolationType.ViolationCategory.valueOf(violationCategoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INPUT_INVALID, "Invalid violation category: " + violationCategoryStr);
        }

        if (violationTypeIds == null || violationTypeIds.isEmpty()) {
            throw new AppException(ErrorCode.INPUT_NULL, "ViolationTypeIds is null or empty");
        }

        for (Long id : violationTypeIds) {
            violationTypeRepository.findByViolationTypeIdAndIsActiveTrue(id)
                    .filter(vt -> vt.getViolationCategory() == expectedCategory)
                    .ifPresent(validViolations::add);
        }

        if (validViolations.isEmpty()) {
            throw new AppException(ErrorCode.VIOLATION_TYPE_NOT_FOUND, "No valid ViolationType match category " + expectedCategory);
        }

        return validViolations;
    }

}
