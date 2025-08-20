using AutoMapper;
using DotNetEnv;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using subject_service.Services.Interfaces;
using Swashbuckle.AspNetCore.SwaggerGen;
using System.ComponentModel.DataAnnotations;
using Microsoft.IdentityModel.Tokens;
using subject_service.Dtos.Subject;
using subject_service.Common.Results;

namespace subject_service.Controllers
{
    [Route("subject")]
    [ApiController]
    public class SubjectController : ControllerBase
    {
        private readonly ISubjectService _subjectService;
        private readonly ITeacherSubjectClassService _teacherSubjectClassService;
        private readonly ISubjectClassService _subjectClassService;

        public SubjectController(ISubjectService subjectService, ITeacherSubjectClassService teacherSubjectClassService, ISubjectClassService subjectClassService)
        {
            _subjectService = subjectService;
            _teacherSubjectClassService = teacherSubjectClassService;
            _subjectClassService  = subjectClassService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _subjectService.GetAllSubjects(page, pageSize, sortColumn, isDescending);

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
            var item = await _subjectService.GetAllExistingSubjects(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-all-subjects-by/teacherId/{teacherId}")]
        public async Task<IActionResult> GetAllSubjectsByTeacherId(string teacherId)
        {
            var item = await _subjectService.GetAllSubjectsByTeacherId(teacherId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        
        [HttpGet("get-all-teachers-by/subjectId/{subjectId}")]
        public async Task<IActionResult> GetAllTeachersBySubjectId(long subjectId)
        {
            var item = await _teacherSubjectClassService.GetAllTeachersBySubjectId(subjectId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpGet("get-subject-by/subjectId/{subjectId}")]
        public async Task<IActionResult> GetSubjectById(long subjectId)
        {
            var item = await _subjectService.GetSubjectById(subjectId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-subject-name-by/subjectId/{subjectId}")]
        public async Task<IActionResult> GetSubjectNameById(long subjectId)
        {
            var item = await _subjectService.GetSubjectNameById(subjectId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-all-subjects-by/subjectIds")]
        public async Task<IActionResult> GetAllSubjectsByIds([FromQuery] List<long> ids)
        {
            var item = await _subjectService.GetAllSubjectsByIds(ids);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("search-subjects/{keyword}")]
        public async Task<IActionResult> SearchSubjects(string? keyword, [FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false, [FromQuery]bool includeDeleted = false)
        {
            var item = await _subjectService.SearchSubjects(keyword, page, pageSize, sortColumn, isDescending, includeDeleted);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> AddSubject(List<SubjectCreateUpdateDto> subjectCreateDtos)
        {
            if (subjectCreateDtos == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var item = await _subjectService.AddSubject(subjectCreateDtos);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        //[HttpPost("assign-subject-to-classes")]
        //public async Task<IActionResult> AssignSubjectToClasses(long subjectId, List<long> classIds)
        //{
        //    var item = await _subjectClassService.AssignSubjectToClasses(subjectId, classIds);

        //    var response = new ApiResponse<object>
        //    {
        //        Code = item.Item2,
        //        Result = item.Item1
        //    };
        //    return Ok(response);
        //}
        
        //[HttpPost("assign-teacher-to-subject")]
        //public async Task<IActionResult> AssignTeacherToSubject([FromBody] List<TeacherSubjectClassCreateUpdateDto> teacherSubjectClassCreateDtos)
        //{
        //    if (teacherSubjectClassCreateDtos.IsNullOrEmpty())
        //    {
        //        var errorResponse = new ApiResponse<TeacherSubjectClassDto>
        //        {
        //            Code = 1003,
        //            Result = null
        //        };
        //        return Ok(errorResponse);
        //    }

        //    var item = await _teacherSubjectClassService.AddTeacherSubjectClass(teacherSubjectClassCreateDtos);

        //    var response = new ApiResponse<object>
        //    {
        //        Code = item.Item2,
        //        Result = item.Item1
        //    };
        //    return Ok(response);
        //}

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, SubjectCreateUpdateDto subjectUpdateDto)
        {
            if (subjectUpdateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _subjectService.UpdateSubject(id, subjectUpdateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = subjectUpdateDto
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

            var codeResult = await _subjectService.DeleteSubject(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
