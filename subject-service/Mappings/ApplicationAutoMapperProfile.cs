using AutoMapper;
using subject_service.Common.Pagination;
using subject_service.Dtos;
using subject_service.Dtos.ComponentPoint;
using subject_service.Dtos.Mark;
using subject_service.Dtos.MarkSubject;
using subject_service.Dtos.MarkType;
using subject_service.Dtos.StudentSubjectScore;
using subject_service.Dtos.Subject;
using subject_service.Dtos.SubjectClass;
using subject_service.Dtos.TeacherSubjectClass;
using subject_service.Models;

namespace subject_service.Mappings
{
    public class ApplicationAutoMapperProfile : Profile
    {
        public ApplicationAutoMapperProfile()
        {
            CreateMap<SubjectCreateUpdateDto, Subject>();
            CreateMap<Subject, SubjectDto>();
            CreateMap<Subject, SubjectWithDeletedDto>();

            CreateMap<MarkCreateUpdateDto, Mark>();
            CreateMap<Mark, MarkDto>();
            CreateMap<Mark, MarkWithDeletedDto>();

            CreateMap<MarkSubjectCreateUpdateDto, MarkSubject>();
            CreateMap<MarkSubject, MarkSubjectDto>();
            CreateMap<MarkSubject, MarkSubjectWithDeletedDto>();

            CreateMap<ComponentPointCreateUpdateDto, ComponentPoint>();
            CreateMap<ComponentPoint, ComponentPointDto>();
            CreateMap<ComponentPoint, ComponentPointWithDeletedDto>();

            CreateMap<MarkTypeCreateUpdateDto, MarkType>();
            CreateMap<MarkType, MarkTypeDto>();
            CreateMap<MarkType, MarkTypeWithDeletedDto>();

            CreateMap<TeacherSubjectClassCreateUpdateDto, TeacherSubjectClass>();
            CreateMap<TeacherSubjectClass, TeacherSubjectClassDto>();
            CreateMap<TeacherSubjectClass, TeacherSubjectClassWithDeletedDto>();
            //CreateMap<TeacherSubjectClassDto, TeacherSubjectClass>();

            CreateMap<SubjectClassCreateUpdateDto, SubjectClass>();
            CreateMap<SubjectClass, SubjectClassDto>();
            CreateMap<SubjectClass, SubjectClassWithDeletedDto>();
            //CreateMap<SubjectClassDto, SubjectClass>();

            CreateMap<ViewStudentSubjectScore, ViewStudentSubjectScoreDto>();

            CreateMap(typeof(PagedResult<>), typeof(PagedResult<>))
               .ConvertUsing(typeof(PagedResultConverter<,>));
        }
    }
}
