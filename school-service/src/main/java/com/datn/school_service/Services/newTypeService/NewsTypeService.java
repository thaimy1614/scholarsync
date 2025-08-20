package com.datn.school_service.Services.newTypeService;

import com.datn.school_service.Dto.Request.AddNewsTypeRequest;
import com.datn.school_service.Dto.Request.SearchNewsTypeRequest;
import com.datn.school_service.Dto.Request.UpdateNewTypeRequest;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.NewsTypeMapper;
import com.datn.school_service.Models.NewsType;
import com.datn.school_service.Repository.NewsTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class NewsTypeService implements NewsTypeServiceInterface {

    final NewsTypeRepository newsTypeRepository;

    final NewsTypeMapper newsTypeMapper;

    @Override
    public Page<NewsTypeResponse> getAll(Pageable pageable, boolean active) {
        Page<NewsType> newsTypePage;
        if (active) {
            newsTypePage = newsTypeRepository.findAllByIsActiveTrue(pageable);
        } else {
            newsTypePage = newsTypeRepository.findAllByIsActiveFalse(pageable);
        }
        return newsTypePage.map(newsTypeMapper::toNewsTypeResponse);
    }

    @Override
    public NewsTypeResponse getNewsTypeById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        NewsType newsType = newsTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND));

        return newsTypeMapper.toNewsTypeResponse(newsType);
    }

    @Override
    public void createNewsType(AddNewsTypeRequest addNewTypeRequest) {
        if (addNewTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean exists = newsTypeRepository.existsByNewsTypeName(addNewTypeRequest.getNewsTypeName());
        if (exists) {
            throw new AppException(ErrorCode.NEWS_TYPENAME_ALREADY_EXIT);
        }
        NewsType newstype = newsTypeMapper.toNewsType(addNewTypeRequest);

        newsTypeRepository.save(newstype);

    }

    @Override
    public void updateNewsType(Long id, UpdateNewTypeRequest updateNewTypeRequest) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        NewsType existingNewsType = newsTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND));

        if (updateNewTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (!existingNewsType.getNewsTypeName().equals(updateNewTypeRequest.getNewsTypeName()) &&
                newsTypeRepository.existsByNewsTypeName(updateNewTypeRequest.getNewsTypeName())) {
            throw new AppException(ErrorCode.NEWS_TYPENAME_ALREADY_EXIT);
        }

        newsTypeMapper.toUpdateNewType(existingNewsType, updateNewTypeRequest);
        newsTypeRepository.save(existingNewsType);
    }

    @Override
    public void deleteNewsType(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        NewsType existingNewsType = newsTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND));
        if(!existingNewsType.isActive())
        {
            throw new AppException(ErrorCode.NEWS_TYPE_IS_DELETED);
        }
        existingNewsType.setActive(false);
        newsTypeRepository.save(existingNewsType);
    }

    @Override
    public void restoreNewsType(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        NewsType existingNewsType = newsTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND));
        if(existingNewsType.isActive())
        {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE, existingNewsType.getNewsTypeName());
        }
        existingNewsType.setActive(false);
        newsTypeRepository.save(existingNewsType);
    }

    @Override
    public List<NewsTypeResponse> searchNewsType(SearchNewsTypeRequest keyword, boolean active) {
        if (keyword == null || keyword.getNewsTypeName() == null || keyword.getNewsTypeName().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getNewsTypeName().trim();
        List<NewsType> found;
        if (active) {
            found = newsTypeRepository.findAllByIsActiveTrueAndNewsTypeNameContainingIgnoreCase(newKeyword);
        } else {
            found = newsTypeRepository.findAllByIsActiveFalseAndNewsTypeNameContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND);
        }
        return found.stream()
                .map(newsTypeMapper::toNewsTypeResponse)
                .collect(Collectors.toList());
    }

}
