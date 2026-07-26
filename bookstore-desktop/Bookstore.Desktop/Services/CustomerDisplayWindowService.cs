using System.Windows;
using Bookstore.Desktop.ViewModels;
using Bookstore.Desktop.Views;

namespace Bookstore.Desktop.Services;

public sealed class CustomerDisplayWindowService
{
    private readonly CustomerDisplayViewModel viewModel;
    private CustomerDisplayWindow? window;

    public CustomerDisplayWindowService(CustomerDisplayViewModel viewModel)
    {
        this.viewModel = viewModel;
    }

    public void Show(bool activate = false)
    {
        if (window != null)
        {
            if (window.WindowState == WindowState.Minimized)
            {
                window.WindowState = WindowState.Normal;
            }

            if (activate)
            {
                window.Activate();
            }
            return;
        }

        window = new CustomerDisplayWindow
        {
            DataContext = viewModel
        };
        window.Closed += (_, _) => window = null;
        window.Show();

        if (activate)
        {
            window.Activate();
        }
    }

    public void CloseAndReset()
    {
        var currentWindow = window;
        window = null;
        currentWindow?.Close();
        viewModel.Reset();
    }
}
