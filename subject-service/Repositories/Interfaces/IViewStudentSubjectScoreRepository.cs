using subject_service.Models;
using System.Linq.Expressions;

namespace subject_service.Repositories.Interfaces
{
    public interface IViewStudentSubjectScoreRepository
    {
        Task<(IEnumerable<ViewStudentSubjectScore>, int)> GetAllAsync();
        Task<(List<ViewStudentSubjectScore>, int)> GetByConditionAsync(Expression<Func<ViewStudentSubjectScore, bool>> expression);
    }
}
