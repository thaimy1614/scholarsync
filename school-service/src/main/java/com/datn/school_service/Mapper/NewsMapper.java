package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.News.AddNewsRequest;
import com.datn.school_service.Dto.Respone.ClassResponse;
import com.datn.school_service.Dto.Respone.NewsResponse;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Models.News;
import com.datn.school_service.Repository.ClassRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {ClassMapper.class, NewsTypeMapper.class})
public interface NewsMapper {

    News toNews(AddNewsRequest addNewsRequest);

    NewsResponse toNewsResponse(News news);

    void toUpdateNews(@MappingTarget News news, AddNewsRequest addNewsRequest);


}
