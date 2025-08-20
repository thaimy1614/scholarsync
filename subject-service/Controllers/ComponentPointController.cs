using AutoMapper;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using subject_service.HttpApi;
using subject_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Authorization;
using subject_service.Common.Results;
using subject_service.Dtos.ComponentPoint;

namespace subject_service.Controllers
{
    [Route("subject/component-point")]
    [ApiController]
    public class ComponentPointController : ControllerBase
    {
        private readonly IComponentPointService _componentPointService;
        private readonly IMapper _mapper;

        public ComponentPointController(IComponentPointService componentPointService, IMapper mapper)
        {
            _componentPointService = componentPointService;
            _mapper = mapper;
        }

        [HttpGet("get-all")]
        //[Authorize(Roles = "TEACHER")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _componentPointService.GetAllComponentPoints(page, pageSize, sortColumn, isDescending);

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
            var item = await _componentPointService.GetAllExistingComponentPoints(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(ComponentPointCreateUpdateDto componentPointCreateDto)
        {
            if (componentPointCreateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _componentPointService.AddComponentPoint(componentPointCreateDto);
            
            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, ComponentPointCreateUpdateDto componentPointUpdateDto)
        {
            if (componentPointUpdateDto == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _componentPointService.UpdateComponentPoint(id, componentPointUpdateDto);

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
            if (id == null)
            {
                var errorResponse = new ApiResponse<object>
                {
                    Code = 1003,
                    Result = null
                };
                return Ok(errorResponse);
            }

            var codeResult = await _componentPointService.DeleteComponentPoint(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
