using subject_service.Common.Pagination;
using subject_service.Dtos.SubjectClass;

namespace subject_service.Services.Interfaces
{
    public interface ISubjectClassService
    {
        Task<(PagedResult<SubjectClassWithDeletedDto>, int)> GetAllSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<SubjectClassDto>, int)> GetAllExistingSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(SubjectClassDto, int)> GetSubjectClassById(long subjectId, long classId);
        Task<int> AddSubjectClass(SubjectClassCreateUpdateDto SubjectClassCreateDto);
        Task<(List<long>, int)> AssignSubjectToClasses(long subjectId, List<long> classIds);
        Task<int> UpdateSubjectClass(SubjectClassCreateUpdateDto subjectClassUpdateDto);
        Task<int> DeleteSubjectClass(long subjectId, long classId);
    }
}