namespace subject_service.Dtos.Subject
{
    public class SubjectWithDeletedDto
    {
        public long Id { get; set; }

        public string Name { get; set; }

        public int WeeklySlots { get; set; }

        public int MaxSlotsPerSession { get; set; }

        public bool PreferConsecutive { get; set; }

        public bool IsMainSubject { get; set; }

        public bool IsScoreable { get; set; }

        public bool IsEvaluateWithScore { get; set; }

        public int? SpecialSlot { get; set; }

        public long? SpecialRoom { get; set; }

        public long SchoolYearId { get; set; }

        public bool IsDeleted { get; set; }
    }
}
