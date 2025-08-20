namespace subject_service.Common.Exporting
{
    public class ExportResult
    {
        public byte[] Content { get; set; } = Array.Empty<byte>();

        public string ContentType { get; set; } = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        public string FileName { get; set; } = $"Export_{DateTime.UtcNow:yyyyMMddHHmmss}.xlsx";
    }
}
