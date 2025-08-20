using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface ISubjectClassRepository
    {
        Task<(PagedResult<SubjectClass>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(SubjectClass, int)> GetByIdAsync(long subjectId, long classId);
        Task<(List<SubjectClass>, int)> GetByConditionAsync(Expression<Func<SubjectClass, bool>> expression);
        Task<(SubjectClass, int)> GetEntityExistAsync(SubjectClass entity);
        Task<int> AddAsync(SubjectClass entity);
        Task<int> UpdateAsync(SubjectClass entity);
        Task<int> DeleteAsync(long subjectId, long classId);
    }
}