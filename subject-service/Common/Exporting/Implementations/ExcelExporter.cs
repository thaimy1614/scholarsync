using ClosedXML.Excel;
using DocumentFormat.OpenXml.Spreadsheet;
using subject_service.Common.Exporting.Interfaces;
using subject_service.Common.Exporting.Models;
using System.Data;

namespace subject_service.Common.Exporting.Implementations
{
    public class ExcelExporter : IExcelExporter
    {
        public void BuildSheet(
    IXLWorksheet worksheet,
    string mainTitle,
    List<(string Title, int Span)> groupHeaders,
    List<string> subHeaders,
    List<List<object>> rows
)
        {
            int currentRow = 1;
            int totalColumns = subHeaders.Count;
            double minColumnWidth = 14;
            double maxColumnWidth = 30;

            // 1. Main title (merge toàn bộ hàng đầu tiên)
            worksheet.Range(currentRow, 1, currentRow, totalColumns).Merge();
            worksheet.Cell(currentRow, 1).Value = mainTitle;
            worksheet.Cell(currentRow, 1).Style.Font.SetBold().Font.FontSize = 16;
            worksheet.Cell(currentRow, 1).Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
            worksheet.Cell(currentRow, 1).Style.Alignment.Vertical = XLAlignmentVerticalValues.Center;
            currentRow++;

            // 2. Group header
            int col = 1;

            foreach (var group in groupHeaders)
            {
                var hasEmptySubHeader = subHeaders.Skip(col - 1).Take(group.Span).All(string.IsNullOrWhiteSpace);
                var groupRange = hasEmptySubHeader
                    ? worksheet.Range(currentRow, col, currentRow + 1, col + group.Span - 1)
                    : worksheet.Range(currentRow, col, currentRow, col + group.Span - 1);

                groupRange.Merge();
                groupRange.Value = group.Title;
                groupRange.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
                groupRange.Style.Alignment.Vertical = XLAlignmentVerticalValues.Center;
                groupRange.Style.Font.SetBold();
                groupRange.Style.Alignment.WrapText = true;

                // AutoFit header column
                /*for (int i = 0; i < group.Span; i++)
                {
                    var column = worksheet.Column(col + i);
                    column.AdjustToContents();

                    if (column.Width < minColumnWidth)
                        column.Width = minColumnWidth;

                    column.Style.Alignment.WrapText = true;
                }*/

                // AutoFit group header column (chỉ fit 1 cột)
                worksheet.Column(col).AdjustToContents();
                //worksheet.Row(currentRow).AdjustToContents();

                if (group.Title.Contains("No."))
                {
                    worksheet.Column(col).Width = 10;
                }
                else if (group.Title.Contains("Full Name"))
                {
                    worksheet.Column(col).Width = 20;
                }
                else if (group.Title.Contains("Academic Performance"))
                {
                    worksheet.Column(col).Width = 12;
                }
                else if (group.Title.Contains("Comments on progress"))
                {
                    worksheet.Column(col).Width = 30;
                }
                else if (worksheet.Column(col).Width != 12 * group.Span)
                {
                    worksheet.Column(col).Width = 12 * group.Span;
                }

                double groupWidth = worksheet.Column(col).Width;
                double subWidth = groupWidth / group.Span;

                // Gán chiều rộng đều cho các subheader (nếu có)
                if (!hasEmptySubHeader || group.Span > 1)
                {
                    for (int i = 0; i < group.Span; i++)
                    {
                        worksheet.Column(col + i).Width = subWidth;
                        worksheet.Column(col + i).Style.Alignment.WrapText = true;
                    }
                }

                col += group.Span;
            } 

            /*foreach (var group in groupHeaders)
            {
                var groupRange = worksheet.Range(currentRow, col, currentRow, col + group.Span - 1);

                // Nếu không có subheader tương ứng (tức là subHeader trống), merge cả 2 dòng
                bool hasEmptySubHeader = subHeaders.Skip(col - 1).Take(group.Span).All(sh => string.IsNullOrWhiteSpace(sh));
                if (hasEmptySubHeader)
                {
                    groupRange = worksheet.Range(currentRow, col, currentRow + 1, col + group.Span - 1);
                }

                groupRange.Merge();
                worksheet.Cell(currentRow, col).Value = group.Title;
                groupRange.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
                groupRange.Style.Alignment.Vertical = XLAlignmentVerticalValues.Center;
                groupRange.Style.Font.SetBold();

                // AutoFit group header column (chỉ fit 1 cột)
                worksheet.Column(col).AdjustToContents();
                double groupWidth = worksheet.Column(col).Width;
                double subWidth = groupWidth / group.Span;

                // Gán chiều rộng đều cho các subheader (nếu có)
                if (!hasEmptySubHeader)
                {
                    for (int i = 0; i < group.Span; i++)
                    {
                        worksheet.Column(col + i).Width = subWidth;
                        worksheet.Column(col + i).Style.Alignment.WrapText = true;
                    }
                }

                col += group.Span;
            }*/

            // 3. Subheaders (dòng thứ 3 nếu có)
            /*currentRow++;
            for (int i = 0; i < subHeaders.Count; i++)
            {
                if (!string.IsNullOrWhiteSpace(subHeaders[i]))
                {
                    worksheet.Cell(currentRow, i + 1).Value = subHeaders[i];
                    worksheet.Cell(currentRow, i + 1).Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
                    worksheet.Cell(currentRow, i + 1).Style.Font.SetBold();
                }
            }*/
            currentRow++;
            for (int i = 0; i < subHeaders.Count; i++)
            {
                if (!string.IsNullOrWhiteSpace(subHeaders[i]))
                {
                    var cell = worksheet.Cell(currentRow, i + 1);
                    cell.Value = subHeaders[i];
                    cell.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
                    cell.Style.Alignment.Vertical = XLAlignmentVerticalValues.Center;
                    cell.Style.Font.SetBold();
                    cell.Style.Alignment.WrapText = true;

                    var colDef = worksheet.Column(i + 1);

                    //if(subHeaders[i] == "Note")
                    //{
                    //    colDef.Width = 30;
                    //} else
                    //{
                    //    colDef.Width = 12;
                    //}
                    //if (colDef.Width < minColumnWidth)
                    //    colDef.Width = minColumnWidth;
                }
            }


            // 4. Data rows
            int dataStartRow = currentRow + 1;
            for (int i = 0; i < rows.Count; i++)
            {
                for (int j = 0; j < rows[i].Count; j++)
                {
                    //worksheet.Cell(dataStartRow + i, j + 1).SetValue((XLCellValue)rows[i][j]);

                    var cell = worksheet.Cell(dataStartRow + i, j + 1);
                    var value = rows[i][j];

                    switch (value)
                    {
                        case null:
                            cell.SetValue(string.Empty);
                            break;
                        case string str:
                            cell.SetValue(str);
                            break;
                        case decimal d:
                            cell.SetValue(d);
                            break;
                        case double dbl:
                            cell.SetValue(dbl);
                            break;
                        case int n:
                            cell.SetValue(n);
                            break;
                        case DateTime dt:
                            cell.SetValue(dt);
                            break;
                        default:
                            cell.SetValue(value.ToString());
                            break;
                    }

                    cell.Style.Alignment.WrapText = true;
                }
            }

            // 5. Style toàn bộ vùng sử dụng
            var usedRange = worksheet.RangeUsed();
            if (usedRange != null)
            {
                usedRange.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
                usedRange.Style.Alignment.Vertical = XLAlignmentVerticalValues.Center;
                usedRange.Style.Border.OutsideBorder = XLBorderStyleValues.Thin;
                usedRange.Style.Border.InsideBorder = XLBorderStyleValues.Thin;


                //worksheet.Columns().AdjustToContents(); // Autofit mọi cột trước khi enforce MinWidth

                // Đảm bảo min width cho tất cả cột
                foreach (var row in worksheet.RowsUsed())
                {
                    //row.AdjustToContents();
                    //row.ClearHeight();
                    //if(row.Height < row.Worksheet.Value)
                    //{

                    //}
                    //if (column.Width > maxColumnWidth)
                    //{
                    //    column.Width = maxColumnWidth;
                    //    column.Style.Alignment.WrapText = true;
                    //    column.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Left;
                    //    column.Style.Alignment.Vertical = XLAlignmentVerticalValues.Top;
                    //}
                    //else if (column.Width < minColumnWidth)
                    //    column.Width = minColumnWidth;

                }

                //worksheet.RowsUsed().AdjustToContents();
                //worksheet.RowsUsed().Clear

                //worksheet.Rows().AdjustToContents();
            }
        }


