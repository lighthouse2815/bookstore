using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace Bookstore.Desktop.Services;

public class GoogleOAuthService
{
    private static readonly HttpClient HttpClient = new();

    public async Task<string> GetIdTokenAsync(string clientId, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(clientId))
        {
            throw new InvalidOperationException("Chua cau hinh Google Client ID.");
        }

        var state = CreateBase64UrlToken(32);
        var codeVerifier = CreateBase64UrlToken(32);
        var codeChallenge = CreateCodeChallenge(codeVerifier);

        using var listener = new TcpListener(IPAddress.Loopback, 0);
        listener.Start();

        var port = ((IPEndPoint)listener.LocalEndpoint).Port;
        var redirectUri = $"http://127.0.0.1:{port}/";
        var authorizationUrl = BuildAuthorizationUrl(clientId.Trim(), redirectUri, state, codeChallenge);

        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
        timeout.CancelAfter(TimeSpan.FromMinutes(3));

        OpenBrowser(authorizationUrl);

        using var tcpClient = await listener.AcceptTcpClientAsync(timeout.Token);
        await using var stream = tcpClient.GetStream();
        using var reader = new StreamReader(stream, Encoding.UTF8, leaveOpen: true);

        var requestLine = await reader.ReadLineAsync().WaitAsync(timeout.Token);
        if (string.IsNullOrWhiteSpace(requestLine))
        {
            await WriteBrowserResponseAsync(stream, false, "Khong nhan duoc phan hoi Google.", timeout.Token);
            throw new InvalidOperationException("Khong nhan duoc phan hoi Google.");
        }

        await DrainHeadersAsync(reader, timeout.Token);

        var query = ParseQueryFromRequestLine(requestLine, redirectUri);
        var isValidState = query.TryGetValue("state", out var receivedState)
            && string.Equals(receivedState, state, StringComparison.Ordinal);
        if (!isValidState)
        {
            await WriteBrowserResponseAsync(stream, false, "Trang thai xac thuc Google khong hop le.", timeout.Token);
            throw new InvalidOperationException("Trang thai xac thuc Google khong hop le.");
        }

        if (query.TryGetValue("error", out var oauthError))
        {
            await WriteBrowserResponseAsync(stream, false, "Dang nhap Google bi huy hoac that bai.", timeout.Token);
            throw new InvalidOperationException($"Dang nhap Google that bai: {oauthError}");
        }

        if (!query.TryGetValue("code", out var code) || string.IsNullOrWhiteSpace(code))
        {
            await WriteBrowserResponseAsync(stream, false, "Google khong tra ma xac thuc.", timeout.Token);
            throw new InvalidOperationException("Google khong tra ma xac thuc.");
        }

        await WriteBrowserResponseAsync(stream, true, "Da nhan xac thuc Google. Ban co the dong tab nay.", timeout.Token);

