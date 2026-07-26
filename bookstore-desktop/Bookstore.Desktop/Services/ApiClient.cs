using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Stores;

namespace Bookstore.Desktop.Services;

public class ApiClient
{
    private readonly HttpClient httpClient;
    private readonly SemaphoreSlim refreshLock = new(1, 1);
    private readonly AppSettingsStore settingsStore;
    private readonly AuthStore authStore;

    public ApiClient(
        AppSettingsStore settingsStore,
        AuthStore authStore,
        HttpMessageHandler? messageHandler = null)
    {
        this.settingsStore = settingsStore;
        this.authStore = authStore;
        httpClient = messageHandler == null ? new HttpClient() : new HttpClient(messageHandler);
    }

    public Task<JsonElement> GetAsync(string path)
    {
        return SendAsync(HttpMethod.Get, path, null);
    }

    public async Task<ApiGetResponse> GetWithHeadersAsync(string path)
    {
        var response = await SendForResponseAsync(HttpMethod.Get, path, null);
        return new ApiGetResponse(ParseBody(response.Body), response.Headers);
    }

    public Task<JsonElement> PostAsync<T>(string path, T payload)
    {
        var json = JsonSerializer.Serialize(payload, JsonHelper.Options);
        return SendAsync(HttpMethod.Post, path, json);
    }

    private async Task<JsonElement> SendAsync(HttpMethod method, string path, string? json)
    {
        var response = await SendForResponseAsync(method, path, json);
        return ParseBody(response.Body);
    }

    private async Task<ApiHttpResponse> SendForResponseAsync(HttpMethod method, string path, string? json)
    {
        var accessTokenUsed = authStore.AccessToken;
        var response = await SendOnceAsync(method, path, json, includeAccessToken: true);

        if (response.StatusCode == HttpStatusCode.Unauthorized
            && ShouldTryRefresh(path)
            && await RefreshAccessTokenAsync(accessTokenUsed))
        {
            response = await SendOnceAsync(method, path, json, includeAccessToken: true);
        }

        if (!response.IsSuccessStatusCode)
        {
            throw new ApiClientException(CreateErrorMessage(response.StatusCode, response.Body), response.StatusCode);
        }

        return response;
    }

    private static JsonElement ParseBody(string body)
    {
        if (string.IsNullOrWhiteSpace(body))
        {
            return default;
        }

        using var document = JsonDocument.Parse(body);
        return Unwrap(document.RootElement.Clone());
    }

    private async Task<ApiHttpResponse> SendOnceAsync(
        HttpMethod method,
        string path,
        string? json,
        bool includeAccessToken)
    {
        using var request = new HttpRequestMessage(method, BuildUri(path));
        if (includeAccessToken && !string.IsNullOrWhiteSpace(authStore.AccessToken))
        {
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", authStore.AccessToken);
        }

        if (json != null)
        {
            request.Content = new StringContent(json, Encoding.UTF8, "application/json");
        }

        using var response = await httpClient.SendAsync(request);
        var body = await response.Content.ReadAsStringAsync();
        var headers = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        foreach (var header in response.Headers)
        {
            headers[header.Key] = string.Join(",", header.Value);
        }
        foreach (var header in response.Content.Headers)
        {
            headers[header.Key] = string.Join(",", header.Value);
        }

        return new ApiHttpResponse(response.IsSuccessStatusCode, response.StatusCode, body, headers);
    }

    private async Task<bool> RefreshAccessTokenAsync(string? accessTokenUsed)
    {
        await refreshLock.WaitAsync();
        try
        {
            if (!string.Equals(authStore.AccessToken, accessTokenUsed, StringComparison.Ordinal)
                && !string.IsNullOrWhiteSpace(authStore.AccessToken))
            {
                return true;
            }

            var refreshToken = authStore.RefreshToken;
            var currentUser = authStore.CurrentUser;
            if (string.IsNullOrWhiteSpace(refreshToken) || currentUser == null)
            {
                authStore.Clear();
                return false;
            }

            var json = JsonSerializer.Serialize(new { refreshToken }, JsonHelper.Options);
            var response = await SendOnceAsync(HttpMethod.Post, "/api/auth/refresh", json, includeAccessToken: false);
            if (!response.IsSuccessStatusCode || string.IsNullOrWhiteSpace(response.Body))
            {
                authStore.Clear();
                return false;
            }

            using var document = JsonDocument.Parse(response.Body);
            var session = Unwrap(document.RootElement.Clone()).Deserialize<LoginResponse>(JsonHelper.Options);
            if (session == null
                || string.IsNullOrWhiteSpace(session.AccessToken)
                || string.IsNullOrWhiteSpace(session.RefreshToken))
            {
                authStore.Clear();
                return false;
            }

            authStore.SetSession(session.AccessToken, session.RefreshToken, currentUser);
            return true;
        }
        catch (HttpRequestException)
        {
            return false;
        }
        catch (JsonException)
        {
            authStore.Clear();
            return false;
        }
        finally
        {
            refreshLock.Release();
        }
    }

    private static bool ShouldTryRefresh(string path)
    {
        return !path.StartsWith("/api/auth/", StringComparison.OrdinalIgnoreCase);
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

    private sealed record ApiHttpResponse(
        bool IsSuccessStatusCode,
        HttpStatusCode StatusCode,
        string Body,
        IReadOnlyDictionary<string, string> Headers);
}

public sealed record ApiGetResponse(
    JsonElement Data,
    IReadOnlyDictionary<string, string> Headers);

public class ApiClientException : Exception
{
    public ApiClientException(string message, HttpStatusCode statusCode) : base(message)
    {
        StatusCode = statusCode;
    }

    public HttpStatusCode StatusCode { get; }
}
