using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface IMarkTypeRepository
    {
        Task<(PagedResult<MarkType>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool isDeleted);
        Task<(MarkType, int)> GetByIdAsync(long id);
        Task<(List<MarkType>, int)> GetByConditionAsync(Expression<Func<MarkType, bool>> expression);
        Task<(MarkType, int)> GetEntityExistAsync(MarkType item);
        Task<int> AddAsync(MarkType entity);
        Task<int> UpdateAsync(MarkType entity);
        Task<int> DeleteAsync(long id);
    }
}
