using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface IMarkRepository
    {
        Task<(PagedResult<Mark>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool isDeleted);
        Task<(List<Mark>, int)> GetByConditionAsync(Expression<Func<Mark, bool>> expression);
        Task<(Mark, int)> GetByIdAsync(long id);
        Task<(Mark, int)> GetEntityExistAsync(Mark item);
        Task<int> AddAsync(Mark mark);
        Task<int> UpdateAsync(Mark mark);
        Task<int> DeleteAsync(long id);
    }
}
