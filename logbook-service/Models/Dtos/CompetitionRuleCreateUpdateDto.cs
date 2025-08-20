namespace logbook_service.Models.Dtos
{
    public class CompetitionRuleCreateUpdateDto
    {
        public string name { get; set; }
        public string type { get; set; }
        public int score { get; set; }
        public string description { get; set; }
    }
}