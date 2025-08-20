namespace subject_service.Common.Exporting.Models
{
    public class ExcelSheetData
    {
        public string SheetName { get; set; } = string.Empty;
        public string MainTitle { get; set; } = string.Empty;
        public List<(string Title, int Span)> GroupHeaders { get; set; } = new();
        public List<string> SubHeaders { get; set; } = new();
        public List<List<object>> Rows { get; set; } = new();
        public string? FileNameBase { get; set; } = null;
    }
}
