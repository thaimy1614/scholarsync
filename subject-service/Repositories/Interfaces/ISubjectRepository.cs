using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface ISubjectRepository
    {
        Task<(PagedResult<Subject>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(Subject, int)> GetByIdAsync(long id);
        Task<(List<Subject>, int)> GetByConditionAsync(Expression<Func<Subject, bool>> expression);
        Task<(PagedResult<Subject>, int)> SearchAsync(string? keyword, int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(Subject, int)> GetEntityExistAsync(Subject entity);
        Task<int> AddAsync(Subject entity);
        Task<int> UpdateAsync(Subject entity);
        Task<int> DeleteAsync(long id);
    }
}
