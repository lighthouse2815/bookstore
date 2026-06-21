using Bookstore.Desktop.Config;
using CommunityToolkit.Mvvm.ComponentModel;
using System.Windows;
using System.Windows.Media;

namespace Bookstore.Desktop.Stores;

public partial class AppSettingsStore : ObservableObject
{
    private static readonly IReadOnlyDictionary<string, string> LightThemeBrushes = new Dictionary<string, string>
    {
        ["AppBackgroundBrush"] = "#F6F8FB",
        ["SurfaceBrush"] = "#FFFFFF",
        ["SurfaceAltBrush"] = "#F8FAFC",
        ["CardBrush"] = "#FFFFFF",
        ["SidebarBrush"] = "#1F2937",
        ["SidebarForegroundBrush"] = "#FFFFFF",
        ["SidebarDividerBrush"] = "#4B5563",
        ["PrimaryBrush"] = "#2563EB",
        ["BorderBrushLight"] = "#DDE3EA",
        ["PrimaryTextBrush"] = "#111827",
        ["SecondaryTextBrush"] = "#64748B",
        ["TextBoxBackgroundBrush"] = "#FFFFFF",
        ["TextBoxForegroundBrush"] = "#111827",
        ["TextBoxBorderBrush"] = "#CBD5E1",
        ["InfoBrush"] = "#2563EB"
    };

    private static readonly IReadOnlyDictionary<string, string> DarkThemeBrushes = new Dictionary<string, string>
    {
        ["AppBackgroundBrush"] = "#0F172A",
        ["SurfaceBrush"] = "#111827",
        ["SurfaceAltBrush"] = "#1E293B",
        ["CardBrush"] = "#111827",
        ["SidebarBrush"] = "#020617",
        ["SidebarForegroundBrush"] = "#E5E7EB",
        ["SidebarDividerBrush"] = "#334155",
        ["PrimaryBrush"] = "#60A5FA",
        ["BorderBrushLight"] = "#334155",
        ["PrimaryTextBrush"] = "#E5E7EB",
        ["SecondaryTextBrush"] = "#94A3B8",
        ["TextBoxBackgroundBrush"] = "#0B1220",
        ["TextBoxForegroundBrush"] = "#E5E7EB",
        ["TextBoxBorderBrush"] = "#475569",
        ["InfoBrush"] = "#93C5FD"
    };

    public AppSettingsStore()
    {
        ApplyTheme(IsDarkMode);
    }

    [ObservableProperty]
    private string apiBaseUrl = AppConfig.DefaultApiBaseUrl;

    [ObservableProperty]
    private string googleClientId = AppConfig.DefaultGoogleClientId;

    [ObservableProperty]
    private bool isDarkMode;

    public void UpdateBaseUrl(string value)
    {
        var normalized = string.IsNullOrWhiteSpace(value)
            ? AppConfig.DefaultApiBaseUrl
            : value.Trim().TrimEnd('/');
        ApiBaseUrl = normalized;
    }

    public void UpdateGoogleClientId(string value)
    {
        GoogleClientId = string.IsNullOrWhiteSpace(value)
            ? AppConfig.DefaultGoogleClientId
            : value.Trim();
    }

    public void UpdateTheme(bool value)
    {
        IsDarkMode = value;
        ApplyTheme(value);
    }

    private static void ApplyTheme(bool isDarkMode)
    {
        if (Application.Current is null)
        {
            return;
        }

        var palette = isDarkMode ? DarkThemeBrushes : LightThemeBrushes;

        foreach (var (key, hexColor) in palette)
        {
            if (ColorConverter.ConvertFromString(hexColor) is not Color color)
            {
                continue;
            }
            Application.Current.Resources[key] = new SolidColorBrush(color);
        }
    }
}
