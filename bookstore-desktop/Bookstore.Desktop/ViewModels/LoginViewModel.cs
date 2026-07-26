using Bookstore.Desktop.Config;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class LoginViewModel : ObservableObject
{
    private readonly AuthService authService;
    private readonly GoogleOAuthService googleOAuthService;
    private readonly AppSettingsStore settingsStore;

    public LoginViewModel(AuthService authService, GoogleOAuthService googleOAuthService, AppSettingsStore settingsStore)
    {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
        this.settingsStore = settingsStore;
        email = AppConfig.DevelopmentUsername;
        password = AppConfig.DevelopmentPassword;
        settingsStore.PropertyChanged += (_, args) =>
        {
            if (args.PropertyName == nameof(AppSettingsStore.IsDarkMode))
            {
                OnPropertyChanged(nameof(IsDarkMode));
                OnPropertyChanged(nameof(ThemeButtonText));
            }
        };
    }

    public event EventHandler? LoginSucceeded;
    public bool IsDarkMode => settingsStore.IsDarkMode;
    public string ThemeButtonText => IsDarkMode ? "☀  Chế độ sáng" : "☾  Chế độ tối";

    [ObservableProperty]
    [NotifyCanExecuteChangedFor(nameof(LoginCommand))]
    private string email = "";

    [ObservableProperty]
    [NotifyCanExecuteChangedFor(nameof(LoginCommand))]
    private string password = "";

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    [NotifyCanExecuteChangedFor(nameof(LoginCommand))]
    [NotifyCanExecuteChangedFor(nameof(GoogleLoginCommand))]
    private bool isLoading;

    private bool CanLogin => !IsLoading
        && !string.IsNullOrWhiteSpace(Email)
        && !string.IsNullOrWhiteSpace(Password);

    private bool CanGoogleLogin => !IsLoading;

    [RelayCommand]
    private void ToggleTheme()
    {
        settingsStore.UpdateTheme(!settingsStore.IsDarkMode);
    }

    [RelayCommand(CanExecute = nameof(CanLogin))]
    private async Task LoginAsync()
    {
        try
        {
            IsLoading = true;
            Message = "";
            await authService.LoginAsync(Email, Password);
            Password = AppConfig.DevelopmentPassword;
            LoginSucceeded?.Invoke(this, EventArgs.Empty);
        }
        catch (Exception exception)
        {
            Message = exception.Message;
        }
        finally
        {
            IsLoading = false;
        }
    }

    [RelayCommand(CanExecute = nameof(CanGoogleLogin))]
    private async Task GoogleLoginAsync()
    {
        try
        {
            IsLoading = true;
            Message = "";

            if (string.IsNullOrWhiteSpace(settingsStore.GoogleClientId))
            {
                Message = "Chua cau hinh Google Client ID. Vao Cai dat de nhap OAuth Client ID cho desktop.";
                return;
            }

            var idToken = await googleOAuthService.GetIdTokenAsync(settingsStore.GoogleClientId);
            await authService.LoginWithGoogleAsync(idToken);
            LoginSucceeded?.Invoke(this, EventArgs.Empty);
        }
        catch (Exception exception)
        {
            Message = exception.Message;
        }
        finally
        {
            IsLoading = false;
        }
    }

    public Task RequestPasswordResetOtpAsync(string email)
    {
        return authService.RequestPasswordResetOtpAsync(email);
    }

    public Task<string> VerifyPasswordResetOtpAsync(string email, string otpCode)
    {
        return authService.VerifyPasswordResetOtpAsync(email, otpCode);
    }

    public Task ResetPasswordAsync(string resetToken, string newPassword)
    {
        return authService.ResetPasswordAsync(resetToken, newPassword);
    }
}
