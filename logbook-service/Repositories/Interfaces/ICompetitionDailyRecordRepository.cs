using logbook_service.Models;
using logbook_service.Models.Dtos;
using System.Linq.Expressions;

namespace logbook_service.Repositories.Interfaces
{
    public interface ICompetitionDailyRecordRepository
    {
        Task<(PagedResult<CompetitionDailyRecord>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(CompetitionDailyRecord, int)> GetByIdAsync(long id);
        Task<(List<CompetitionDailyRecord>, int)> GetByConditionAsync(Expression<Func<CompetitionDailyRecord, bool>> expression);
        Task<(CompetitionDailyRecord, int)> GetEntityExistAsync(CompetitionDailyRecord entity);
        Task<int> AddAsync(CompetitionDailyRecord entity);
        Task<int> UpdateAsync(CompetitionDailyRecord entity);
        Task<int> DeleteAsync(long id);
    }
}