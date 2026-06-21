namespace Bookstore.Desktop.Dtos;

public class PasswordResetTokenResponse
{
    public string ResetToken { get; init; } = "";
    public DateTimeOffset ExpiresAt { get; init; }
}
