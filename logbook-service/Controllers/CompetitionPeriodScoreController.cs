using Microsoft.AspNetCore.Mvc;
using logbook_service.HttpApi;
using logbook_service.Models.Dtos;
using logbook_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;

namespace logbook_service.Controllers
{
    [Route("logbook/competition-period-score")]
    [ApiController]
    public class CompetitionPeriodScoreController : ControllerBase
    {
        private readonly ICompetitionPeriodScoreService _competitionPeriodScoreService;

        public CompetitionPeriodScoreController(ICompetitionPeriodScoreService competitionPeriodScoreService)
        {
            _competitionPeriodScoreService = competitionPeriodScoreService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _competitionPeriodScoreService.GetAllCompetitionPeriodScores(page, pageSize, sortColumn, isDescending);

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
            var item = await _competitionPeriodScoreService.GetAllExistingCompetitionPeriodScores(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(CompetitionPeriodScoreCreateUpdateDto competitionPeriodScoreCreateDto)
        {
            var codeResult = await _competitionPeriodScoreService.AddCompetitionPeriodScore(competitionPeriodScoreCreateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, CompetitionPeriodScoreCreateUpdateDto competitionPeriodScoreUpdateDto)
        {
            var codeResult = await _competitionPeriodScoreService.UpdateCompetitionPeriodScore(id, competitionPeriodScoreUpdateDto);

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

            var codeResult = await _competitionPeriodScoreService.DeleteCompetitionPeriodScore(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
