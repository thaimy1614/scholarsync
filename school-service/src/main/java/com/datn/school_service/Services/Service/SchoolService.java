package com.datn.school_service.Services.Service;

import com.datn.school_service.Dto.Request.SchoolRequest;
import com.datn.school_service.Dto.Respone.SchoolResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.ClassMapper;
import com.datn.school_service.Mapper.SchoolMapper;
import com.datn.school_service.Models.School;
import com.datn.school_service.Repository.SchoolRepository;
import com.datn.school_service.Services.InterfaceService.SchoolServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService implements SchoolServiceInterface {
    final SchoolRepository schoolRepository;
    final SchoolMapper schoolMapper;
    private final ClassMapper classMapper;

    @Override
    public SchoolResponse getSchoolInfo() {
        try {
            return schoolRepository.findAll()
                    .stream()
                    .findFirst()
                    .map(schoolMapper::toSchoolRespone)
                    .orElseThrow(() -> new AppException(ErrorCode.ENTITY_EMPTY, "School"));
        } catch (Exception e) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "School");
        }
    }

    @Override
    public SchoolResponse updateSchool(Long ID, SchoolRequest schoolRequest) {
        if (ID == null || schoolRequest == null) {
            throw new AppException(ErrorCode.SCHOOL_NOT_FOUND);
        }

        try {
            School school = schoolRepository.findById(ID)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_NOT_FOUND));

            schoolMapper.updateSchool(school, schoolRequest);

            return schoolMapper.toSchoolRespone(schoolRepository.saveAndFlush(school));
        } catch (DataAccessException e) {
            throw new AppException(ErrorCode.FAILED_SAVE_ENTITY, "school");
        }
    }

    @Override
    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(schoolMapper::toSchoolRespone)
                .collect(Collectors.toList());
    }
}
