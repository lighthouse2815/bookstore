using System.Windows;
using System.Windows.Threading;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using Bookstore.Desktop.ViewModels;

namespace Bookstore.Desktop;

public partial class App : Application
{
    public App()
    {
        DispatcherUnhandledException += OnDispatcherUnhandledException;
        AppDomain.CurrentDomain.UnhandledException += OnCurrentDomainUnhandledException;
    }

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        try
        {
            var settingsStore = new AppSettingsStore();
            var authStore = new AuthStore();
            var cartStore = new PosCartStore();
            var navigationService = new NavigationService();
            var apiClient = new ApiClient(settingsStore, authStore);
            var authService = new AuthService(apiClient, authStore);
            var googleOAuthService = new GoogleOAuthService();
            var bookService = new BookService(apiClient);
            var posService = new PosService(apiClient);
            var orderService = new OrderService(apiClient);
            var inventoryService = new InventoryService(bookService);
            var printerService = new ReceiptPrinterService();

            var receiptViewModel = new ReceiptPreviewViewModel(printerService);
            var posViewModel = new PosViewModel(bookService, posService, cartStore, navigationService, receiptViewModel, authStore);
            var orderLookupViewModel = new OrderLookupViewModel(orderService);
            var inventoryViewModel = new InventoryViewModel(inventoryService);
            var settingsViewModel = new SettingsViewModel(settingsStore);

            var mainViewModel = new MainViewModel(
                authStore,
                cartStore,
                navigationService,
                authService,
                googleOAuthService,
                settingsStore,
                posViewModel,
                orderLookupViewModel,
                inventoryViewModel,
                settingsViewModel);

            MainWindow = new MainWindow
            {
                DataContext = mainViewModel
            };
            MainWindow.Show();
        }
        catch (Exception exception)
        {
            ShowFatalError("Loi khoi dong ung dung", exception);
            Shutdown(-1);
        }
    }

    private void OnDispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
    {
        ShowFatalError("Loi UI chua duoc xu ly", e.Exception);
        e.Handled = true;
        Shutdown(-1);
    }

    private void OnCurrentDomainUnhandledException(object? sender, UnhandledExceptionEventArgs e)
    {
        if (e.ExceptionObject is Exception exception)
        {
            ShowFatalError("Loi ung dung chua duoc xu ly", exception);
        }
        else
        {
            MessageBox.Show(
                "Ung dung da gap loi khong xac dinh va phai dong.",
                "Bookstore POS",
                MessageBoxButton.OK,
                MessageBoxImage.Error);
        }
    }

    private static void ShowFatalError(string title, Exception exception)
    {
        MessageBox.Show(
            $"{title}\n\n{exception}",
            "Bookstore POS",
            MessageBoxButton.OK,
            MessageBoxImage.Error);
    }
}
