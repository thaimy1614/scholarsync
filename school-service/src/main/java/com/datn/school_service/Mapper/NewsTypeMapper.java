package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.AddNewsTypeRequest;
import com.datn.school_service.Dto.Request.UpdateNewTypeRequest;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Models.NewsType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface NewsTypeMapper {
    NewsType toNewsType(AddNewsTypeRequest addNewsTypeRequest);


    NewsTypeResponse toNewsTypeResponse(NewsType newsType);

    void toUpdateNewType(@MappingTarget NewsType newsType, UpdateNewTypeRequest updateNewTypeRequest);

    NewsType toNewsTypeUpdate(UpdateNewTypeRequest updateNewTypeRequest);


}
