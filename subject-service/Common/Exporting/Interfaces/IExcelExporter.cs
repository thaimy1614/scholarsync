using ClosedXML.Excel;
using subject_service.Common.Exporting.Models;
using System.Data;

namespace subject_service.Common.Exporting.Interfaces
{
    public interface IExcelExporter
    {
        void BuildSheet(
    IXLWorksheet worksheet,
    string mainTitle,
    List<(string Title, int Span)> groupHeaders,
    List<string> subHeaders,
    List<List<object>> rows
    );

        Task<ExportResult> ExportSheetAsync(
    string sheetName,
    string mainTitle,
    List<(string Title, int Span)> groupHeaders,
    List<string> subHeaders,
    List<List<object>> rows,
    string? fileNameBase = null);

        Task<ExportResult> ExportMultipleSheetsAsync(
    List<ExcelSheetData> sheetDataList,
    string? fileNameBase = null);
    }
}
