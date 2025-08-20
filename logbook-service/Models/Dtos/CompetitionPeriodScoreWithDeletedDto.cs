namespace logbook_service.Models.Dtos
{
    public class CompetitionPeriodScoreWithDeletedDto
    {
        public long id { get; set; }

        public long classId { get; set; }

        public string teacherId { get; set; }

        public DateTime recordDate { get; set; }

        public int periodNumber { get; set; }

        public string scoreGrade { get; set; }

        public int scoreValue { get; set; }

        public string note { get; set; }

        public bool isDeleted { get; set; } = false;
    }
}
