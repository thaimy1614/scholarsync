using Microsoft.AspNetCore.Mvc;
using logbook_service.HttpApi;
using logbook_service.Models.Dtos;
using logbook_service.Services.Interfaces;
using System.ComponentModel.DataAnnotations;

namespace logbook_service.Controllers
{
    [Route("logbook/competition-daily-record")]
    [ApiController]
    public class CompetitionDailyRecordController : ControllerBase
    {
        private readonly ICompetitionDailyRecordService _competitionDailyRecordService;

        public CompetitionDailyRecordController(ICompetitionDailyRecordService competitionDailyRecordService)
        {
            _competitionDailyRecordService = competitionDailyRecordService;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll([FromQuery][Required] int page, [FromQuery][Required] int pageSize, [FromQuery] string sortColumn = "", [FromQuery] bool isDescending = false)
        {
            var item = await _competitionDailyRecordService.GetAllCompetitionDailyRecords(page, pageSize, sortColumn, isDescending);

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
            var item = await _competitionDailyRecordService.GetAllExistingCompetitionDailyRecords(page, pageSize, sortColumn, isDescending);

            var response = new ApiResponse<object>
            {
                Code = item.Item2,
                Result = item.Item1
            };
            return Ok(response);
        }

        [HttpPost("add")]
        public async Task<IActionResult> Add(CompetitionDailyRecordCreateUpdateDto competitionDailyRecordCreateDto)
        {
            var codeResult = await _competitionDailyRecordService.AddCompetitionDailyRecord(competitionDailyRecordCreateDto);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }

        [HttpPut("edit/{id}")]
        public async Task<IActionResult> Edit(long id, CompetitionDailyRecordCreateUpdateDto competitionDailyRecordUpdateDto)
        {
            var codeResult = await _competitionDailyRecordService.UpdateCompetitionDailyRecord(id, competitionDailyRecordUpdateDto);

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

            var codeResult = await _competitionDailyRecordService.DeleteCompetitionDailyRecord(id);

            var response = new ApiResponse<object>
            {
                Code = codeResult,
                Result = null
            };
            return Ok(response);
        }
    }
}
