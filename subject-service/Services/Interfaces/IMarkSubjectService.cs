using subject_service.Common.Pagination;
using subject_service.Dtos.MarkSubject;

namespace subject_service.Services.Interfaces
{
    public interface IMarkSubjectService
    {
        Task<(PagedResult<MarkSubjectWithDeletedDto>, int)> GetAllSubjectMarks(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<MarkSubjectDto>, int)> GetAllExistingSubjectMarks(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(MarkSubjectDto, int)> GetSubjectMarkById(long id);
        Task<int> AddSubjectMark(MarkSubjectCreateUpdateDto SubjectMarkCreateDto);
        Task<int> UpdateSubjectMark(long id, MarkSubjectCreateUpdateDto SubjectMarkUpdateDto);
        Task<int> DeleteSubjectMark(long id);
    }
}