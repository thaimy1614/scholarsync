using logbook_service.Models;
using logbook_service.Models.Dtos;
using System.Linq.Expressions;

namespace logbook_service.Repositories.Interfaces
{
    public interface ICompetitionRuleRepository
    {
        Task<(PagedResult<CompetitionRule>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(CompetitionRule, int)> GetByIdAsync(long id);
        Task<(List<CompetitionRule>, int)> GetByConditionAsync(Expression<Func<CompetitionRule, bool>> expression);
        Task<(CompetitionRule, int)> GetEntityExistAsync(CompetitionRule entity);
        Task<int> AddAsync(CompetitionRule entity);
        Task<int> UpdateAsync(CompetitionRule entity);
        Task<int> DeleteAsync(long id);
    }
}