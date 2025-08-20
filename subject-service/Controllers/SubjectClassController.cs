using Microsoft.AspNetCore.Mvc;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;
using subject_service.Dtos.SubjectClass;
using subject_service.Common.Results;

namespace subject_service.Controllers
{
    [Route("subject/subject-class")]
    [ApiController]
    public class SubjectClassController : ControllerBase
    {
        private readonly ISubjectClassService _subjectClassService;

        public SubjectClassController(ISubjectClassService subjectClassService)
        {
            _subjectClassService = subjectClassService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _subjectClassService.GetAllSubjectClasses(page, pageSize, sortColumn, isDescending);
            
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
            var item = await _subjectClassService.GetAllExistingSubjectClasses(page, pageSize, sortColumn, isDescending);
            
            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(SubjectClassCreateUpdateDto subjectClassCreateDto)
        {
            var codeResult = await _subjectClassService.AddSubjectClass(subjectClassCreateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpPost("assign-subject-to-classes")]
        public async Task<IActionResult> AssignSubjectToClasses(long subjectId, List<long> classIds)
        {
            var item = await _subjectClassService.AssignSubjectToClasses(subjectId, classIds);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPut("edit")]
        public async Task<IActionResult> Edit(SubjectClassCreateUpdateDto subjectClassUpdateDto)
        {
            var codeResult = await _subjectClassService.UpdateSubjectClass(subjectClassUpdateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpDelete("delete/subjectId/{subjectId}/classId/{classId}")]
        public async Task<IActionResult> Delete(long subjectId, long classId)
        {
            var codeResult = await _subjectClassService.DeleteSubjectClass(subjectId, classId);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
