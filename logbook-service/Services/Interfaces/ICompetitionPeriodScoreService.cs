using logbook_service.Models;
using logbook_service.Models.Dtos;

namespace logbook_service.Services.Interfaces
{
    public interface ICompetitionPeriodScoreService
    {
        Task<(PagedResult<CompetitionPeriodScoreWithDeletedDto>, int)> GetAllCompetitionPeriodScores(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<CompetitionPeriodScoreDto>, int)> GetAllExistingCompetitionPeriodScores(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(CompetitionPeriodScoreDto, int)> GetCompetitionPeriodScoreById(long id);
        Task<int> AddCompetitionPeriodScore(CompetitionPeriodScoreCreateUpdateDto CompetitionPeriodScoreCreateDto);
        Task<int> UpdateCompetitionPeriodScore(long id, CompetitionPeriodScoreCreateUpdateDto CompetitionPeriodScoreUpdateDto);
        Task<int> DeleteCompetitionPeriodScore(long id);
    }
}