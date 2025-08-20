namespace logbook_service.Models.Dtos
{
    public class CompetitionRuleWithDeletedDto
    {
        public long id { get; set; }

        public string name { get; set; }

        public string type { get; set; }  

        public int score { get; set; }

        public string description { get; set; }

        public bool isDeleted { get; set; } = false;
    }
}
