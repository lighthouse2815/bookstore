using System.Windows;
using System.Windows.Controls;
using Bookstore.Desktop.ViewModels;

namespace Bookstore.Desktop.Views;

public partial class LoginView : UserControl
{
    public LoginView()
    {
        InitializeComponent();
        Loaded += LoginView_OnLoaded;
    }

    private void LoginView_OnLoaded(object sender, RoutedEventArgs e)
    {
        if (DataContext is LoginViewModel viewModel
            && !string.Equals(PasswordInput.Password, viewModel.Password, StringComparison.Ordinal))
        {
            PasswordInput.Password = viewModel.Password;
        }
    }

    private void PasswordInput_OnPasswordChanged(object sender, RoutedEventArgs e)
    {
        if (DataContext is LoginViewModel viewModel && sender is PasswordBox passwordBox)
        {
            viewModel.Password = passwordBox.Password;
        }
    }

    private void ForgotPasswordButton_OnClick(object sender, RoutedEventArgs e)
    {
        if (DataContext is not LoginViewModel viewModel)
        {
            return;
        }

        var window = new ForgotPasswordWindow(viewModel)
        {
            Owner = Window.GetWindow(this)
        };
        window.ShowDialog();
    }
}
