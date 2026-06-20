namespace Bookstore.Desktop.Dtos;

public class ResetPasswordRequest
{
    public string ResetToken { get; init; } = "";
    public string NewPassword { get; init; } = "";
}