        public async Task<ExportResult> ExportSheetAsync(
    string sheetName,
    string mainTitle,
    List<(string Title, int Span)> groupHeaders,
    List<string> subHeaders,
    List<List<object>> rows,
    string? fileNameBase = null)
        {
            using var workbook = new XLWorkbook();
            var worksheet = workbook.Worksheets.Add(sheetName);

            BuildSheet(worksheet, mainTitle, groupHeaders, subHeaders, rows);

            string fileName = string.IsNullOrWhiteSpace(fileNameBase)
                ? $"Sheet_{sheetName}_{DateTime.Now:yyyyMMdd_HHmmss}.xlsx"
                : $"{fileNameBase}_{DateTime.Now:yyyyMMdd_HHmmss}.xlsx";

            using var stream = new MemoryStream();
            workbook.SaveAs(stream);

            return new ExportResult
            {
                Content = stream.ToArray(),
                ContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                FileName = fileName
            };
        }

        public async Task<ExportResult> ExportMultipleSheetsAsync(
    List<ExcelSheetData> sheetDataList,
    string? fileNameBase = null)
        {
            using var workbook = new XLWorkbook();

            foreach (var sheetData in sheetDataList)
            {
                var worksheet = workbook.Worksheets.Add(sheetData.SheetName);
                BuildSheet(worksheet, sheetData.MainTitle, sheetData.GroupHeaders, sheetData.SubHeaders, sheetData.Rows);
            }

            string fileName = string.IsNullOrWhiteSpace(fileNameBase)
                ? $"MultiSheet_{DateTime.Now:yyyyMMdd_HHmmss}.xlsx"
                : $"{fileNameBase}_{DateTime.Now:yyyyMMdd_HHmmss}.xlsx";

            using var stream = new MemoryStream();
            workbook.SaveAs(stream);

            return new ExportResult
            {
                Content = stream.ToArray(),
                ContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                FileName = fileName
            };
        }
    }
}
