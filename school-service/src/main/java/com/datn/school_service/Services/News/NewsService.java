package com.datn.school_service.Services.News;
import com.datn.school_service.Dto.Request.News.AddNewsRequest;
import com.datn.school_service.Dto.Request.News.SearchNewsRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.NewsResponse;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.datn.school_service.Mapper.ClassMapper;
import com.datn.school_service.Mapper.NewsMapper;
import com.datn.school_service.Mapper.NewsTypeMapper;
import com.datn.school_service.Models.Class;
import com.datn.school_service.Models.News;
import com.datn.school_service.Models.NewsType;
import com.datn.school_service.Repository.ClassRepository;
import com.datn.school_service.Repository.NewsRepository;
import com.datn.school_service.Repository.NewsTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService implements NewsServiceInterface {

    final NewsRepository newsRepository;

    final NewsMapper newsMapper;

    final ClassMapper classMapper;

    final NewsTypeMapper newsTypeMapper;

    final ClassRepository classRepository;

    final NewsTypeRepository newsTypeRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public Page<NewsResponse> getAll(Pageable pageable, boolean active) {
        Page<News> newsPage = active
                ? newsRepository.findAllByIsActiveTrue(pageable)
                : newsRepository.findAllByIsActiveFalse(pageable);

        List<NewsResponse> responses = newsPage.getContent().stream().map(news -> {
            NewsResponse newsResponse = newsMapper.toNewsResponse(news);

            // Gán thông tin newsType nếu tồn tại
            newsTypeRepository.findById(news.getNewsTypeId()).ifPresent(newsType ->
                    newsResponse.setNewsTypeResponse(newsTypeMapper.toNewsTypeResponse(newsType))
            );

            // Gọi userServiceClient để lấy tên chủ sở hữu
            String ownerId = news.getNewsOwner();
            if (ownerId != null) {
                try {
                    List<String> userIdList = List.of(ownerId);
                    var rawResult = userServiceClient.getUsersByIds(userIdList).getResult();

                    if (rawResult != null && !rawResult.isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        GetUserNameResponse getUserNameResponse = mapper.convertValue(rawResult.get(0), GetUserNameResponse.class);
                        newsResponse.setNewsOwnerName(getUserNameResponse.getFullName());
                    } else {
                        newsResponse.setNewsOwnerName("Không xác định");
                    }
                } catch (Exception e) {
                    // Ghi log lỗi nhưng không dừng chương trình
                    log.error("Lỗi khi lấy tên người dùng với ID = {}: {}", ownerId, e.getMessage());
                    newsResponse.setNewsOwnerName("no owner");
                }
            } else {
                newsResponse.setNewsOwnerName("no owner");
            }

            return newsResponse;
        }).collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, newsPage.getTotalElements());
    }



    @Override
    public NewsResponse getNewsById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        List<String> userId = new ArrayList<>();

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        NewsType newsType = newsTypeRepository.findById(news.getNewsTypeId()).orElseThrow(() -> new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND));
        NewsResponse newsResponse =  newsMapper.toNewsResponse(news);
        newsResponse.setNewsTypeResponse(newsTypeMapper.toNewsTypeResponse(newsType));
        userId.add(news.getNewsOwner());
        var rawResult = userServiceClient.getUsersByIds(userId).getResult();
        ObjectMapper mapper = new ObjectMapper();
        GetUserNameResponse getUserNameResponse = mapper.convertValue(rawResult.get(0), GetUserNameResponse.class);
        newsResponse.setNewsOwnerName(getUserNameResponse.getFullName());
        return newsResponse;
    }

    @Override
    public void createNews(AddNewsRequest addNewsRequest) {
        if (addNewsRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        ApiResponse<Map<String, Boolean>> result;
        boolean existsTitle = newsRepository.existsByNewsTitle(addNewsRequest.getNewsTitle());

         if(existsTitle){
            throw new AppException(ErrorCode.NEWS_TITLE_ALREADY_EXIT);
        }
        if(!newsTypeRepository.existsByNewsTypeIdAndIsActiveTrue(addNewsRequest.getNewsTypeId()))
        {
            throw new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND);
        }
        String ownerId = addNewsRequest.getNewsOwner();
        List<String> userIdList = List.of(ownerId);

        try {
            result = userServiceClient.checkUserRole(userIdList, "PRINCIPAL");
        } catch (Exception ex) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: " + ex.getMessage());
        }

        if (result == null || !result.getResult().getOrDefault(ownerId, false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Principal with ID " + ownerId);
        }


        News news = newsMapper.toNews(addNewsRequest);

        newsRepository.save(news);

    }

    @Override
    public void updateNews(Long id, AddNewsRequest addNewsRequest) {
        ApiResponse<Map<String, Boolean>> result;
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (addNewsRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        boolean existsContent = newsRepository.existsByNewsContent(addNewsRequest.getNewsContent());

            boolean existsTitle = newsRepository.existsByNewsTitle(addNewsRequest.getNewsTitle());



        if(!newsTypeRepository.existsByNewsTypeIdAndIsActiveTrue(addNewsRequest.getNewsTypeId()))
        {
            throw new AppException(ErrorCode.NEWS_TYPE_NOT_FOUND);
        }

       else if(existsTitle && !addNewsRequest.getNewsTitle().equalsIgnoreCase(news.getNewsTitle())){
            throw new AppException(ErrorCode.NEWS_TITLE_ALREADY_EXIT);
        }
        String ownerId = addNewsRequest.getNewsOwner();
        List<String> userIdList = List.of(ownerId);

        try {
            result = userServiceClient.checkUserRole(userIdList, "PRINCIPAL");
        } catch (Exception ex) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Error calling user service: " + ex.getMessage());
        }

        if (result == null || !result.getResult().getOrDefault(ownerId, false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Principal with ID " + ownerId);
        }


        newsMapper.toUpdateNews(news, addNewsRequest);
        newsRepository.save(news);
    }

    @Override
    public void deleteNews(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        if(!news.isActive())
        {
            throw new AppException(ErrorCode.NEWS_IS_DELETED);
        }
        news.setActive(false);
        newsRepository.save(news);
    }

    @Override
    public void restoreNews(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        if(news.isActive())
        {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE, news.getNewsTitle());
        }
        news.setActive(true);
        newsRepository.save(news);
    }

    @Override
    public List<NewsResponse> searchNews(SearchNewsRequest keyword, boolean active) {
        if (keyword == null || keyword.getNewsKeySearch() == null || keyword.getNewsKeySearch().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getNewsKeySearch().trim();
        List<News> found;
        if(active) {
            found = newsRepository.findAllByIsActiveTrueAndNewsContentContainingIgnoreCaseOrIsActiveTrueAndNewsTitleContainingIgnoreCaseOrIsActiveTrueAndNewsOwnerContainingIgnoreCase(newKeyword,newKeyword,newKeyword);
        }
        else
        {
            found = newsRepository.findAllByIsActiveFalseAndNewsContentContainingIgnoreCaseOrIsActiveFalseAndNewsTitleContainingIgnoreCaseOrIsActiveFalseAndNewsOwnerContainingIgnoreCase(newKeyword,newKeyword,newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.NEWS_NOT_FOUND);
        }

        return found.stream().map(news -> {
            NewsResponse newsResponse = newsMapper.toNewsResponse(news);

            newsTypeRepository.findById(news.getNewsTypeId()).ifPresent(newsType ->
                    newsResponse.setNewsTypeResponse(newsTypeMapper.toNewsTypeResponse(newsType))
            );
            List<String> userId = new ArrayList<>();
            userId.add(news.getNewsOwner());
            var rawResult = userServiceClient.getUsersByIds(userId).getResult();
            ObjectMapper mapper = new ObjectMapper();
            GetUserNameResponse getUserNameResponse = mapper.convertValue(rawResult.get(0), GetUserNameResponse.class);
            newsResponse.setNewsOwnerName(getUserNameResponse.getFullName());
            return newsResponse;
        }).collect(Collectors.toList());

    }



}
