using logbook_service.Models;
using logbook_service.Models.Dtos;
using System.Linq.Expressions;

namespace logbook_service.Repositories.Interfaces
{
    public interface ICompetitionPeriodScoreRepository
    {
        Task<(PagedResult<CompetitionPeriodScore>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(CompetitionPeriodScore, int)> GetByIdAsync(long id);
        Task<(List<CompetitionPeriodScore>, int)> GetByConditionAsync(Expression<Func<CompetitionPeriodScore, bool>> expression);
        Task<(CompetitionPeriodScore, int)> GetEntityExistAsync(CompetitionPeriodScore entity);
        Task<int> AddAsync(CompetitionPeriodScore entity);
        Task<int> UpdateAsync(CompetitionPeriodScore entity);
        Task<int> DeleteAsync(long id);
    }
}