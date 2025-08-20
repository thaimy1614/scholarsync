namespace logbook_service.Models.Dtos
{
    public class CompetitionDailyDetailDto
    {
        public long competitionDailyRecordId { get; set; }
        public long competitionRuleId { get; set; }
        public int score { get; set; }
        public string note { get; set; }
    }
}