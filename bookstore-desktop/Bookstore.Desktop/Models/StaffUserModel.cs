namespace Bookstore.Desktop.Models;

public class StaffUserModel
{
    public string Id { get; init; } = "";
    public string Username { get; init; } = "";
    public string? Email { get; init; }
    public string? PhoneNumber { get; init; }
    public string Status { get; init; } = "";
    public bool Locked { get; init; }
    public IReadOnlyList<string> Roles { get; init; } = Array.Empty<string>();

    public string DisplayName => string.IsNullOrWhiteSpace(Username) ? Email ?? "Nhân viên" : Username;
    public bool CanUsePos => Roles.Any(role => role.Equals("ADMIN", StringComparison.OrdinalIgnoreCase)
        || role.Equals("STAFF", StringComparison.OrdinalIgnoreCase));
}
