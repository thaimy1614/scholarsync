namespace logbook_service.Models.Dtos
{
    public class CompetitionDailyRecordWithDeletedDto
    {
        public long id { get; set; }

        public long classId { get; set; }

        public string teacherId { get; set; }

        public DateTime recordDate { get; set; }

        public int totalScore { get; set; }

        public string note { get; set; }

        public bool isDeleted { get; set; } = false;
    }
}
