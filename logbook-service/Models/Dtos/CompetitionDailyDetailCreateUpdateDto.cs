namespace logbook_service.Models.Dtos
{
    public class CompetitionDailyDetailCreateUpdateDto
    {
        public long competitionDailyRecordId { get; set; }
        public long competitionRuleId { get; set; }
        public int score { get; set; }
        public string note { get; set; }
    }
}