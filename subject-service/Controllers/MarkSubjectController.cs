using Microsoft.AspNetCore.Mvc;
using subject_service.Common.Results;
using subject_service.Dtos.MarkSubject;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;

namespace subject_service.Controllers
{
    [Route("subject/mark-subject")]
    [ApiController]
    public class MarkSubjectController : ControllerBase
    {
        private readonly IMarkSubjectService _subjectMarkService;

        public MarkSubjectController(IMarkSubjectService subjectMarkService)
        {
            _subjectMarkService = subjectMarkService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _subjectMarkService.GetAllSubjectMarks(page, pageSize, sortColumn, isDescending);

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
            var item = await _subjectMarkService.GetAllExistingSubjectMarks(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(MarkSubjectCreateUpdateDto subjectMarkCreateDto)
        {
            if (subjectMarkCreateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _subjectMarkService.AddSubjectMark(subjectMarkCreateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = subjectMarkCreateDto
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, MarkSubjectCreateUpdateDto subjectMarkUpdateDto)
        {
            if (subjectMarkUpdateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _subjectMarkService.UpdateSubjectMark(id, subjectMarkUpdateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = subjectMarkUpdateDto
            };
            return Ok(response);
        }

        [HttpDelete("delete/{id}")]
        public async Task<IActionResult> Delete(long id)
        {
            if (id == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _subjectMarkService.DeleteSubjectMark(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
