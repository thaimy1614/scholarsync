using logbook_service.Models;
using logbook_service.Models.Dtos;
using System.Linq.Expressions;

namespace logbook_service.Repositories.Interfaces
{
    public interface ICompetitionDailyDetailRepository
    {
        Task<(PagedResult<CompetitionDailyDetail>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(CompetitionDailyDetail, int)> GetByIdAsync(long id);
        Task<(List<CompetitionDailyDetail>, int)> GetByConditionAsync(Expression<Func<CompetitionDailyDetail, bool>> expression);
        Task<(CompetitionDailyDetail, int)> GetEntityExistAsync(CompetitionDailyDetail entity);
        Task<int> AddAsync(CompetitionDailyDetail entity);
        Task<int> UpdateAsync(CompetitionDailyDetail entity);
        Task<int> DeleteAsync(long id);
    }
}