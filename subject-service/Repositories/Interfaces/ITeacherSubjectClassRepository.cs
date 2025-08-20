using subject_service.Common.Pagination;
using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface ITeacherSubjectClassRepository
    {
        //Task<(IEnumerable<TeacherSubjectClass>, int)> GetAllAsync();
        Task<(PagedResult<TeacherSubjectClass>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll);
        Task<(List<TeacherSubjectClass>, int)> GetByConditionAsync(Expression<Func<TeacherSubjectClass, bool>> expression);
        Task<(TeacherSubjectClass, int)> GetByIdAsync(string teacherId, long subjectId, long classId);
        Task<int> AddAsync(TeacherSubjectClass entity);
        Task<int> UpdateAsync(TeacherSubjectClass entity);
        Task<int> DeleteAsync(string teacherId, long subjectId, long classId);
    }
}
