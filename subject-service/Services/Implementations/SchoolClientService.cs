using subject_service.Common.Results;
using subject_service.Dtos.Class;
using subject_service.Dtos.Student;
using subject_service.Services.Interfaces;
using System.Net.Http;
using System.Text.Json;

namespace subject_service.Services.Implementations
{
    public class SchoolClientService : ISchoolClientService
    {
        private readonly HttpClient _httpClient;

        public SchoolClientService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public async Task<(ClassDto, int)> GetClassNameByClassId(long classId)
        {
            string _apiSchoolUrl = Environment.GetEnvironmentVariable("SCHOOL_SERVICE");
            int code = 1003;
            var classDto = new ClassDto();
            if (!string.IsNullOrEmpty(_apiSchoolUrl))
            {
                try
                {
                    //string queryString = string.Join("&", teacherIds.Select(id => $"userIds={id}"));
                    var schoolResponse = await _httpClient.GetAsync($"{_apiSchoolUrl}/school/class/get-class-by-id/{classId}");
                    var schoolContent = await schoolResponse.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<ClassDto>>(schoolContent, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (schoolResponse != null && schoolContent != null && apiResponse != null)
                    {
                        code = apiResponse.Code;
                        classDto = apiResponse.Result;
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (classDto, code);
        }
        
        public async Task<(List<StudentDto>, int)> GetListStudentByClassId(long classId)
        {
            string _apiSchoolUrl = Environment.GetEnvironmentVariable("SCHOOL_SERVICE");
            int code = 1003;
            var classDto = new List<StudentDto>();
            if (!string.IsNullOrEmpty(_apiSchoolUrl))
            {
                try
                {
                    var schoolResponse = await _httpClient.GetAsync($"{_apiSchoolUrl}/school/class/get-list-student-by-class-id/{classId}");
                    var schoolContent = await schoolResponse.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<StudentDto>>>(schoolContent, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (schoolResponse != null && schoolContent != null && apiResponse != null)
                    {
                        code = apiResponse.Code;
                        classDto = apiResponse.Result;
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (classDto, code);
        }
    }
}
