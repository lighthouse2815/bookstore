using Bookstore.Desktop.Config;

namespace Bookstore.Desktop.Tests;

[TestClass]
public class AppConfigTests
{
    [TestMethod]
    [DataRow("http://localhost:8080", "http://localhost:8080")]
    [DataRow("http://localhost:8080/", "http://localhost:8080")]
    [DataRow("http://localhost:8080/api", "http://localhost:8080")]
    [DataRow("https://api.example.com/API/", "https://api.example.com")]
    public void NormalizeApiBaseUrl_ReturnsBackendRoot(string input, string expected)
    {
        Assert.AreEqual(expected, AppConfig.NormalizeApiBaseUrl(input));
    }

    [TestMethod]
    public void ParseLines_ReadsSupportedDotEnvSyntax()
    {
        var values = DotEnvConfiguration.ParseLines(
        [
            "# comment",
            "BOOKSTORE_API_BASE_URL=http://localhost:8080/api",
            "GOOGLE_CLIENT_ID=\"desktop-client-id\"",
            "export VITE_GOOGLE_CLIENT_ID='website-client-id'",
            "INVALID LINE"
        ]);

        Assert.AreEqual("http://localhost:8080/api", values["BOOKSTORE_API_BASE_URL"]);
        Assert.AreEqual("desktop-client-id", values["GOOGLE_CLIENT_ID"]);
        Assert.AreEqual("website-client-id", values["VITE_GOOGLE_CLIENT_ID"]);
        Assert.IsFalse(values.ContainsKey("INVALID LINE"));
    }

    [TestMethod]
    [DoNotParallelize]
    public void DefaultValues_PreferProcessEnvironment()
    {
        var previousApiUrl = Environment.GetEnvironmentVariable("BOOKSTORE_API_BASE_URL");
        var previousGoogleClientId = Environment.GetEnvironmentVariable("GOOGLE_CLIENT_ID");

        try
        {
            Environment.SetEnvironmentVariable(
                "BOOKSTORE_API_BASE_URL",
                "https://desktop-api.example.com/api/");
            Environment.SetEnvironmentVariable(
                "GOOGLE_CLIENT_ID",
                "desktop-client-id");

            Assert.AreEqual("https://desktop-api.example.com", AppConfig.DefaultApiBaseUrl);
            Assert.AreEqual("desktop-client-id", AppConfig.DefaultGoogleClientId);
        }
        finally
        {
            Environment.SetEnvironmentVariable("BOOKSTORE_API_BASE_URL", previousApiUrl);
            Environment.SetEnvironmentVariable("GOOGLE_CLIENT_ID", previousGoogleClientId);
        }
    }

    [TestMethod]
    [DoNotParallelize]
    public void DevelopmentCredentials_AreDisabledOutsideDevelopment()
    {
        var previousEnvironment = Environment.GetEnvironmentVariable("BOOKSTORE_ENVIRONMENT");
        var previousUsername = Environment.GetEnvironmentVariable("BOOKSTORE_DEV_USERNAME");
        var previousPassword = Environment.GetEnvironmentVariable("BOOKSTORE_DEV_PASSWORD");

        try
        {
            Environment.SetEnvironmentVariable("BOOKSTORE_DEV_USERNAME", "dev-user");
            Environment.SetEnvironmentVariable("BOOKSTORE_DEV_PASSWORD", "dev-password");

            Environment.SetEnvironmentVariable("BOOKSTORE_ENVIRONMENT", "Production");
            Assert.AreEqual("", AppConfig.DevelopmentUsername);
            Assert.AreEqual("", AppConfig.DevelopmentPassword);

            Environment.SetEnvironmentVariable("BOOKSTORE_ENVIRONMENT", "Development");
            Assert.AreEqual("dev-user", AppConfig.DevelopmentUsername);
            Assert.AreEqual("dev-password", AppConfig.DevelopmentPassword);
        }
        finally
        {
            Environment.SetEnvironmentVariable("BOOKSTORE_ENVIRONMENT", previousEnvironment);
            Environment.SetEnvironmentVariable("BOOKSTORE_DEV_USERNAME", previousUsername);
            Environment.SetEnvironmentVariable("BOOKSTORE_DEV_PASSWORD", previousPassword);
        }
    }
}
