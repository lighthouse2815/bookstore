using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class SettingsViewModel : ObservableObject
{
    private readonly AppSettingsStore settingsStore;

    public SettingsViewModel(AppSettingsStore settingsStore)
    {
        this.settingsStore = settingsStore;
        apiBaseUrl = settingsStore.ApiBaseUrl;
        googleClientId = settingsStore.GoogleClientId;
        isDarkMode = settingsStore.IsDarkMode;
    }

    [ObservableProperty]
    private string apiBaseUrl;

    [ObservableProperty]
    private string googleClientId;

    [ObservableProperty]
    private bool isDarkMode;

    [ObservableProperty]
    private string message = "";

    partial void OnIsDarkModeChanged(bool value)
    {
        settingsStore.UpdateTheme(value);
    }

    [RelayCommand]
    private void Save()
    {
        settingsStore.UpdateBaseUrl(ApiBaseUrl);
        settingsStore.UpdateGoogleClientId(GoogleClientId);
        settingsStore.UpdateTheme(IsDarkMode);
        ApiBaseUrl = settingsStore.ApiBaseUrl;
        GoogleClientId = settingsStore.GoogleClientId;
        Message = "Da luu cau hinh.";
    }
}
