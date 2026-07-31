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
        googleClientId = settingsStore.GoogleClientId;
        isDarkMode = settingsStore.IsDarkMode;
        settingsStore.PropertyChanged += (_, args) =>
        {
            if (args.PropertyName == nameof(AppSettingsStore.IsDarkMode)
                && IsDarkMode != settingsStore.IsDarkMode)
            {
                IsDarkMode = settingsStore.IsDarkMode;
            }
        };
    }

    [ObservableProperty]
    private string googleClientId;

    [ObservableProperty]
    private bool isDarkMode;

    [ObservableProperty]
    private string message = "Đã tự động nạp cấu hình từ .env.";

    partial void OnIsDarkModeChanged(bool value)
    {
        settingsStore.UpdateTheme(value);
    }

    [RelayCommand]
    private void Save()
    {
        settingsStore.UpdateGoogleClientId(GoogleClientId);
        settingsStore.UpdateTheme(IsDarkMode);
        GoogleClientId = settingsStore.GoogleClientId;
        Message = "Đã áp dụng. URL backend được nạp từ file .env khi khởi động.";
    }
}
