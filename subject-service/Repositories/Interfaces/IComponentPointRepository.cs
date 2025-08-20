using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface IComponentPointRepository
    {
        Task<(PagedResult<ComponentPoint>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool isDeleted);
        Task<(ComponentPoint, int)> GetByIdAsync(long id);
        Task<(List<ComponentPoint>, int)> GetByConditionAsync(Expression<Func<ComponentPoint, bool>> expression);
        Task<(ComponentPoint, int)> GetEntityExistAsync(ComponentPoint entity);
        Task<int> AddAsync(ComponentPoint entity);
        Task<int> UpdateAsync(ComponentPoint entity);
        Task<int> DeleteAsync(long id);
    }
}
