using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Stores;

namespace Bookstore.Desktop.Services;

public class ApiClient
{
    private readonly HttpClient httpClient = new();
    private readonly AppSettingsStore settingsStore;
    private readonly AuthStore authStore;

    public ApiClient(AppSettingsStore settingsStore, AuthStore authStore)
    {
        this.settingsStore = settingsStore;
        this.authStore = authStore;
    }

    public Task<JsonElement> GetAsync(string path)
    {
        return SendAsync(HttpMethod.Get, path, null);
    }

    public async Task<JsonElement> PostAsync<T>(string path, T payload)
    {
        var json = JsonSerializer.Serialize(payload, JsonHelper.Options);
        using var content = new StringContent(json, Encoding.UTF8, "application/json");
        return await SendAsync(HttpMethod.Post, path, content);
    }

    private async Task<JsonElement> SendAsync(HttpMethod method, string path, HttpContent? content)
    {
        using var request = new HttpRequestMessage(method, BuildUri(path));
        if (!string.IsNullOrWhiteSpace(authStore.AccessToken))
        {
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", authStore.AccessToken);
        }

        if (content != null)
        {
            request.Content = content;
        }

        using var response = await httpClient.SendAsync(request);
        var body = await response.Content.ReadAsStringAsync();
        if (!response.IsSuccessStatusCode)
        {
            throw new ApiClientException(CreateErrorMessage(response.StatusCode, body), response.StatusCode);
        }

        if (string.IsNullOrWhiteSpace(body))
        {
            return default;
        }

        using var document = JsonDocument.Parse(body);
        var root = document.RootElement.Clone();
        return Unwrap(root);
    }

    private string BuildUri(string path)
    {
        var baseUrl = settingsStore.ApiBaseUrl.TrimEnd('/');
        var normalizedPath = path.StartsWith('/') ? path : "/" + path;
        return baseUrl + normalizedPath;
    }

    private static JsonElement Unwrap(JsonElement root)
    {
        if (root.ValueKind == JsonValueKind.Object && root.TryGetProperty("data", out var data))
        {
            return data.Clone();
        }

        return root;
    }

    private static string CreateErrorMessage(HttpStatusCode statusCode, string body)
    {
        var backendMessage = ExtractBackendMessage(body);
        if (!string.IsNullOrWhiteSpace(backendMessage))
        {
            return backendMessage!;
        }

        return statusCode switch
        {
            HttpStatusCode.BadRequest => "Yêu cầu không hợp lệ.",
            HttpStatusCode.Unauthorized => "Phiên đăng nhập đã hết hạn hoặc không hợp lệ.",
            HttpStatusCode.Forbidden => "Không có quyền thực hiện thao tác này.",
            HttpStatusCode.InternalServerError => "Lỗi server backend.",
            _ => $"Lỗi API: {(int)statusCode} {statusCode}"
        };
    }

    private static string? ExtractBackendMessage(string body)
    {
        if (string.IsNullOrWhiteSpace(body))
        {
            return null;
        }

        try
        {
            using var document = JsonDocument.Parse(body);
            var root = document.RootElement;
            return JsonHelper.GetString(root, "message", "error", "detail", "title");
        }
        catch (JsonException)
        {
            return body;
        }
    }
}

public class ApiClientException : Exception
{
    public ApiClientException(string message, HttpStatusCode statusCode) : base(message)
    {
        StatusCode = statusCode;
    }

    public HttpStatusCode StatusCode { get; }
}
