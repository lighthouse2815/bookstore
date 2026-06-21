namespace Bookstore.Desktop.Dtos;

public class VerifyOtpRequest
{
    public string Email { get; init; } = "";
    public string OtpCode { get; init; } = "";
}
