using AutoMapper;
using subject_service.Common.Results;
using subject_service.Dtos.Student;
using subject_service.Dtos.Teacher;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;
using System.Net.Http;
using System.Text.Json;

namespace subject_service.Services.Implementations
{
    public class UserClientService : IUserClientService
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiUserUrl;

        public UserClientService(HttpClient httpClient)
        {
            _httpClient = httpClient;
            _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE") ?? String.Empty;
        }

        public async Task<(List<TeacherDto>, int)> GetAllTeachersById(List<string> teacherIds)
        {
            //string _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            var teacherDtos = new List<TeacherDto>();
            int code = 1003;
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    code = 1003;
                    string queryString = string.Join("&", teacherIds.Select(s => $"ids={s}"));
                    var userApiGetUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
                    var userApiResponse = await userApiGetUserByIds.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<TeacherDto>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse != null && userApiGetUserByIds.IsSuccessStatusCode && apiResponse != null)
                    {
                        teacherDtos = apiResponse.Result;
                        code = apiResponse.Code;
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (teacherDtos, code);
        }

        public async Task<(List<string>, int)> GetAllIdsRoleTeacherById(List<string> teacherIds)
        {
            //string _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            int code = 1003;
            List<string> idsRoleTeacher = new();
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    string queryString = string.Join("&", teacherIds.Select(id => $"userIds={id}"));
                    var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/teacher?{queryString}");
                    var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse != null && userApiCheckRole.IsSuccessStatusCode && apiResponse != null)
                    {
                        code = apiResponse.Code;
                        if (code == 1000)
                        {
                            foreach (var item in apiResponse.Result)
                            {
                                if (item.Value)
                                {
                                    idsRoleTeacher.Add(item.Key);
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (idsRoleTeacher, code);
        }

        public async Task<(List<string>, int)> GetAllIdsRoleStudentById(List<string> studentIds)
        {
            //string _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            int code = 1003;
            List<string> idsRoleStudent = new();
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    string queryString = string.Join("&", studentIds.Select(id => $"userIds={id}"));
                    var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/student?{queryString}");
                    var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse != null && userApiCheckRole.IsSuccessStatusCode && apiResponse != null)
                    {
                        code = apiResponse.Code;
                        if (code == 1000)
                        {
                            foreach (var item in apiResponse.Result)
                            {
                                if (item.Value)
                                {
                                    idsRoleStudent.Add(item.Key);
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (idsRoleStudent, code);
        }

        public async Task<(List<StudentDto>, int)> GetAllStudentsById(List<string> studentIds)
        {
            //string _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            var studentDtos = new List<StudentDto>();
            int code = 1003;
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    code = 1003;
                    string queryString = string.Join("&", studentIds.Select(s => $"ids={s}"));
                    var userApiGetUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
                    var userApiResponse = await userApiGetUserByIds.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<StudentDto>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse != null && userApiGetUserByIds.IsSuccessStatusCode && apiResponse != null)
                    {
                        studentDtos = apiResponse.Result;
                        code = apiResponse.Code;
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            return (studentDtos, code);
        }

        private async Task<(T? Result, int Code)> GetAsync<T>(string path)
        {
            int code = 1003;
            try
            {
                var response = await _httpClient.GetAsync($"{_apiUserUrl}{path}");
                var content = await response.Content.ReadAsStringAsync();

                var apiResponse = JsonSerializer.Deserialize<ApiResponse<T>>(content, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });

                if (response.IsSuccessStatusCode && apiResponse != null)
                {
                    code = apiResponse.Code;
                    return (apiResponse.Result, code);
                }
            }
            catch (Exception ex)
            {
                code = 1003;
            }

            return (default, code);
        }
    }
}
