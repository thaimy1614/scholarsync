namespace subject_service.Dtos.MarkType
{
    public class MarkTypeCreateUpdateDto
    {
        public string Name { get; set; }

        public int Weight { get; set; }

        public int TotalColumn { get; set; }

        public long SubjectId { get; set; }

        public long ClassId { get; set; }

    }
}
