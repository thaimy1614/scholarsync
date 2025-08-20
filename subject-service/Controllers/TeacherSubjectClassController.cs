using AutoMapper;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using subject_service.HttpApi;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;
using Microsoft.IdentityModel.Tokens;
using subject_service.Dtos.TeacherSubjectClass;
using subject_service.Common.Results;

namespace subject_service.Controllers
{
    [Route("subject/teacher-subject-class")]
    [ApiController]
    public class TeacherSubjectClassController : ControllerBase
    {
        private readonly ITeacherSubjectClassService _teacherSubjectClassService;

        public TeacherSubjectClassController(ITeacherSubjectClassService teacherSubjectClassService)
        {
            _teacherSubjectClassService = teacherSubjectClassService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _teacherSubjectClassService.GetAllTeacherSubjectClasses(page, pageSize, sortColumn, isDescending);
            
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
            var item = await _teacherSubjectClassService.GetAllExistingTeacherSubjectClasses(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }
        
        [HttpGet("get-all-teacher-subject-classes-detail-by/classId/{classId}")]
        public async Task<IActionResult> GetAllTeacherSubjectClassDetailByClassId(long classId)
        {
            var item = await _teacherSubjectClassService.GetAllTeacherSubjectClassDetailByClassId(classId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpGet("check-teacher-subject-class-exists/teacherId/{teacherId}/subjectId/{subjectId}/classId/{classId}")]
        public async Task<IActionResult> CheckTeacherSubjectClassExists(string teacherId, long subjectId, long classId)
        {
            var item = await _teacherSubjectClassService.CheckTeacherSubjectClassExists(teacherId, subjectId, classId);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(List<TeacherSubjectClassCreateUpdateDto> teacherSubjectClassCreateDtos)
        {
            var item = await _teacherSubjectClassService.AddTeacherSubjectClass(teacherSubjectClassCreateDtos);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("assign-teacher-to-subject")]
        public async Task<IActionResult> AssignTeacherToSubject([FromBody] List<TeacherSubjectClassCreateUpdateDto> teacherSubjectClassCreateDtos)
        {
            if (teacherSubjectClassCreateDtos.IsNullOrEmpty())
            {
                var errorResponse = new ApiResponse<TeacherSubjectClassDto>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var item = await _teacherSubjectClassService.AddTeacherSubjectClass(teacherSubjectClassCreateDtos);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPut("edit")]
        public async Task<IActionResult> Edit(TeacherSubjectClassCreateUpdateDto teacherSubjectClassUpdateDto)
        {
            if (teacherSubjectClassUpdateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _teacherSubjectClassService.UpdateTeacherSubjectClass(teacherSubjectClassUpdateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpDelete("delete")]
        public async Task<IActionResult> Delete(string teacherId, long subjectId, long classId)
        {
            //if (id == null)
            //{
            //    var errorResponse = new ApiResponse<object>
            //    {
            //        Code = 1003,
            //        Result = null
            //    };
            //    return Ok(errorResponse);
            //}

            var codeResult = await _teacherSubjectClassService.DeleteTeacherSubjectClass(teacherId, subjectId, classId);
            //int code = codeResult;

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
