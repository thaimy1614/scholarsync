namespace logbook_service.Models.Dtos
{
    public class CompetitionDailyRecordCreateUpdateDto
    {
        public long classId { get; set; }
        public string teacherId { get; set; }
        public DateTime recordDate { get; set; }
        public int totalScore { get; set; }
        public string note { get; set; }
    }
}