using subject_service.Common.Results;
using System.Text.Json;

namespace subject_service.Http
{
    public class HttpClientWrapper
    {
        private readonly HttpClient _httpClient;

        public HttpClientWrapper(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public async Task<(T? Result, int Code)> GetAsync<T>(string fullUrl, string? jsonPath = null)
        {
            int code = 1003;
            try
            {
                var response = await _httpClient.GetAsync(fullUrl);
                var content = await response.Content.ReadAsStringAsync();

                using var document = JsonDocument.Parse(content);
                var root = document.RootElement;

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

            }

            return (default, code);
        }
    }
}
