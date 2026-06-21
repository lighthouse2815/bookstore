namespace Bookstore.Desktop.Config;

public static class AppConfig
{
    public const string DefaultApiBaseUrl = "http://localhost:8080";

    public static string DefaultGoogleClientId =>
        Environment.GetEnvironmentVariable("GOOGLE_CLIENT_ID")
        ?? Environment.GetEnvironmentVariable("VITE_GOOGLE_CLIENT_ID")
        ?? "";
}
