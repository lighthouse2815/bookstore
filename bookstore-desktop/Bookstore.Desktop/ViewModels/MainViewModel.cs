using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class MainViewModel : ObservableObject
{
    private readonly AuthStore authStore;
    private readonly PosCartStore cartStore;
    private readonly NavigationService navigationService;
    private readonly AuthService authService;
    private readonly AppSettingsStore settingsStore;
    private readonly CustomerDisplayWindowService customerDisplayWindowService;

    public MainViewModel(
        AuthStore authStore,
        PosCartStore cartStore,
        NavigationService navigationService,
        AuthService authService,
        GoogleOAuthService googleOAuthService,
        AppSettingsStore settingsStore,
        PosViewModel posViewModel,
        OrderLookupViewModel orderLookupViewModel,
        InventoryViewModel inventoryViewModel,
        ReportsViewModel reportsViewModel,
        SettingsViewModel settingsViewModel,
        CustomerDisplayWindowService customerDisplayWindowService)
    {
        this.authStore = authStore;
        this.cartStore = cartStore;
        this.navigationService = navigationService;
        this.authService = authService;
        this.settingsStore = settingsStore;
        this.customerDisplayWindowService = customerDisplayWindowService;
        PosViewModel = posViewModel;
        OrderLookupViewModel = orderLookupViewModel;
        InventoryViewModel = inventoryViewModel;
        ReportsViewModel = reportsViewModel;
        SettingsViewModel = settingsViewModel;
        LoginViewModel = new LoginViewModel(authService, googleOAuthService, settingsStore);
        LoginViewModel.LoginSucceeded += (_, _) =>
        {
            NavigatePos();
            customerDisplayWindowService.Show();
        };

        navigationService.PropertyChanged += (_, args) =>
        {
            if (args.PropertyName == nameof(NavigationService.CurrentViewModel))
            {
                OnPropertyChanged(nameof(ActiveViewModel));
            }
        };
        authStore.PropertyChanged += (_, _) =>
        {
            OnPropertyChanged(nameof(IsAuthenticated));
            OnPropertyChanged(nameof(IsAdmin));
            OnPropertyChanged(nameof(CurrentUserText));
            if (!authStore.IsAuthenticated)
            {
                customerDisplayWindowService.CloseAndReset();
            }
        };
        settingsStore.PropertyChanged += (_, args) =>
        {
            if (args.PropertyName == nameof(AppSettingsStore.IsDarkMode))
            {
                OnPropertyChanged(nameof(IsDarkMode));
                OnPropertyChanged(nameof(ThemeButtonText));
            }
        };
        navigationService.NavigateTo(PosViewModel);
    }

    public LoginViewModel LoginViewModel { get; }
    public PosViewModel PosViewModel { get; }
    public OrderLookupViewModel OrderLookupViewModel { get; }
    public InventoryViewModel InventoryViewModel { get; }
    public ReportsViewModel ReportsViewModel { get; }
    public SettingsViewModel SettingsViewModel { get; }

    public object? ActiveViewModel => navigationService.CurrentViewModel;
    public bool IsAuthenticated => authStore.IsAuthenticated;
    public bool IsAdmin => authStore.CurrentUser?.IsAdmin == true;
    public bool IsDarkMode => settingsStore.IsDarkMode;
    public string ThemeButtonText => IsDarkMode ? "☀  Sáng" : "☾  Tối";
    public string CurrentUserText => authStore.CurrentUser == null ? "" : $"Nhân viên: {authStore.CurrentUser.DisplayName}";

    [RelayCommand]
    private void NavigatePos()
    {
        navigationService.NavigateTo(PosViewModel);
    }

    [RelayCommand]
    private void NavigateOrderLookup()
    {
        navigationService.NavigateTo(OrderLookupViewModel);
    }

    [RelayCommand]
    private async Task NavigateInventoryAsync()
    {
        navigationService.NavigateTo(InventoryViewModel);
        await InventoryViewModel.LoadAsync();
    }

    [RelayCommand]
    private async Task NavigateReportsAsync()
    {
        if (!IsAdmin)
        {
            return;
        }

        navigationService.NavigateTo(ReportsViewModel);
        await ReportsViewModel.LoadAsync();
    }

    [RelayCommand]
    private void NavigateSettings()
    {
        navigationService.NavigateTo(SettingsViewModel);
    }

    [RelayCommand]
    private void OpenCustomerDisplay()
    {
        customerDisplayWindowService.Show(activate: true);
    }

    [RelayCommand]
    private void ToggleTheme()
    {
        settingsStore.UpdateTheme(!settingsStore.IsDarkMode);
    }

    [RelayCommand]
    private async Task LogoutAsync()
    {
        customerDisplayWindowService.CloseAndReset();
        await authService.LogoutAsync();
        cartStore.Clear();
    }
}
