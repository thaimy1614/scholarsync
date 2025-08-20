using AutoMapper;
using Microsoft.AspNetCore.Mvc;
using subject_service.Models;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;
using subject_service.Dtos.MarkType;
using subject_service.Common.Results;

namespace subject_service.Controllers
{
    [Route("subject/mark-type")]
    [ApiController]
    public class MarkTypeController : ControllerBase
    {
        private readonly IMarkTypeService _markTypeService;
        //private readonly IMapper _mapper;

        public MarkTypeController(IMarkTypeService markTypeService)
        {
            _markTypeService = markTypeService;
            //_mapper = mapper;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _markTypeService.GetAllMarkTypes(page, pageSize, sortColumn, isDescending);
            //var pagedMarkTypeDtos = _mapper.Map<PagedResult<MarkTypeDto>>(item.Item1);

            //int code = item.Item2;
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
            var item = await _markTypeService.GetAllExistingMarkTypes(page, pageSize, sortColumn, isDescending);
            //var pagedMarkTypeDtos = _mapper.Map<PagedResult<MarkTypeDto>>(item.Item1);

            //int code = item.Item2;
            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(MarkTypeCreateUpdateDto markTypeCreateDto)
        {
            if (markTypeCreateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            //var markType = _mapper.Map<MarkType>(markTypeCreateDto);

            var codeResult = await _markTypeService.AddMarkType(markTypeCreateDto);
            //int code = codeResult;

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = markTypeCreateDto
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, MarkTypeCreateUpdateDto markTypeUpdateDto)
        {
            if (markTypeUpdateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            //var markType = _mapper.Map<MarkType>(markTypeUpdateDto);

            var codeResult = await _markTypeService.UpdateMarkType(id, markTypeUpdateDto);
            //int code = codeResult;

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = markTypeUpdateDto
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

            var codeResult = await _markTypeService.DeleteMarkType(id);
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
