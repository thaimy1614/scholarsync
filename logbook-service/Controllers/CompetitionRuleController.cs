using Microsoft.AspNetCore.Mvc;
using logbook_service.HttpApi;
using logbook_service.Models.Dtos;
using logbook_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;

namespace logbook_service.Controllers
{
    [Route("logbook/competition-rule")]
    [ApiController]
    public class CompetitionRuleController : ControllerBase
    {
        private readonly ICompetitionRuleService _competitionRuleService;

        public CompetitionRuleController(ICompetitionRuleService competitionRuleService)
        {
            _competitionRuleService = competitionRuleService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _competitionRuleService.GetAllCompetitionRules(page, pageSize, sortColumn, isDescending);

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
            var item = await _competitionRuleService.GetAllExistingCompetitionRules(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(CompetitionRuleCreateUpdateDto competitionRuleCreateDto)
        {
            var codeResult = await _competitionRuleService.AddCompetitionRule(competitionRuleCreateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, CompetitionRuleCreateUpdateDto competitionRuleUpdateDto)
        {
            var codeResult = await _competitionRuleService.UpdateCompetitionRule(id, competitionRuleUpdateDto);

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

            var codeResult = await _competitionRuleService.DeleteCompetitionRule(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
