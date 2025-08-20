namespace logbook_service.Models.Dtos
{
    public class CompetitionDailyDetailWithDeletedDto
    {
        public long id { get; set; }

        public long competitionDailyRecordId { get; set; }

        public long competitionRuleId { get; set; }

        public string? studentId { get; set; } 

        public int score { get; set; }

        public string note { get; set; }

        public bool isDeleted { get; set; } = false;
    }
}
