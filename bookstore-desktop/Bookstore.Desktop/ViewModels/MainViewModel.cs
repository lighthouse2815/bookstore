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
        SettingsViewModel settingsViewModel)
    {
        this.authStore = authStore;
        this.cartStore = cartStore;
        this.navigationService = navigationService;
        this.authService = authService;
        PosViewModel = posViewModel;
        OrderLookupViewModel = orderLookupViewModel;
        InventoryViewModel = inventoryViewModel;
        SettingsViewModel = settingsViewModel;
        LoginViewModel = new LoginViewModel(authService, googleOAuthService, settingsStore);
        LoginViewModel.LoginSucceeded += (_, _) => NavigatePos();

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
            OnPropertyChanged(nameof(CurrentUserText));
        };
        navigationService.NavigateTo(PosViewModel);
    }

    public LoginViewModel LoginViewModel { get; }
    public PosViewModel PosViewModel { get; }
    public OrderLookupViewModel OrderLookupViewModel { get; }
    public InventoryViewModel InventoryViewModel { get; }
    public SettingsViewModel SettingsViewModel { get; }

    public object? ActiveViewModel => navigationService.CurrentViewModel;
    public bool IsAuthenticated => authStore.IsAuthenticated;
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
    private void NavigateInventory()
    {
        navigationService.NavigateTo(InventoryViewModel);
    }

    [RelayCommand]
    private void NavigateSettings()
    {
        navigationService.NavigateTo(SettingsViewModel);
    }

    [RelayCommand]
    private async Task LogoutAsync()
    {
        await authService.LogoutAsync();
        cartStore.Clear();
    }
}
