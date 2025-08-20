using AutoMapper;
using logbook_service.Models.Dtos;

namespace logbook_service.Models.Mappings
{
    public class ApplicationAutoMapperProfile : Profile
    {
        public ApplicationAutoMapperProfile() 
        {
            CreateMap<CompetitionDailyDetailCreateUpdateDto, CompetitionDailyDetail>();
            CreateMap<CompetitionDailyDetail, CompetitionDailyDetailDto>();
            CreateMap<CompetitionDailyDetail, CompetitionDailyDetailWithDeletedDto>();

            CreateMap<CompetitionDailyRecordCreateUpdateDto, CompetitionDailyRecord>();
            CreateMap<CompetitionDailyRecord, CompetitionDailyRecordDto>();            
            CreateMap<CompetitionDailyRecord, CompetitionDailyRecordWithDeletedDto>();            

            CreateMap<CompetitionPeriodScoreCreateUpdateDto, CompetitionPeriodScore>();
            CreateMap<CompetitionPeriodScore, CompetitionPeriodScoreDto>();
            CreateMap<CompetitionPeriodScore, CompetitionPeriodScoreWithDeletedDto>();

            CreateMap<CompetitionRuleCreateUpdateDto, CompetitionRule>();
            CreateMap<CompetitionRule, CompetitionRuleDto>();
            CreateMap<CompetitionRule, CompetitionRuleWithDeletedDto>();

            CreateMap(typeof(PagedResult<>), typeof(PagedResult<>))
               .ConvertUsing(typeof(PagedResultConverter<,>));
        }
    }
}
