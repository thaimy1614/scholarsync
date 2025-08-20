using System.ComponentModel;
using System.Data;

namespace subject_service.Common.Exporting
{
    public static class DataTableBuilder
    {
        public static DataTable BuildFromList<T>(IEnumerable<T> data)
        {
            var dataTable = new DataTable(typeof(T).Name);

            var properties = TypeDescriptor.GetProperties(typeof(T))
                                           .Cast<PropertyDescriptor>()
                                           .Where(p => p.PropertyType.Namespace == "System") // loại navigation property
                                           .ToList();

            foreach (var prop in properties)
            {
                dataTable.Columns.Add(prop.Name, Nullable.GetUnderlyingType(prop.PropertyType) ?? prop.PropertyType);
            }

            foreach (var item in data)
            {
                var row = dataTable.NewRow();
                foreach (var prop in properties)
                {
                    row[prop.Name] = prop.GetValue(item) ?? DBNull.Value;
                }
                dataTable.Rows.Add(row);
            }

            return dataTable;
        }
    }
}
