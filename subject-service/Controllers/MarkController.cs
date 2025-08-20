using Microsoft.AspNetCore.Mvc;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;
using subject_service.Services.Implementations;
using subject_service.Dtos.Mark;
using subject_service.Common.Results;
using subject_service.Common.Exporting.Interfaces;

namespace subject_service.Controllers
{
    [Route("subject/mark")]
    [ApiController]
    public class MarkController : ControllerBase
    {
        private readonly IMarkService _markService;
        private readonly IMarkExporter _markExporter;

        public MarkController(IMarkService markService, IMarkExporter markExporter)
        {
            _markService = markService;
            _markExporter = markExporter;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _markService.GetAllMarks(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-all-exist")]
        public async Task<IActionResult> GetAllExist([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _markService.GetAllExistingMarks(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpGet("get-academic-performance-summary-by/classId/{classId}")]
        public async Task<IActionResult> GetAcademicPerformanceSummaryByClassIdSemesterId(long classId,[FromQuery] long? semesterId)
        {
            var item = await _markService.GetAcademicPerformanceSummaryByClassIdSemesterId(classId, semesterId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-academic-performance-summary-by/schoolYearId/{schoolYearId}")]
        public async Task<IActionResult> GetAcademicPerformanceSummaryBySchoolYearIdSemesterId(long schoolYearId,[FromQuery] long? semesterId)
        {
            var item = await _markService.GetAcademicPerformanceSummaryBySchoolYearIdSemesterId(schoolYearId, semesterId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-academic-performance-summary")]
        public async Task<IActionResult> GetAcademicPerformanceSummary([FromQuery] long? classId, [FromQuery] long? schoolYearId,[FromQuery] long? semesterId)
        {
            var item = await _markService.GetAcademicPerformanceSummaryByClassIdSchoolYearIdSemesterId(classId, schoolYearId, semesterId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-mark-by/subjectId/{subjectId}/studentId/{studentId}")]
        public async Task<IActionResult> GetMarkBySubjectIdStudentId(long subjectId, string studentId)
        {
            var item = await _markService.GetMarkBySubjectIdStudentId(subjectId, studentId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpGet("get-all-student-scores-by/classId/{classId}/schoolYearId/{schoolYearId}/semesterId/{semesterId}/subjectId/{subjectId}")]
        public async Task<IActionResult> GetAllStudentScoresByClassIdSemesterIdSubjectId(long classId, long schoolYearId, long semesterId, long subjectId)
        {
            var item = await _markService.GetAllStudentScoresByClassIdSemesterIdSubjectId(classId, schoolYearId, semesterId, subjectId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-all-student-scores-by/classId/{classId}/schoolYearId/{schoolYearId}/semesterId/{semesterId}")]
        public async Task<IActionResult> GetAllStudentScoresByClassIdSemesterId(long classId, long schoolYearId, long semesterId)
        {
            var item = await _markService.GetAllStudentScoresByClassIdSemesterId(classId, schoolYearId, semesterId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("student-annual-academic-result/studentId/{studentId}/classId/{classId}/schoolYearId/{schoolYearId}")]
        public async Task<IActionResult> GetStudentAnnualAcademicResult(string studentId, long schoolYearId, long classId)
        {
            var item = await _markService.GetStudentAnnualAcademicResults(studentId, schoolYearId, classId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpGet("export-mark-summary-by-semester")]
        public async Task<IActionResult> ExportMarkSummaryBySemester(
        [FromQuery] [Required] long classId,
        [FromQuery] [Required] long semesterId)
        {
            var result = await _markExporter.ExportMarkSummaryBySemesterAsync(
                classId, semesterId, "", 1
            );

            var response = new ApiResponse<object>
            {
                Code = result.Item2,
                Result = result.Item1
            };
            return Ok(response);
            //return File(result.Item1.Content, result.Item1.ContentType, result.Item1.FileName);
        }
        
        [HttpGet("export-mark-summary-by-semester/demo")]
        public async Task<IActionResult> ExportMarkSummaryBySemesterDemo(
        [FromQuery] [Required] long classId,
        [FromQuery] [Required] long semesterId)
        {
            var result = await _markExporter.ExportMarkSummaryBySemesterAsync(
                classId, semesterId, "", 1
            );

            return File(result.Item1.Content, result.Item1.ContentType, result.Item1.FileName);
        }
        
        [HttpGet("export-subject-score-by-semester")]
        public async Task<IActionResult> ExportSubjectScoreBySemester(
        [FromQuery] [Required] long subjectId,
        [FromQuery] [Required] long classId,
        [FromQuery] [Required] long semesterId)
        {
            var result = await _markExporter.ExportSubjectScoreBySemesterAsync(
                subjectId, classId, semesterId, "", 1
            );

            var response = new ApiResponse<object>
            {
                Code = result.Item2,
                Result = result.Item1
            };
            return Ok(response);
            //return File(result.Item1.Content, result.Item1.ContentType, result.Item1.FileName);
        }
        
        [HttpGet("export-subject-score-by-semester/demo")]
        public async Task<IActionResult> ExportSubjectScoreBySemesterDemo(
        [FromQuery] [Required] long subjectId,
        [FromQuery] [Required] long classId,
        [FromQuery] [Required] long semesterId)
        {
            var result = await _markExporter.ExportSubjectScoreBySemesterAsync(
                subjectId, classId, semesterId, "", 1
            );

            return File(result.Item1.Content, result.Item1.ContentType, result.Item1.FileName);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add([FromBody] List<MarkCreateUpdateDto> markCreateDtos)
        {
            var item = await _markService.AddMark(markCreateDtos);

            var response = new ApiResponse<object>
            {
                Code = item.Item1,
                Result = item.Item2
            };
            return Ok(response);
        }
        
        [HttpPost("generate-marks-for-all-subjects-in-class")]
        public async Task<IActionResult> GenerateMarksForAllSubjectsInClass(long classId, int totalSemester, long schoolYearId, List<string>studentIds)
        {
            var item = await _markService.GenerateMarksForAllSubjectsInClass(classId, totalSemester, schoolYearId, studentIds);

            var response = new ApiResponse<object>
            {
                Code = item,
                Result = null
            };
            return Ok(response);
        }

        [HttpPost("update-score")]
        public async Task<IActionResult> UpdateScore(long markId)
        {
            var item = await _markService.UpdateScore(markId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPut("update-conduct-student/markId/{markId}")]
        public async Task<IActionResult> UpdateConductStudent(long markId, string conduct)
        {
            var codeResult = await _markService.UpdateConductStudent(markId, conduct);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
        
        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, MarkCreateUpdateDto markUpdateDto)
        {
            var codeResult = await _markService.UpdateMark(id, markUpdateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpDelete("delete/{id}")]
        public async Task<IActionResult> Delete(long id)
        {
            var codeResult = await _markService.DeleteMark(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
