using subject_service.Common.Pagination;
using subject_service.Dtos;
using subject_service.Dtos.Mark;

namespace subject_service.Services.Interfaces
{
    public interface IMarkService
    {
        Task<(PagedResult<MarkWithDeletedDto>, int)> GetAllMarks(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<MarkDto>, int)> GetAllExistingMarks(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(List<StudentSubjectScoreDto>, int)> GetAllStudentScoresByClassIdSemesterIdSubjectId(long classId, long schoolYearId,long semesterId, long subjectId);
        Task<(List<StudentSubjectScoreDto>, int)> GetAllStudentScoresByClassIdSemesterId(long classId, long schoolYearId, long semesterId);
        //Task<(List<string>, int)> GetAllIdsRoleStudentById(List<string> studentIds);
        //Task<(List<UserServiceStudentDto>, int)> GetAllStudentsById(List<string> studentIds);
        Task<(MarkDto, int)> GetMarkById(long id);
        Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryByClassIdSemesterId(long classId, long? semesterId);
        Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryBySchoolYearIdSemesterId(long schoolYearId, long? semesterId);
        Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryByClassIdSchoolYearIdSemesterId(long? classId, long? schoolYearId, long? semesterId);
        Task<(StudentYearlyResultDto, int)> GetStudentAnnualAcademicResults(string studentId, long schoolYearId, long classId);
        Task<(MarkDto, int)> GetMarkBySubjectIdStudentId(long subjectId, string studentId);
        Task<int> GenerateMarksForAllSubjectsInClass(
            long classId,
            long totalSemester,
            long schoolYearId,
            List<string> studentIds);
        Task<(int, List<string>)> AddMark(List<MarkCreateUpdateDto> markCreateDtos);
        Task<int> UpdateMark(long id, MarkCreateUpdateDto markUpdateDto);
        Task<int> UpdateConductStudent(long markId, string conduct);
        Task<int> DeleteMark(long id);
        Task<(MarkDto, int)> UpdateScore(long id);
    }
}