        return await ExchangeCodeForIdTokenAsync(clientId.Trim(), redirectUri, code, codeVerifier, timeout.Token);
    }

    private static string BuildAuthorizationUrl(string clientId, string redirectUri, string state, string codeChallenge)
    {
        var values = new Dictionary<string, string>
        {
            ["client_id"] = clientId,
            ["redirect_uri"] = redirectUri,
            ["response_type"] = "code",
            ["scope"] = "openid email profile",
            ["state"] = state,
            ["code_challenge"] = codeChallenge,
            ["code_challenge_method"] = "S256",
            ["prompt"] = "select_account"
        };

        return "https://accounts.google.com/o/oauth2/v2/auth?" + ToQueryString(values);
    }

    private static async Task<string> ExchangeCodeForIdTokenAsync(
        string clientId,
        string redirectUri,
        string code,
        string codeVerifier,
        CancellationToken cancellationToken)
    {
        using var content = new FormUrlEncodedContent(new Dictionary<string, string>
        {
            ["client_id"] = clientId,
            ["code"] = code,
            ["code_verifier"] = codeVerifier,
            ["grant_type"] = "authorization_code",
            ["redirect_uri"] = redirectUri
        });

        using var response = await HttpClient.PostAsync("https://oauth2.googleapis.com/token", content, cancellationToken);
        var body = await response.Content.ReadAsStringAsync(cancellationToken);
        if (!response.IsSuccessStatusCode)
        {
            throw new InvalidOperationException("Khong doi duoc Google authorization code thanh ID token: " + ExtractGoogleError(body));
        }

        using var document = JsonDocument.Parse(body);
        if (!document.RootElement.TryGetProperty("id_token", out var idTokenElement))
        {
            throw new InvalidOperationException("Google khong tra ID token.");
        }

        return idTokenElement.GetString()
            ?? throw new InvalidOperationException("Google tra ID token rong.");
    }

    private static void OpenBrowser(string authorizationUrl)
    {
        Process.Start(new ProcessStartInfo
        {
            FileName = authorizationUrl,
            UseShellExecute = true
        });
    }

    private static async Task DrainHeadersAsync(StreamReader reader, CancellationToken cancellationToken)
    {
        while (true)
        {
            var line = await reader.ReadLineAsync().WaitAsync(cancellationToken);
            if (string.IsNullOrEmpty(line))
            {
                return;
            }
        }
    }

    private static Dictionary<string, string> ParseQueryFromRequestLine(string requestLine, string redirectUri)
    {
        var parts = requestLine.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        if (parts.Length < 2)
        {
            throw new InvalidOperationException("Request callback Google khong hop le.");
        }

        var uri = new Uri(new Uri(redirectUri), parts[1]);
        return ParseQuery(uri.Query);
    }

    private static Dictionary<string, string> ParseQuery(string query)
    {
        var result = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var pair in query.TrimStart('?').Split('&', StringSplitOptions.RemoveEmptyEntries))
        {
            var separatorIndex = pair.IndexOf('=');
            var rawKey = separatorIndex >= 0 ? pair[..separatorIndex] : pair;
            var rawValue = separatorIndex >= 0 ? pair[(separatorIndex + 1)..] : "";
            result[DecodeQueryValue(rawKey)] = DecodeQueryValue(rawValue);
        }

        return result;
    }

    private static string DecodeQueryValue(string value)
    {
        return Uri.UnescapeDataString(value.Replace("+", " "));
    }

    private static async Task WriteBrowserResponseAsync(Stream stream, bool success, string message, CancellationToken cancellationToken)
    {
        var color = success ? "#2563EB" : "#DC2626";
        var html = $"""
            <!doctype html>
            <html lang="vi">
            <head><meta charset="utf-8"><title>Bookstore POS</title></head>
            <body style="font-family:Segoe UI,Arial,sans-serif;padding:40px;color:#172554">
                <h2 style="color:{color}">Bookstore POS</h2>
                <p>{WebUtility.HtmlEncode(message)}</p>
            </body>
            </html>
            """;
        var body = Encoding.UTF8.GetBytes(html);
        var header = Encoding.ASCII.GetBytes(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=utf-8\r\n" +
            $"Content-Length: {body.Length}\r\n" +
            "Connection: close\r\n\r\n");

        await stream.WriteAsync(header, cancellationToken);
        await stream.WriteAsync(body, cancellationToken);
        await stream.FlushAsync(cancellationToken);
    }

    private static string ExtractGoogleError(string body)
    {
        if (string.IsNullOrWhiteSpace(body))
        {
            return "empty response";
        }

        try
        {
            using var document = JsonDocument.Parse(body);
            if (document.RootElement.TryGetProperty("error_description", out var description))
            {
                return description.GetString() ?? body;
            }

            if (document.RootElement.TryGetProperty("error", out var error))
            {
                return error.GetString() ?? body;
            }
        }
        catch (JsonException)
        {
            return body;
        }

        return body;
    }

    private static string CreateCodeChallenge(string codeVerifier)
    {
        var hash = SHA256.HashData(Encoding.ASCII.GetBytes(codeVerifier));
        return ToBase64Url(hash);
    }

    private static string CreateBase64UrlToken(int byteLength)
    {
        var bytes = RandomNumberGenerator.GetBytes(byteLength);
        return ToBase64Url(bytes);
    }

    private static string ToBase64Url(byte[] bytes)
    {
        return Convert.ToBase64String(bytes)
            .TrimEnd('=')
            .Replace('+', '-')
            .Replace('/', '_');
    }

    private static string ToQueryString(Dictionary<string, string> values)
    {
        return string.Join("&", values.Select(pair =>
            $"{Uri.EscapeDataString(pair.Key)}={Uri.EscapeDataString(pair.Value)}"));
    }
}
