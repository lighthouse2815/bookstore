namespace Bookstore.Desktop.Config;

public static class AppConfig
{
    private const string FallbackApiBaseUrl = "http://localhost:8080";

    public static string DefaultApiBaseUrl
    {
        get
        {
            var configuredUrl = DotEnvConfiguration.GetFirst(
                "BOOKSTORE_API_BASE_URL",
                "API_BASE_URL",
                "VITE_API_BASE_URL");

            if (!string.IsNullOrWhiteSpace(configuredUrl))
            {
                return NormalizeApiBaseUrl(configuredUrl);
            }

            var serverPort = DotEnvConfiguration.GetFirst("SERVER_PORT");
            return int.TryParse(serverPort, out var port) && port is > 0 and <= 65535
                ? $"http://localhost:{port}"
                : FallbackApiBaseUrl;
        }
    }

    public static string DefaultGoogleClientId =>
        DotEnvConfiguration.GetFirst("GOOGLE_CLIENT_ID", "VITE_GOOGLE_CLIENT_ID") ?? "";

    public static bool IsDevelopment =>
        string.Equals(
            DotEnvConfiguration.GetFirst(
                "BOOKSTORE_ENVIRONMENT",
                "DOTNET_ENVIRONMENT",
                "ASPNETCORE_ENVIRONMENT"),
            "Development",
            StringComparison.OrdinalIgnoreCase);

    public static string DevelopmentUsername => IsDevelopment
        ? DotEnvConfiguration.GetFirst("BOOKSTORE_DEV_USERNAME") ?? ""
        : "";

    public static string DevelopmentPassword => IsDevelopment
        ? DotEnvConfiguration.GetFirst("BOOKSTORE_DEV_PASSWORD") ?? ""
        : "";

    internal static string NormalizeApiBaseUrl(string value)
    {
        var normalized = value.Trim().TrimEnd('/');
        return normalized.EndsWith("/api", StringComparison.OrdinalIgnoreCase)
            ? normalized[..^4]
            : normalized;
    }
}
