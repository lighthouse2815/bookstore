using System.Text.Json;
using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Stores;

namespace Bookstore.Desktop.Services;

public class AuthService
{
    private readonly ApiClient apiClient;
    private readonly AuthStore authStore;

    public AuthService(ApiClient apiClient, AuthStore authStore)
    {
        this.apiClient = apiClient;
        this.authStore = authStore;
    }

    public async Task LoginAsync(string email, string password)
    {
        var sessionElement = await apiClient.PostAsync("/api/auth/login", new LoginRequest
        {
            Username = email,
            Password = password
        });

        var session = sessionElement.Deserialize<LoginResponse>(JsonHelper.Options)
            ?? throw new InvalidOperationException("Backend khong tra thong tin dang nhap.");

        await ApplySessionAsync(session);
    }

    public async Task LoginWithGoogleAsync(string idToken)
    {
        var sessionElement = await apiClient.PostAsync("/api/auth/google", new GoogleLoginRequest
        {
            IdToken = idToken
        });

        var session = sessionElement.Deserialize<LoginResponse>(JsonHelper.Options)
            ?? throw new InvalidOperationException("Backend khong tra thong tin dang nhap Google.");

        await ApplySessionAsync(session);
    }

    public Task RequestPasswordResetOtpAsync(string email)
    {
        return apiClient.PostAsync("/api/auth/forgot-password/request-otp", new RequestPasswordResetOtpRequest
        {
            Email = email
        });
    }

    public async Task<string> VerifyPasswordResetOtpAsync(string email, string otpCode)
    {
        var element = await apiClient.PostAsync("/api/auth/forgot-password/verify-otp", new VerifyOtpRequest
        {
            Email = email,
            OtpCode = otpCode
        });

        var response = element.Deserialize<PasswordResetTokenResponse>(JsonHelper.Options)
            ?? throw new InvalidOperationException("Backend khong tra reset token.");

        return response.ResetToken;
    }

    public Task ResetPasswordAsync(string resetToken, string newPassword)
    {
        return apiClient.PostAsync("/api/auth/forgot-password/reset", new ResetPasswordRequest
        {
            ResetToken = resetToken,
            NewPassword = newPassword
        });
    }

    public Task LogoutAsync()
    {
        authStore.Clear();
        return Task.CompletedTask;
    }

    private async Task ApplySessionAsync(LoginResponse session)
    {
        authStore.AccessToken = session.AccessToken;
        authStore.RefreshToken = session.RefreshToken;

        var user = await GetCurrentUserAsync(session.Roles);
        if (!user.CanUsePos)
        {
            authStore.Clear();
            throw new InvalidOperationException("Tai khoan nay khong co quyen dung app ban hang tai quay.");
        }

        if (!user.Status.Equals("ACTIVE", StringComparison.OrdinalIgnoreCase) || user.Locked)
        {
            authStore.Clear();
            throw new InvalidOperationException("Tai khoan chua active hoac dang bi khoa.");
        }

        authStore.SetSession(session.AccessToken, session.RefreshToken, user);
    }

    private async Task<StaffUserModel> GetCurrentUserAsync(IReadOnlyList<string> fallbackRoles)
    {
        var element = await apiClient.GetAsync("/api/users/me");
        var roles = JsonHelper.GetStringList(element, "roles", "roleNames");
        if (roles.Count == 0)
        {
            roles = fallbackRoles;
        }

        return new StaffUserModel
        {
            Id = JsonHelper.GetString(element, "userId", "id") ?? "",
            Username = JsonHelper.GetString(element, "username", "name") ?? "",
            Email = JsonHelper.GetString(element, "email"),
            PhoneNumber = JsonHelper.GetString(element, "phoneNumber", "phone"),
            Status = JsonHelper.GetString(element, "status") ?? "",
            Locked = bool.TryParse(JsonHelper.GetString(element, "locked"), out var locked) && locked,
            Roles = roles
        };
    }
}
