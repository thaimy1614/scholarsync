namespace subject_service.Dtos.Subject
{
    public partial class SubjectCreateUpdateDto
    {
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
    }
}
