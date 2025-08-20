using subject_service.Common.Pagination;
using subject_service.Dtos.Subject;

namespace subject_service.Services.Interfaces
{
    public interface ISubjectService
    {
        Task<(PagedResult<SubjectWithDeletedDto>, int)> GetAllSubjects(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<SubjectDto>, int)> GetAllExistingSubjects(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(List<SubjectDto>, int)> GetAllSubjectsByTeacherId(string teacherId);
        Task<(PagedResult<SubjectDto>, int)> SearchSubjects(string? keyword, int page, int pageSize, string sortColumn, bool isDescending, bool includeDeleted);
        Task<(SubjectDto, int)> GetSubjectById(long id);
        Task<(string, int)> GetSubjectNameById(long id);
        Task<(List<SubjectDto>, int)> GetAllSubjectsByIds(List<long> ids);
        //Task<(Dictionary<long, bool>, int)> VerifyClassIdsExistenceByTeacher(string teacherId, List<long> classIds);
        Task<(List<string>, int)> AddSubject (List<SubjectCreateUpdateDto> subjectCreateDtos);
        Task<int> UpdateSubject(long id, SubjectCreateUpdateDto subjectUpdateDto);
        Task<int> DeleteSubject (long id);
    }
}
