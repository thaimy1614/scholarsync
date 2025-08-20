using Microsoft.EntityFrameworkCore;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{ 
    public class ViewStudentSubjectScoreRepository : IViewStudentSubjectScoreRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public ViewStudentSubjectScoreRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<(IEnumerable<ViewStudentSubjectScore>, int)> GetAllAsync()
        {
            try
            {
                var result = await _dbContext.viewStudentSubjectScores.ToListAsync();
                return (result, 1000);
            }
            catch (Exception ex)
            {
                return (new List<ViewStudentSubjectScore>(), 1001);
            }
        }

        public async Task<(List<ViewStudentSubjectScore>, int)> GetByConditionAsync(Expression<Func<ViewStudentSubjectScore, bool>> expression)
        {
            try
            {
                var result = await _dbContext.viewStudentSubjectScores.AsNoTracking().Where(expression).ToListAsync();
                if (result == null)
                {
                    return (new(), 1002);
                }
                return (result, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }
    }
}
