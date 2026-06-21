using System.Windows;
using System.Windows.Media;
using Bookstore.Desktop.ViewModels;

namespace Bookstore.Desktop.Views;

public partial class ForgotPasswordWindow : Window
{
    private readonly LoginViewModel loginViewModel;
    private string? resetToken;

    public ForgotPasswordWindow(LoginViewModel loginViewModel)
    {
        this.loginViewModel = loginViewModel;
        InitializeComponent();
    }

    private async void SendOtpButton_OnClick(object sender, RoutedEventArgs e)
    {
        var email = EmailTextBox.Text.Trim();
        if (string.IsNullOrWhiteSpace(email))
        {
            ShowStatus("Vui lòng nhập email.", true);
            return;
        }

        await RunAsync(async () =>
        {
            resetToken = null;
            ResetPanel.Visibility = Visibility.Collapsed;
            ResetPanel.IsEnabled = false;
            await loginViewModel.RequestPasswordResetOtpAsync(email);
            OtpPanel.IsEnabled = true;
            ShowStatus("Nếu email tồn tại, OTP đã được gửi. Vui lòng kiểm tra email.", false);
        });
    }

    private async void VerifyOtpButton_OnClick(object sender, RoutedEventArgs e)
    {
        var email = EmailTextBox.Text.Trim();
        var otpCode = OtpTextBox.Text.Trim();
        if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(otpCode))
        {
            ShowStatus("Vui lòng nhập email và OTP.", true);
            return;
        }

        await RunAsync(async () =>
        {
            resetToken = await loginViewModel.VerifyPasswordResetOtpAsync(email, otpCode);
            ResetPanel.Visibility = Visibility.Visible;
            ResetPanel.IsEnabled = true;
            ShowStatus("OTP hợp lệ. Hãy nhập mật khẩu mới.", false);
        });
    }

    private async void ResetPasswordButton_OnClick(object sender, RoutedEventArgs e)
    {
        if (string.IsNullOrWhiteSpace(resetToken))
        {
            ShowStatus("Chưa xác thực OTP.", true);
            return;
        }

        var newPassword = NewPasswordBox.Password;
        var confirmPassword = ConfirmPasswordBox.Password;
        if (newPassword.Length < 8)
        {
            ShowStatus("Mật khẩu mới phải có ít nhất 8 ký tự.", true);
            return;
        }

        if (!string.Equals(newPassword, confirmPassword, StringComparison.Ordinal))
        {
            ShowStatus("Mật khẩu nhập lại không khớp.", true);
            return;
        }

        await RunAsync(async () =>
        {
            await loginViewModel.ResetPasswordAsync(resetToken, newPassword);
            ShowStatus("Đã đặt lại mật khẩu. Bạn có thể đăng nhập bằng mật khẩu mới.", false);
            ResetPasswordButton.IsEnabled = false;
        });
    }

    private async Task RunAsync(Func<Task> action)
    {
        SetBusy(true);
        try
        {
            await action();
        }
        catch (Exception exception)
        {
            ShowStatus(exception.Message, true);
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void SetBusy(bool isBusy)
    {
        SendOtpButton.IsEnabled = !isBusy;
        VerifyOtpButton.IsEnabled = !isBusy && OtpPanel.IsEnabled;
        ResetPasswordButton.IsEnabled = !isBusy && ResetPanel.IsEnabled;
    }

    private void ShowStatus(string message, bool isError)
    {
        StatusBorder.Visibility = Visibility.Visible;
        StatusBorder.Background = CreateBrush(isError ? "#FEF2F2" : "#EFF6FF");
        StatusBorder.BorderBrush = CreateBrush(isError ? "#FECACA" : "#BFDBFE");
        StatusIcon.Text = isError ? "\uE783" : "\uE946";
        StatusIcon.Foreground = CreateBrush(isError ? "#DC2626" : "#2563EB");
        StatusTextBlock.Text = message;
        StatusTextBlock.Foreground = CreateBrush(isError ? "#991B1B" : "#1D4ED8");
    }

    private static Brush CreateBrush(string color)
    {
        return (Brush)new BrushConverter().ConvertFromString(color)!;
    }
}
