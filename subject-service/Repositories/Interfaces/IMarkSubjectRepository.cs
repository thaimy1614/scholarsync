using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface IMarkSubjectRepository
    {
        Task<(PagedResult<MarkSubject>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(MarkSubject, int)> GetByIdAsync(long id);
        Task<(List<MarkSubject>, int)> GetByConditionAsync(Expression<Func<MarkSubject, bool>> expression);
        Task<(MarkSubject, int)> GetEntityExistAsync(MarkSubject entity);
        Task<int> AddAsync(MarkSubject entity);
        Task<int> UpdateAsync(MarkSubject entity);
        Task<int> DeleteAsync(long id);
    }
}