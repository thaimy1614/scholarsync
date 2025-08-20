using logbook_service.Models;
using logbook_service.Models.Dtos;

namespace logbook_service.Services.Interfaces
{
    public interface ICompetitionDailyDetailService
    {
        Task<(PagedResult<CompetitionDailyDetailWithDeletedDto>, int)> GetAllCompetitionDailyDetails(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<CompetitionDailyDetailDto>, int)> GetAllExistingCompetitionDailyDetails(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(CompetitionDailyDetailDto, int)> GetCompetitionDailyDetailById(long id);
        Task<int> AddCompetitionDailyDetail(CompetitionDailyDetailCreateUpdateDto CompetitionDailyDetailCreateDto);
        Task<int> UpdateCompetitionDailyDetail(long id, CompetitionDailyDetailCreateUpdateDto CompetitionDailyDetailUpdateDto);
        Task<int> DeleteCompetitionDailyDetail(long id);
    }
}