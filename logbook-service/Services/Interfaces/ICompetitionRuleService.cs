using logbook_service.Models;
using logbook_service.Models.Dtos;

namespace logbook_service.Services.Interfaces
{
    public interface ICompetitionRuleService
    {
        Task<(PagedResult<CompetitionRuleWithDeletedDto>, int)> GetAllCompetitionRules(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<CompetitionRuleDto>, int)> GetAllExistingCompetitionRules(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(CompetitionRuleDto, int)> GetCompetitionRuleById(long id);
        Task<int> AddCompetitionRule(CompetitionRuleCreateUpdateDto CompetitionRuleCreateDto);
        Task<int> UpdateCompetitionRule(long id, CompetitionRuleCreateUpdateDto CompetitionRuleUpdateDto);
        Task<int> DeleteCompetitionRule(long id);
    }
}