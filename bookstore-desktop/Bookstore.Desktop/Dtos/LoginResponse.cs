namespace Bookstore.Desktop.Dtos;

public class LoginResponse
{
    public string UserId { get; init; } = "";
    public string Status { get; init; } = "";
    public IReadOnlyList<string> Roles { get; init; } = Array.Empty<string>();
    public string AccessToken { get; init; } = "";
    public string RefreshToken { get; init; } = "";
}
